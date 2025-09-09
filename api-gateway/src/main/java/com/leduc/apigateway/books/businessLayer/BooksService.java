package com.leduc.apigateway.books.businessLayer;

import com.leduc.apigateway.books.presentationLayer.BookRequestModel;
import com.leduc.apigateway.books.presentationLayer.BookResponseModel;

import java.util.List;

public interface BooksService {
    List<BookResponseModel> getAllBooks();
    BookResponseModel getBookById(String bookId);
    BookResponseModel createBook(BookRequestModel bookRequestModel);
    BookResponseModel updateBook(String bookId, BookRequestModel bookRequestModel);
    void deleteBook(String bookId);
}
