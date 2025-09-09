package com.leduc.loans.domainclientLayer.books;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leduc.loans.utils.exceptions.InvalidInputException;
import com.leduc.loans.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.books-service.host=localhost",
        "app.books-service.port=7001"
})
class BooksServiceClientTest {

    @Autowired private RestTemplate restTemplate;
    @Autowired private ObjectMapper mapper;
    @Autowired private BooksServiceClient client;

    private MockRestServiceServer server;
    private final String BASE = "http://localhost:7001/api/v1/books";

    @BeforeEach
    void init() {
        server = MockRestServiceServer.createServer(restTemplate);
    }



    @Test
    void getAllBooks_whenFound_returnsList() throws Exception {
        var b1 = BookModel.builder()
                .bookId("6fa459ea-ee8a-3ca4-894e-d6f1d55b4f2a").title("T1")
                .authorFirstName("A").authorLastName("B")
                .bookType("EBOOK").genre("FICTION").isbn("978")
                .build();
        var list = List.of(b1);

        server.expect(requestTo(BASE))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(list), MediaType.APPLICATION_JSON));

        List<BookModel> result = client.getAllBooks();
        assertThat(result).hasSize(1)
                .first().usingRecursiveComparison().isEqualTo(b1);
    }

    @Test
    void getBookByBookId_whenFound_returnsModel() throws Exception {
        var b = BookModel.builder()
                .bookId("6fa459ea-ee8a-3ca4-894e-d6f1d55b4f2a").title("T2")
                .authorFirstName("X").authorLastName("Y")
                .bookType("HARDCOVER").genre("SCIENCE").isbn("123")
                .build();

        server.expect(requestTo(BASE + "/6fa459ea-ee8a-3ca4-894e-d6f1d55b4f2a"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(b), MediaType.APPLICATION_JSON));

        BookModel result = client.getBookByBookId("6fa459ea-ee8a-3ca4-894e-d6f1d55b4f2a");
        assertThat(result).usingRecursiveComparison().isEqualTo(b);
    }

    @Test
    void getBookByBookId_when404_throwsNotFound() {
        server.expect(requestTo(BASE + "/6fa459ea-ee8a-3ca4-894e-d6f1d55b4f2b"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.getBookByBookId("6fa459ea-ee8a-3ca4-894e-d6f1d55b4f2b"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getBookByBookId_when422_throwsInvalidInput() {
        server.expect(requestTo(BASE + "/6fa459ea-ee8a-3ca4-894e-d6f1d55b4f2"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY));

        assertThatThrownBy(() -> client.getBookByBookId("bad"))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void getAllBooks_when404_throwsNotFound() {
        server.expect(requestTo(BASE))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Books down\"}"));

        assertThatThrownBy(client::getAllBooks)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Books down");
    }

    @Test
    void getAllBooks_when422_throwsInvalidInput() {
        server.expect(requestTo(BASE))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Bad books request\"}"));

        assertThatThrownBy(client::getAllBooks)
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Bad books request");
    }

    @Test
    void createBook_whenNull_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> client.createBook(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("BookModel must not be null");
    }

    @Test
    void createBook_whenSuccess_returnsCreatedBook() throws Exception {
        var request = BookModel.builder()
                .isbn("978-0-00-000000-0")
                .title("New Book")
                .authorFirstName("John")
                .authorLastName("Doe")
                .genre("MYSTERY")
                .bookType("HARDCOVER")
                .build();
        var response = BookModel.builder()
                .bookId("6fa459ea-ee8a-3ca4-894e-d6f1d55b4f2c")
                .isbn(request.getIsbn())
                .title(request.getTitle())
                .authorFirstName(request.getAuthorFirstName())
                .authorLastName(request.getAuthorLastName())
                .genre(request.getGenre())
                .bookType(request.getBookType())
                .build();

        server.expect(requestTo(BASE))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(mapper.writeValueAsString(request)))
                .andRespond(withSuccess(mapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

        BookModel result = client.createBook(request);
        assertThat(result).usingRecursiveComparison().isEqualTo(response);
    }

    @Test
    void createBook_when404_throwsNotFoundException() throws Exception {
        var req = BookModel.builder().isbn("978").build();

        server.expect(requestTo(BASE))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Book service down\"}"));

        assertThatThrownBy(() -> client.createBook(req))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Book service down");
    }

    @Test
    void createBook_when422_throwsInvalidInputException() throws Exception {
        var req = BookModel.builder().isbn("978").build();

        server.expect(requestTo(BASE))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Bad payload\"}"));

        assertThatThrownBy(() -> client.createBook(req))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Bad payload");
    }


    @Test
    void updateBook_whenIdInvalid_throwsIllegalArgumentException() {
        var dummy = BookModel.builder().isbn("978").build();
        assertThatThrownBy(() -> client.updateBook("short-id", dummy))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book ID must be exactly 36 characters long");
    }

    @Test
    void updateBook_whenRequestNull_throwsIllegalArgumentException() {
        String id = "6fa459ea-ee8a-3ca4-894e-d6f1d55b4f2c";
        assertThatThrownBy(() -> client.updateBook(id, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("BookModel must not be null");
    }

    @Test
    void updateBook_whenSuccess_returnsUpdatedBook() throws Exception {
        String id = "6fa459ea-ee8a-3ca4-894e-d6f1d55b4f2c";
        var request = BookModel.builder()
                .isbn("978-1-23-456789-0")
                .title("Updated Title")
                .authorFirstName("Alice")
                .authorLastName("Smith")
                .genre("SCI-FI")
                .bookType("EBOOK")
                .build();
        var response = BookModel.builder()
                .bookId(id)
                .isbn(request.getIsbn())
                .title(request.getTitle())
                .authorFirstName(request.getAuthorFirstName())
                .authorLastName(request.getAuthorLastName())
                .genre(request.getGenre())
                .bookType(request.getBookType())
                .build();

        server.expect(requestTo(BASE + "/" + id))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().json(mapper.writeValueAsString(request)))
                .andRespond(withNoContent());

        server.expect(requestTo(BASE + "/" + id))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

        BookModel result = client.updateBook(id, request);
        assertThat(result).usingRecursiveComparison().isEqualTo(response);
    }

    @Test
    void updateBook_when404_throwsNotFoundException() throws Exception {
        String id = "6fa459ea-ee8a-3ca4-894e-d6f1d55b4f2c";
        var req = BookModel.builder().isbn("978").build();

        server.expect(requestTo(BASE + "/" + id))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"No such book\"}"));

        assertThatThrownBy(() -> client.updateBook(id, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No such book");
    }

    @Test
    void updateBook_when422_throwsInvalidInputException() throws Exception {
        String id = "6fa459ea-ee8a-3ca4-894e-d6f1d55b4f2c";
        var req = BookModel.builder().isbn("978").build();

        server.expect(requestTo(BASE + "/" + id))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid update\"}"));

        assertThatThrownBy(() -> client.updateBook(id, req))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Invalid update");
    }


    @Test
    void deleteBook_whenIdInvalid_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> client.deleteBook("too-short"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book ID must be exactly 36 characters long");
    }

    @Test
    void deleteBook_whenSuccess_completesSilently() {
        String id = "6fa459ea-ee8a-3ca4-894e-d6f1d55b4f2c";

        server.expect(requestTo(BASE + "/" + id))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withNoContent());

        client.deleteBook(id);
    }

    @Test
    void deleteBook_when404_throwsNotFoundException() throws Exception {
        String id = "6fa459ea-ee8a-3ca4-894e-d6f1d55b4f2c";

        server.expect(requestTo(BASE + "/" + id))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Book not found\"}"));

        assertThatThrownBy(() -> client.deleteBook(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Book not found");
    }

    @Test
    void deleteBook_when422_throwsInvalidInputException() throws Exception {
        String id = "6fa459ea-ee8a-3ca4-894e-d6f1d55b4f2c";

        server.expect(requestTo(BASE + "/" + id))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Cannot delete\"}"));

        assertThatThrownBy(() -> client.deleteBook(id))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Cannot delete");
    }

}