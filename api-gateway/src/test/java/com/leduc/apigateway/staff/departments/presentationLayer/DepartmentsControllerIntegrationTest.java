package com.leduc.apigateway.staff.departments.presentationLayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leduc.apigateway.staff.departments.domainclientLayer.DepartmentName;
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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DepartmentsControllerIntegrationTest {

    @Autowired
    WebTestClient webClient;

    @Autowired
    RestTemplate restTemplate;

    private MockRestServiceServer mockServer;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String API_BASE     = "/api/v1/departments";
    private static final String SERVICE_BASE = "http://localhost:7002/api/v1/departments";

    private final String VALID_ID   = "123e4567-e89b-12d3-a456-426614174000";
    private final String MISSING_ID = "00000000-0000-0000-0000-000000000000";
    private final String BAD_ID     = "bad-id";

    @BeforeEach
    void init() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void whenGetAllDepartments_thenReturnList() throws JsonProcessingException {
        var dept = new DepartmentResponseModel();
        dept.setDepartmentId(VALID_ID);

        mockServer.expect(ExpectedCount.once(),
                        requestTo(SERVICE_BASE))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(new DepartmentResponseModel[]{ dept }),
                        MediaType.APPLICATION_JSON
                ));

        webClient.get().uri(API_BASE)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(DepartmentResponseModel.class)
                .value(list -> {
                    assertEquals(1, list.size());
                    assertEquals(VALID_ID, list.get(0).getDepartmentId());
                });

        mockServer.verify();
    }

    @Test
    void whenGetAllDepartmentsAndNoneExist_thenReturnEmptyList() {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(SERVICE_BASE))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        webClient.get().uri(API_BASE)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(DepartmentResponseModel.class)
                .hasSize(0);

        mockServer.verify();
    }

    @Test
    void whenGetDepartmentByIdExists_thenReturnDepartment() throws Exception {
        var dept = new DepartmentResponseModel();
        dept.setDepartmentId(VALID_ID);

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI(SERVICE_BASE + "/" + VALID_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(dept),
                        MediaType.APPLICATION_JSON
                ));

        webClient.get().uri(API_BASE + "/" + VALID_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(DepartmentResponseModel.class)
                .value(resp -> assertEquals(VALID_ID, resp.getDepartmentId()));

        mockServer.verify();
    }

    @Test
    void whenGetDepartmentByIdNotFound_thenReturn404() {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(SERVICE_BASE + "/" + MISSING_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.get().uri(API_BASE + "/" + MISSING_ID)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void whenGetDepartmentByIdInvalid_thenReturn422() {
        webClient.get().uri(API_BASE + "/" + BAD_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenCreateDepartment_thenReturn201() throws JsonProcessingException {
        var req = new DepartmentRequestModel();
        req.setDepartmentName(DepartmentName.ARCHIVES_MANAGEMENT);

        var created = new DepartmentResponseModel();
        created.setDepartmentId(VALID_ID);

        mockServer.expect(ExpectedCount.once(),
                        requestTo(SERVICE_BASE))
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
                .expectBody(DepartmentResponseModel.class)
                .value(resp -> assertEquals(VALID_ID, resp.getDepartmentId()));

        mockServer.verify();
    }

    @Test
    void whenCreateDepartmentInvalid_thenReturn422() {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(SERVICE_BASE))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

        webClient.post().uri(API_BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        mockServer.verify();
    }

    @Test
    void whenUpdateDepartment_thenReturn200() throws Exception {
        var req = new DepartmentRequestModel();
        req.setDepartmentName(DepartmentName.DIGITAL_RESOURCES);

        var updated = new DepartmentResponseModel();
        updated.setDepartmentId(VALID_ID);

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI(SERVICE_BASE + "/" + VALID_ID)))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.OK));

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI(SERVICE_BASE + "/" + VALID_ID)))
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
                .expectBody(DepartmentResponseModel.class)
                .value(resp -> assertEquals(VALID_ID, resp.getDepartmentId()));

        mockServer.verify();
    }

    @Test
    void whenUpdateDepartmentNotFound_thenReturn404() {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(SERVICE_BASE + "/" + MISSING_ID))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.put().uri(API_BASE + "/" + MISSING_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void whenUpdateDepartmentInvalid_thenReturn422() {
        webClient.put().uri(API_BASE + "/" + BAD_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenDeleteDepartment_thenReturn204() {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(SERVICE_BASE + "/" + VALID_ID))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        webClient.delete().uri(API_BASE + "/" + VALID_ID)
                .exchange()
                .expectStatus().isNoContent();

        mockServer.verify();
    }

    @Test
    void whenDeleteDepartmentNotFound_thenReturn404() {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(SERVICE_BASE + "/" + MISSING_ID))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.delete().uri(API_BASE + "/" + MISSING_ID)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void whenDeleteDepartmentInvalid_thenReturn422() {
        webClient.delete().uri(API_BASE + "/" + BAD_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

}
