package com.leduc.apigateway.books.presentationLayer;

import com.leduc.apigateway.books.businessLayer.BooksService;
import com.leduc.apigateway.utils.exceptions.InvalidInputException;
import com.leduc.apigateway.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class BooksControllerUnitTest {

    @Autowired
    BooksController booksController;

    @MockitoBean
    BooksService booksService;

    private final String VALID_ID   = "123e4567-e89b-12d3-a456-426614174000";
    private final String MISSING_ID = "00000000-0000-0000-0000-000000000000";
    private final String BAD_ID     = "short-id";

    @Test
    void getAllBooks_thenOk() {
        when(booksService.getAllBooks()).thenReturn(Collections.emptyList());

        ResponseEntity<List<BookResponseModel>> resp = booksController.getAllBooks();
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().isEmpty());
    }

    @Test
    void getBookById_thenOk() {
        var book = new BookResponseModel();
        book.setBookId(VALID_ID);
        when(booksService.getBookById(VALID_ID)).thenReturn(book);

        ResponseEntity<BookResponseModel> resp = booksController.getBookById(VALID_ID);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(VALID_ID, resp.getBody().getBookId());
    }

    @Test
    void getBookById_notFound_throwsNotFound() {
        when(booksService.getBookById(MISSING_ID))
                .thenThrow(new NotFoundException("Book not found"));

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                booksController.getBookById(MISSING_ID)
        );
        assertEquals("Book not found", ex.getMessage());
    }

    @Test
    void getBookById_invalid_throwsInvalidInput() {
        when(booksService.getBookById(BAD_ID))
                .thenThrow(new InvalidInputException("Invalid book ID"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                booksController.getBookById(BAD_ID)
        );
        assertEquals("Invalid book ID", ex.getMessage());
    }

    @Test
    void createBook_thenCreated() {
        var req = new BookRequestModel();
        req.setTitle("My Book");

        var created = new BookResponseModel();
        created.setBookId(VALID_ID);
        when(booksService.createBook(req)).thenReturn(created);

        ResponseEntity<BookResponseModel> resp = booksController.createBook(req);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertEquals(VALID_ID, resp.getBody().getBookId());
    }

    @Test
    void createBook_null_throwsInvalidInput() {
        when(booksService.createBook(null))
                .thenThrow(new InvalidInputException("BookRequestModel must not be null"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                booksController.createBook(null)
        );
        assertEquals("BookRequestModel must not be null", ex.getMessage());
    }

    @Test
    void updateBook_thenOk() {
        var req = new BookRequestModel();
        req.setTitle("Updated");

        var updated = new BookResponseModel();
        updated.setBookId(VALID_ID);
        when(booksService.updateBook(VALID_ID, req)).thenReturn(updated);

        ResponseEntity<BookResponseModel> resp = booksController.updateBook(VALID_ID, req);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(updated.getTitle(), resp.getBody().getTitle());
    }

    @Test
    void updateBook_notFound_throwsNotFound() {
        var req = new BookRequestModel();
        when(booksService.updateBook(MISSING_ID, req))
                .thenThrow(new NotFoundException("Book not found"));

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                booksController.updateBook(MISSING_ID, req)
        );
        assertEquals("Book not found", ex.getMessage());
    }

    @Test
    void updateBook_invalid_throwsInvalidInput() {
        var req = new BookRequestModel();
        when(booksService.updateBook(BAD_ID, req))
                .thenThrow(new InvalidInputException("Invalid book ID"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                booksController.updateBook(BAD_ID, req)
        );
        assertEquals("Invalid book ID", ex.getMessage());
    }

    @Test
    void deleteBook_thenNoContent() {
        // service does nothing
        doNothing().when(booksService).deleteBook(VALID_ID);

        ResponseEntity<Void> resp = booksController.deleteBook(VALID_ID);
        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
    }

    @Test
    void deleteBook_notFound_throwsNotFound() {
        doThrow(new NotFoundException("Book not found"))
                .when(booksService).deleteBook(MISSING_ID);

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                booksController.deleteBook(MISSING_ID)
        );
        assertEquals("Book not found", ex.getMessage());
    }

    @Test
    void deleteBook_invalid_throwsInvalidInput() {
        doThrow(new InvalidInputException("Invalid book ID"))
                .when(booksService).deleteBook(BAD_ID);

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                booksController.deleteBook(BAD_ID)
        );
        assertEquals("Invalid book ID", ex.getMessage());
    }
}
