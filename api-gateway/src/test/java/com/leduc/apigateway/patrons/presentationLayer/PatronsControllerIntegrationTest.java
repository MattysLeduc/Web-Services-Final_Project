package com.leduc.apigateway.patrons.presentationLayer;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PatronsControllerIntegrationTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String API_BASE     = "/api/v1/patrons";
    private static final String SERVICE_BASE = "http://localhost:7001/api/v1/patrons";

    private final String VALID_ID   = "123e4567-e89b-12d3-a456-426614174000";
    private final String MISSING_ID = "00000000-0000-0000-0000-000000000000";
    private final String BAD_ID     = "short-id";

    @BeforeEach
    void init() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void whenGetAllPatrons_thenReturnList() throws JsonProcessingException {
        var patron = new PatronResponseModel();
        patron.setPatronId(VALID_ID);

        mockServer.expect(ExpectedCount.once(), requestTo(SERVICE_BASE))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(new PatronResponseModel[]{ patron }),
                        MediaType.APPLICATION_JSON
                ));

        webClient.get().uri(API_BASE)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PatronResponseModel.class)
                .value(list -> {
                    assertEquals(1, list.size());
                    assertEquals(VALID_ID, list.get(0).getPatronId());
                });
    }

    @Test
    void whenGetAllPatronsAndNoneExist_thenReturnEmptyList() {
        mockServer.expect(ExpectedCount.once(), requestTo(SERVICE_BASE))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        webClient.get().uri(API_BASE)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PatronResponseModel.class)
                .hasSize(0);
    }

    @Test
    void whenGetPatronByIdExists_thenReturnPatron() throws Exception {
        var patron = new PatronResponseModel();
        patron.setPatronId(VALID_ID);

        mockServer.expect(ExpectedCount.once(), requestTo(new URI(SERVICE_BASE + "/" + VALID_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(patron),
                        MediaType.APPLICATION_JSON
                ));

        webClient.get().uri(API_BASE + "/" + VALID_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PatronResponseModel.class)
                .value(resp -> assertEquals(VALID_ID, resp.getPatronId()));
    }

    @Test
    void whenGetPatronByIdNotFound_thenReturn404() {
        mockServer.expect(ExpectedCount.once(), requestTo(SERVICE_BASE + "/" + MISSING_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.get().uri(API_BASE + "/" + MISSING_ID)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenGetPatronByIdInvalid_thenReturn422() {
        webClient.get().uri(API_BASE + "/" + BAD_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenCreatePatron_thenReturn201() throws JsonProcessingException {
        var req = new PatronRequestModel();
        req.setFirstName("Alice");
        req.setLastName("Leduc");

        var created = new PatronResponseModel();
        created.setPatronId(VALID_ID);

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
                .expectBody(PatronResponseModel.class)
                .value(resp -> assertEquals(VALID_ID, resp.getPatronId()));
    }

    @Test
    void whenCreatePatronInvalid_thenReturn422() {
        mockServer.expect(ExpectedCount.once(), requestTo(SERVICE_BASE))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

        webClient.post().uri(API_BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenUpdatePatron_thenReturn200() throws Exception {
        var req = new PatronRequestModel();
        req.setFirstName("Bob");
        req.setLastName("Smith");

        var updated = new PatronResponseModel();
        updated.setPatronId(VALID_ID);

        mockServer.expect(ExpectedCount.once(), requestTo(new URI(SERVICE_BASE + "/" + VALID_ID)))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.OK));

        mockServer.expect(ExpectedCount.once(), requestTo(new URI(SERVICE_BASE + "/" + VALID_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(updated),
                        MediaType.APPLICATION_JSON
                ));

        webClient.put().uri(API_BASE + "/" + VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PatronResponseModel.class)
                .value(resp -> assertEquals(VALID_ID, resp.getPatronId()));
    }

    @Test
    void whenUpdatePatronNotFound_thenReturn404() {
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
    void whenUpdatePatronInvalid_thenReturn422() {
        webClient.put().uri(API_BASE + "/" + BAD_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenDeletePatron_thenReturn204() {
        mockServer.expect(ExpectedCount.once(), requestTo(SERVICE_BASE + "/" + VALID_ID))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        webClient.delete().uri(API_BASE + "/" + VALID_ID)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void whenDeletePatronNotFound_thenReturn404() {
        mockServer.expect(ExpectedCount.once(), requestTo(SERVICE_BASE + "/" + MISSING_ID))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.delete().uri(API_BASE + "/" + MISSING_ID)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenDeletePatronInvalid_thenReturn422() {
        webClient.delete().uri(API_BASE + "/" + BAD_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
