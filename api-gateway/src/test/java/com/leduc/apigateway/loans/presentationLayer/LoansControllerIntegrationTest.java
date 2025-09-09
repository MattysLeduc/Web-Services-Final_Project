package com.leduc.apigateway.loans.presentationLayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leduc.apigateway.loans.domainclientLayer.LoanStatus;
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
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LoansControllerIntegrationTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String API_BASE     = "/api/v1/patrons";
    private static final String SERVICE_BASE = "http://localhost:7004/api/v1/patrons";

    private final String PATRON_ID        = "123e4567-e89b-12d3-a456-426614174000";
    private final String LOAN_ID          = "987e6543-e21b-12d3-a456-426655440000";
    private final String MISSING_LOAN_ID  = "00000000-0000-0000-0000-000000000000";
    private final String BAD_ID           = "bad-id";

    @BeforeEach
    void init() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void whenGetAllLoans_thenReturnList() throws JsonProcessingException {
        var loan = new LoanResponseModel();
        loan.setPatronId(PATRON_ID);
        loan.setLoanId(LOAN_ID);

        mockServer.expect(ExpectedCount.once(),
                        requestTo(SERVICE_BASE + "/" + PATRON_ID + "/loans"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(new LoanResponseModel[]{ loan }),
                        MediaType.APPLICATION_JSON
                ));

        webClient.get().uri(API_BASE + "/" + PATRON_ID + "/loans")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(LoanResponseModel.class)
                .value(list -> {
                    assertEquals(1, list.size());
                    assertEquals(LOAN_ID, list.get(0).getLoanId());
                });
    }

    @Test
    void whenGetAllButPatronIdIsInvalid_thenReturn422() throws JsonProcessingException {
        webClient.get()
                .uri(API_BASE + "/" + BAD_ID + "/loans")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenGetAllLoansAndNoneExist_thenReturnEmptyList() {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(SERVICE_BASE + "/" + PATRON_ID + "/loans"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        webClient.get().uri(API_BASE + "/" + PATRON_ID + "/loans")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(LoanResponseModel.class)
                .hasSize(0);
    }

    @Test
    void whenGetLoanByIdExists_thenReturnLoan() throws Exception {
        // We need TWO GETs: one for existence check, one to return
        var loan = new LoanResponseModel();
        loan.setPatronId(PATRON_ID);
        loan.setLoanId(LOAN_ID);

        mockServer.expect(ExpectedCount.twice(),
                        requestTo(new URI(SERVICE_BASE + "/" + PATRON_ID + "/loans/" + LOAN_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(loan), MediaType.APPLICATION_JSON));

        webClient.get()
                .uri(API_BASE + "/" + PATRON_ID + "/loans/" + LOAN_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoanResponseModel.class)
                .value(resp -> assertEquals(LOAN_ID, resp.getLoanId()));

        mockServer.verify();
    }

    @Test
    void whenGetLoanByIdNotFound_thenReturn404() {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(SERVICE_BASE + "/" + PATRON_ID + "/loans/" + MISSING_LOAN_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.get().uri(API_BASE + "/" + PATRON_ID + "/loans/" + MISSING_LOAN_ID)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenGetLoanByIdInvalid_thenReturn422() {
        webClient.get().uri(API_BASE + "/" + PATRON_ID + "/loans/" + BAD_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenCreateLoan_thenReturn201() throws JsonProcessingException {
        var req = new LoanRequestModel();
        req.setPatronId(PATRON_ID);
        req.setBookId("222e6543-e21b-12d3-a456-426655440000");
        req.setStatus(LoanStatus.CHECKED_OUT);

        var created = new LoanResponseModel();
        created.setPatronId(PATRON_ID);
        created.setLoanId(LOAN_ID);
        created.setStatus(LoanStatus.CHECKED_OUT);

        mockServer.expect(ExpectedCount.once(),
                        requestTo(SERVICE_BASE + "/" + PATRON_ID + "/loans"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(created))
                );

        webClient.post().uri(API_BASE + "/" + PATRON_ID + "/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LoanResponseModel.class)
                .value(resp -> {
                    assertEquals(LOAN_ID, resp.getLoanId());
                    assertEquals(LoanStatus.CHECKED_OUT, resp.getStatus());
                });
    }

    @Test
    void whenCreateLoanInvalid_thenReturn422() {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(SERVICE_BASE + "/" + PATRON_ID + "/loans"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

        webClient.post().uri(API_BASE + "/" + PATRON_ID + "/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenUpdateLoan_thenReturn200() throws Exception {
        var req = new LoanRequestModel();
        req.setBookId("333e6543-e21b-12d3-a456-426655440000");
        req.setStatus(LoanStatus.RETURNED);

        var updated = new LoanResponseModel();
        updated.setPatronId(PATRON_ID);
        updated.setLoanId(LOAN_ID);
        updated.setStatus(LoanStatus.RETURNED);

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI(SERVICE_BASE + "/" + PATRON_ID + "/loans/" + LOAN_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(updated), MediaType.APPLICATION_JSON));

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI(SERVICE_BASE + "/" + PATRON_ID + "/loans/" + LOAN_ID)))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.OK));

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI(SERVICE_BASE + "/" + PATRON_ID + "/loans/" + LOAN_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(updated), MediaType.APPLICATION_JSON));

        webClient.put()
                .uri(API_BASE + "/" + PATRON_ID + "/loans/" + LOAN_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoanResponseModel.class)
                .value(resp -> assertEquals(LoanStatus.RETURNED, resp.getStatus()));

        mockServer.verify();
    }

    @Test
    void whenUpdateLoanNotFound_thenReturn404() throws Exception {
        // GET existence → 404
        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI(SERVICE_BASE + "/" + PATRON_ID + "/loans/" + MISSING_LOAN_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.put()
                .uri(API_BASE + "/" + PATRON_ID + "/loans/" + MISSING_LOAN_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void whenUpdateLoanInvalid_thenReturn422() {
        webClient.put().uri(API_BASE + "/" + PATRON_ID + "/loans/" + BAD_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenDeleteLoan_thenReturn204() throws Exception {
        var existing = new LoanResponseModel();
        existing.setPatronId(PATRON_ID);
        existing.setLoanId(LOAN_ID);

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI(SERVICE_BASE + "/" + PATRON_ID + "/loans/" + LOAN_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(existing), MediaType.APPLICATION_JSON));

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI(SERVICE_BASE + "/" + PATRON_ID + "/loans/" + LOAN_ID)))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        webClient.delete()
                .uri(API_BASE + "/" + PATRON_ID + "/loans/" + LOAN_ID)
                .exchange()
                .expectStatus().isNoContent();

        mockServer.verify();
    }

    @Test
    void whenDeleteLoanNotFound_thenReturn404() throws Exception {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI(SERVICE_BASE + "/" + PATRON_ID + "/loans/" + MISSING_LOAN_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.delete()
                .uri(API_BASE + "/" + PATRON_ID + "/loans/" + MISSING_LOAN_ID)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void whenDeleteLoanInvalid_thenReturn422() {
        webClient.delete().uri(API_BASE + "/" + PATRON_ID + "/loans/" + BAD_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenUpdateLoanNoBody_thenReturn400() throws URISyntaxException {

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI(SERVICE_BASE + "/" + PATRON_ID + "/loans/" + LOAN_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"patronId\":\"" + PATRON_ID + "\",\"loanId\":\"" + LOAN_ID + "\"}",
                        MediaType.APPLICATION_JSON
                ));

        webClient.put()
                .uri(API_BASE + "/" + PATRON_ID + "/loans/" + LOAN_ID)
                .contentType(MediaType.APPLICATION_JSON)
                // no bodyValue()
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        webClient.put()
                .uri(API_BASE + "/" + PATRON_ID + "/loans/" + LOAN_ID)
                .contentType(MediaType.APPLICATION_JSON)
                // ← no .bodyValue(...)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void whenUpdateLoanMalformedJson_thenReturn500() {
        webClient.put()
                .uri(API_BASE + "/" + PATRON_ID + "/loans/" + LOAN_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("10")   // missing value
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void whenUpdateLoanWrongContentType_thenReturn500() {
        webClient.put()
                .uri(API_BASE + "/" + PATRON_ID + "/loans/" + LOAN_ID)
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue("some text")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
