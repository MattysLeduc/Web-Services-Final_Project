package com.leduc.books.businessLayer;



import com.leduc.books.dataAccessLayer.LoanStatus;
import com.leduc.books.presentationLayer.BookRequestModel;
import com.leduc.books.presentationLayer.BookResponseModel;

import java.util.List;

public interface BookService {

    List<BookResponseModel> getAllBooks();
    BookResponseModel getBookById(String bookId);
    BookResponseModel createBook(BookRequestModel bookRequestModel);
    BookResponseModel updateBook(String bookId, BookRequestModel bookRequestModel);
    void deleteBook(String bookId);
    // for aggregate Invariant
    Boolean updateBookCopies(String bookId, LoanStatus loanStatus);
}
