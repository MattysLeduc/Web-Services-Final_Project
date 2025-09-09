package com.leduc.apigateway.books.businessLayer;

import com.leduc.apigateway.books.domainclientLayer.BooksServiceClient;
import com.leduc.apigateway.books.presentationLayer.BookRequestModel;
import com.leduc.apigateway.books.presentationLayer.BookResponseModel;
import com.leduc.apigateway.utils.exceptions.InvalidInputException;
import com.leduc.apigateway.utils.exceptions.NotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration")
class BooksServiceUnitTest {

    @Mock
    private BooksServiceClient booksServiceClient;

    @InjectMocks
    private BooksServiceImpl service;

    @Test
    void getAllBooks_returnsListWithHateoasLinks() {
        // Arrange
        BookResponseModel b1 = new BookResponseModel();
        b1.setBookId("11111111-1111-1111-1111-111111111111");
        BookResponseModel b2 = new BookResponseModel();
        b2.setBookId("22222222-2222-2222-2222-222222222222");
        when(booksServiceClient.getAllBooks()).thenReturn(Arrays.asList(b1, b2));

        // Act
        List<BookResponseModel> result = service.getAllBooks();

        // Assert
        assertEquals(2, result.size());
        result.forEach(book -> {
            assertTrue(book.getLink("self").isPresent(), "self link present");
            assertTrue(book.getLink("all-books").isPresent(), "all-books link present");
        });
    }

    @Test
    void getBookById_withInvalidId_throwsInvalidInputException() {
        assertThrows(InvalidInputException.class,
                () -> service.getBookById("short-id"));
    }

    @Test
    void getBookById_notFound_throwsNotFoundException() {
        String id = "33333333-3333-3333-3333-333333333333";
        when(booksServiceClient.getBookByBookId(id))
                .thenThrow(EntityNotFoundException.class);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.getBookById(id));
        assertTrue(ex.getMessage().contains(id));
    }

    @Test
    void getBookById_success_returnsBookWithLinks() {
        String id = "44444444-4444-4444-4444-444444444444";
        BookResponseModel model = new BookResponseModel();
        model.setBookId(id);
        when(booksServiceClient.getBookByBookId(id)).thenReturn(model);

        BookResponseModel result = service.getBookById(id);

        assertEquals(id, result.getBookId());
        assertTrue(result.getLink("self").isPresent());
        assertTrue(result.getLink("all-books").isPresent());
    }

    @Test
    void createBook_withNullRequest_throwsInvalidInputException() {
        assertThrows(InvalidInputException.class,
                () -> service.createBook(null));
    }

    @Test
    void createBook_success_returnsBookWithLinks() {
        BookRequestModel req = new BookRequestModel();
        BookResponseModel created = new BookResponseModel();
        created.setBookId("55555555-5555-5555-5555-555555555555");
        when(booksServiceClient.createBook(req)).thenReturn(created);

        BookResponseModel result = service.createBook(req);

        assertEquals(created.getBookId(), result.getBookId());
        assertTrue(result.getLink("self").isPresent());
        assertTrue(result.getLink("all-books").isPresent());
    }

    @Test
    void updateBook_withInvalidId_throwsInvalidInputException() {
        assertThrows(InvalidInputException.class,
                () -> service.updateBook("bad", new BookRequestModel()));
    }

    @Test
    void updateBook_withNullRequest_throwsInvalidInputException() {
        String id = "66666666-6666-6666-6666-666666666666";
        assertThrows(InvalidInputException.class,
                () -> service.updateBook(id, null));
    }

    @Test
    void updateBook_notFound_throwsNotFoundException() {
        String id = "77777777-7777-7777-7777-777777777777";
        BookRequestModel req = new BookRequestModel();
        when(booksServiceClient.updateBook(id, req))
                .thenThrow(EntityNotFoundException.class);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.updateBook(id, req));
        assertTrue(ex.getMessage().contains(id));
    }

    @Test
    void updateBook_success_returnsBookWithLinks() {
        String id = "88888888-8888-8888-8888-888888888888";
        BookRequestModel req = new BookRequestModel();
        BookResponseModel updated = new BookResponseModel();
        updated.setBookId(id);
        when(booksServiceClient.updateBook(id, req)).thenReturn(updated);

        BookResponseModel result = service.updateBook(id, req);

        assertEquals(id, result.getBookId());
        assertTrue(result.getLink("self").isPresent());
        assertTrue(result.getLink("all-books").isPresent());
    }

    @Test
    void deleteBook_withInvalidId_throwsInvalidInputException() {
        assertThrows(InvalidInputException.class,
                () -> service.deleteBook("bad-id"));
    }

    @Test
    void deleteBook_notFound_throwsNotFoundException() {
        String id = "99999999-9999-9999-9999-999999999999";
        doThrow(EntityNotFoundException.class)
                .when(booksServiceClient).deleteBook(id);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.deleteBook(id));
        assertTrue(ex.getMessage().contains(id));
    }

    @Test
    void deleteBook_success_invokesClientDeleteOnce() {
        String id = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
        doNothing().when(booksServiceClient).deleteBook(id);

        service.deleteBook(id);

        verify(booksServiceClient, times(1)).deleteBook(id);
    }
}
