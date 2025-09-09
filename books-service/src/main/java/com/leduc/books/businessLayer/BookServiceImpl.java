package com.leduc.books.businessLayer;


import com.leduc.books.dataAccessLayer.*;
import com.leduc.books.mappingLayer.BookRequestMapper;
import com.leduc.books.mappingLayer.BookResponseMapper;
import com.leduc.books.presentationLayer.BookRequestModel;
import com.leduc.books.presentationLayer.BookResponseModel;


import com.leduc.books.utils.exceptions.BookAlreadyRegisteredException;
import com.leduc.books.utils.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {


        private final BookRepository bookRepository;
        private final BookResponseMapper bookResponseMapper;
        private final BookRequestMapper bookRequestMapper;

        public BookServiceImpl(BookRepository bookRepository, BookResponseMapper bookResponseMapper, BookRequestMapper bookRequestMapper) {
            this.bookRepository = bookRepository;
            this.bookResponseMapper = bookResponseMapper;
            this.bookRequestMapper = bookRequestMapper;
        }

        @Override
        public List<BookResponseModel> getAllBooks() {
            List<Book> books = bookRepository.findAll();
            return bookResponseMapper.entityToResponseModelList(books);
        }

        @Override
        public BookResponseModel getBookById(String bookId) {
            Book foundBook = bookRepository.findBookByBookIdentifier_BookId(bookId);
            if (foundBook == null) {
                throw new NotFoundException("Book with ID " + bookId + " not found");
            }
            if (bookId.length() != 36){
                throw new IllegalArgumentException("Book id length must be 36");
            }
            return bookResponseMapper.entityToResponseModel(foundBook);
        }

        @Override
        public BookResponseModel createBook(BookRequestModel bookRequestModel) {
            if (bookRequestModel == null) {
                throw new IllegalArgumentException("BookRequestModel is null");
            }

            if (bookRequestModel.getTitle() == null){
                throw new RuntimeException("Book needs to have a title");
            }

            if (bookRequestModel.getIsbn() != null && bookRepository.findByIsbn(bookRequestModel.getIsbn()) != null) {
                throw new BookAlreadyRegisteredException("Book with ISBN " + bookRequestModel.getIsbn() + " is already registered.");
            }


            BookIdentifier bookIdentifier = new BookIdentifier(); // Generate identifier
            Book book = bookRequestMapper.toEntity(bookRequestModel, bookIdentifier);

            Book savedBook = bookRepository.save(book);
            return bookResponseMapper.entityToResponseModel(savedBook);
        }

    @Override
    public BookResponseModel updateBook(String bookId, BookRequestModel bookRequestModel) {
        // Retrieve the existing book
        Book existingBook = bookRepository.findBookByBookIdentifier_BookId(bookId);
        if (existingBook == null) {
            throw new NotFoundException("Book not found with ID: " + bookId);
        }
        if (bookId.length() != 36){
            throw new IllegalArgumentException("Book id length must be 36");
        }

            existingBook.setIsbn(bookRequestModel.getIsbn());

            existingBook.setTitle(bookRequestModel.getTitle());

            existingBook.setGenre(GenreName.valueOf(bookRequestModel.getGenre()));

            existingBook.setBookType(BookType.valueOf(bookRequestModel.getBookType()));

            existingBook.setAgeGroup(AgeGroup.valueOf(bookRequestModel.getAgeGroup()));

            existingBook.setPublicationDate(bookRequestModel.getPublicationDate());

            existingBook.setCopiesAvailable(bookRequestModel.getCopiesAvailable());

            Author updatedAuthor = new Author(bookRequestModel.getAuthorFirstName(), bookRequestModel.getAuthorLastName(), bookRequestModel.getAuthorBiography());
            existingBook.setAuthor(updatedAuthor);
        Book updatedBook = bookRepository.save(existingBook);

        return bookResponseMapper.entityToResponseModel(updatedBook);
    }

    @Override
    public void deleteBook(String bookId) {
        Book foundBook = bookRepository.findBookByBookIdentifier_BookId(bookId);
        if (foundBook == null) {
            throw new NotFoundException("Book with ID " + bookId + " not found");
        }
        if (bookId.length() != 36){
            throw new IllegalArgumentException("Book id length must be 36");
        }

        bookRepository.delete(foundBook);
    }

    @Override
    public Boolean updateBookCopies(String bookId, LoanStatus loanStatus) {
        // Validate the book
        Book existingBook = bookRepository.findBookByBookIdentifier_BookId(bookId);
        if (existingBook == null) {
            return false; // Book not found
        }

        if (loanStatus == LoanStatus.CHECKED_OUT) {
            // Reduce the available copies by 1 if the book is being checked out
            if (existingBook.getCopiesAvailable() > 0) {
                existingBook.setCopiesAvailable(existingBook.getCopiesAvailable() - 1);
                bookRepository.save(existingBook);
                return true;
            } else {
                throw new IllegalStateException("No copies available to check out");
            }
        } else if (loanStatus == LoanStatus.RETURNED) {
            // Add back the available copy if the book is returned
            existingBook.setCopiesAvailable(existingBook.getCopiesAvailable() + 1);
            bookRepository.save(existingBook);
            return true;
        }

        return false;
    }
}
