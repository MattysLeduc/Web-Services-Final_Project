package com.leduc.apigateway.books.presentationLayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BooksControllerIntegrationTest {

    @Autowired
    WebTestClient webClient;

    @Autowired
    RestTemplate restTemplate;

    private MockRestServiceServer mockServer;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String API_BASE = "/api/v1/books";
    private static final String SERVICE_BASE = "http://localhost:7003/api/v1/books";

    private final String VALID_ID   = "123e4567-e89b-12d3-a456-426614174000";
    private final String MISSING_ID = "00000000-0000-0000-0000-000000000000";
    private final String BAD_ID     = "short-id";

    @BeforeEach
    void init() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void whenGetAllBooks_thenReturnList() throws JsonProcessingException {
        // stub GET all
        var book = new BookResponseModel();
        book.setBookId(VALID_ID);

        mockServer.expect(ExpectedCount.once(), requestTo(SERVICE_BASE))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new BookResponseModel[]{ book }),
                        MediaType.APPLICATION_JSON));

        webClient.get().uri(API_BASE)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BookResponseModel.class)
                .value(list -> {
                    assertEquals(1, list.size());
                    assertEquals(VALID_ID, list.get(0).getBookId());
                });
    }

    @Test
    void whenGetAllBooksAndNoneExist_thenReturnEmptyList() {
        mockServer.expect(ExpectedCount.once(), requestTo(SERVICE_BASE))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        webClient.get().uri(API_BASE)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BookResponseModel.class)
                .hasSize(0);
    }

    @Test
    void whenGetBookByIdExists_thenReturnBook() throws Exception {
        var book = new BookResponseModel();
        book.setBookId(VALID_ID);

        mockServer.expect(ExpectedCount.once(), requestTo(new URI(SERVICE_BASE + "/" + VALID_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(book),
                        MediaType.APPLICATION_JSON));

        webClient.get().uri(API_BASE + "/" + VALID_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookResponseModel.class)
                .value(resp -> assertEquals(VALID_ID, resp.getBookId()));
    }

    @Test
    void whenGetBookByIdNotFound_thenReturn404() {
        mockServer.expect(ExpectedCount.once(), requestTo(SERVICE_BASE + "/" + MISSING_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.get().uri(API_BASE + "/" + MISSING_ID)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenGetBookByIdInvalid_thenReturn422() {
        webClient.get().uri(API_BASE + "/" + BAD_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenCreateBook_thenReturn201() throws JsonProcessingException {
        var req = new BookRequestModel();
        req.setIsbn("978-0134685991");
        req.setTitle("Effective Java");

        var created = new BookResponseModel();
        created.setBookId(VALID_ID);
        created.setTitle("Effective Java");

        mockServer.expect(ExpectedCount.once(), requestTo(SERVICE_BASE))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(created))
                );

        webClient.post().uri(API_BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BookResponseModel.class)
                .value(resp -> {
                    assertEquals(VALID_ID, resp.getBookId());
                    assertEquals("Effective Java", resp.getTitle());
                });
    }

    @Test
    void whenCreateBookInvalid_thenReturn422() {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(SERVICE_BASE))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

        webClient.post()
                .uri(API_BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        mockServer.verify();
    }

    @Test
    void whenUpdateBook_thenReturn200() throws Exception {
        var req = new BookRequestModel();
        req.setTitle("Updated Title");

        var updated = new BookResponseModel();
        updated.setBookId(VALID_ID);
        updated.setTitle("Updated Title");

        // stub PUT
        mockServer.expect(ExpectedCount.once(), requestTo(new URI(SERVICE_BASE + "/" + VALID_ID)))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.OK));

        // stub follow-up GET by id
        mockServer.expect(ExpectedCount.once(), requestTo(new URI(SERVICE_BASE + "/" + VALID_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(updated),
                        MediaType.APPLICATION_JSON));

        webClient.put().uri(API_BASE + "/" + VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookResponseModel.class)
                .value(resp -> assertEquals("Updated Title", resp.getTitle()));
    }

    @Test
    void whenUpdateBookNotFound_thenReturn404() {
        mockServer.expect(ExpectedCount.once(), requestTo(SERVICE_BASE + "/" + MISSING_ID))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.put().uri(API_BASE + "/" + MISSING_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenUpdateBookInvalid_thenReturn422() {
        webClient.put().uri(API_BASE + "/" + BAD_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenDeleteBook_thenReturn204() {
        mockServer.expect(ExpectedCount.once(), requestTo(SERVICE_BASE + "/" + VALID_ID))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        webClient.delete().uri(API_BASE + "/" + VALID_ID)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void whenDeleteBookNotFound_thenReturn404() {
        mockServer.expect(ExpectedCount.once(), requestTo(SERVICE_BASE + "/" + MISSING_ID))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.delete().uri(API_BASE + "/" + MISSING_ID)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenDeleteBookInvalid_thenReturn422() {
        webClient.delete().uri(API_BASE + "/" + BAD_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
