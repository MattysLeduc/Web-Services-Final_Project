package com.leduc.apigateway.books.businessLayer;

import com.leduc.apigateway.books.domainclientLayer.BooksServiceClient;
import com.leduc.apigateway.books.presentationLayer.BookRequestModel;
import com.leduc.apigateway.books.presentationLayer.BookResponseModel;
import com.leduc.apigateway.books.presentationLayer.BooksController;
import com.leduc.apigateway.utils.exceptions.InvalidInputException;
import com.leduc.apigateway.utils.exceptions.NotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@Service
public class BooksServiceImpl implements BooksService {

    private final BooksServiceClient booksServiceClient;

    public BooksServiceImpl(BooksServiceClient booksServiceClient) {
        this.booksServiceClient = booksServiceClient;
    }

    @Override
    public List<BookResponseModel> getAllBooks() {
        log.debug("BooksServiceImpl.getAllBooks()");
        List<BookResponseModel> books = booksServiceClient.getAllBooks();
        for (BookResponseModel book : books) {
            addHateoasLinks(book);
        }
        return books;
    }

    @Override
    public BookResponseModel getBookById(String bookId) {
        log.debug("BooksServiceImpl.getBookById({})", bookId);
        if (bookId == null || bookId.length() != 36) {
            throw new InvalidInputException("Book ID must be exactly 36 characters long");
        }
        try {
            BookResponseModel book = booksServiceClient.getBookByBookId(bookId);
            return addHateoasLinks(book);
        } catch (EntityNotFoundException ex) {
            throw new NotFoundException("Book with ID " + bookId + " not found", ex);
        }
    }

    @Override
    public BookResponseModel createBook(BookRequestModel bookRequestModel) {
        log.debug("BooksServiceImpl.createBook()");
        if (bookRequestModel == null) {
            throw new InvalidInputException("BookRequestModel must not be null");
        }
        BookResponseModel created = booksServiceClient.createBook(bookRequestModel);
        return addHateoasLinks(created);
    }

    @Override
    public BookResponseModel updateBook(String bookId, BookRequestModel bookRequestModel) {
        log.debug("BooksServiceImpl.updateBook({})", bookId);
        if (bookId == null || bookId.length() != 36) {
            throw new InvalidInputException("Book ID must be exactly 36 characters long");
        }
        if (bookRequestModel == null) {
            throw new InvalidInputException("BookRequestModel must not be null");
        }
        try {
            BookResponseModel updated = booksServiceClient.updateBook(bookId, bookRequestModel);
            return addHateoasLinks(updated);
        } catch (EntityNotFoundException ex) {
            throw new NotFoundException("Book with ID " + bookId + " not found", ex);
        }
    }

    @Override
    public void deleteBook(String bookId) {
        log.debug("BooksServiceImpl.deleteBook({})", bookId);
        if (bookId == null || bookId.length() != 36) {
            throw new InvalidInputException("Book ID must be exactly 36 characters long");
        }
        try {
            booksServiceClient.deleteBook(bookId);
        } catch (EntityNotFoundException ex) {
            throw new NotFoundException("Book with ID " + bookId + " not found", ex);
        }
    }

    private BookResponseModel addHateoasLinks(BookResponseModel book) {
        Link self = linkTo(methodOn(BooksController.class)
                .getBookById(book.getBookId())).withSelfRel();
        book.add(self);

        Link all = linkTo(methodOn(BooksController.class)
                .getAllBooks()).withRel("all-books");
        book.add(all);

        return book;
    }
}
