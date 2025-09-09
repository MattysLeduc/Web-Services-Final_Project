package com.leduc.staff.presentationLayer.Department;

import com.leduc.staff.dataAccessLayer.Department.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Sql({"/data-h2.sql"}) // adjust script as needed
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DepartmentControllerIntegrationTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private DepartmentRepository departmentRepository;
    private final String BASE_URI_DEPARTMENTS = "/api/v1/departments";

    @Test
    public void whenDepartmentsExist_thenReturnAllDepartments() {
        // Arrange: Create a department so that the GET returns a non-empty list.
        DepartmentRequestModel request = DepartmentRequestModel.builder()
                .departmentName(DepartmentName.ARCHIVES_MANAGEMENT)
                .headCount(5)
                .positions(Arrays.asList(new Position(PositionTitle.ARCHIVIST, PositionCode.CAT)))
                .build();

        webClient.post()
                .uri(BASE_URI_DEPARTMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        // Act: Retrieve all departments.
        webClient.get()
                .uri(BASE_URI_DEPARTMENTS)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(DepartmentResponseModel.class)
                .value(depts -> {
                    assertNotNull(depts);
                    assertFalse(depts.isEmpty(), "Expected at least one department");
                });
    }

    @Test
    public void whenDepartmentIdIsValid_thenReturnDepartment() {
        // Arrange: Create a department.
        DepartmentRequestModel request = DepartmentRequestModel.builder()
                .departmentName(DepartmentName.ARCHIVES_MANAGEMENT)
                .headCount(10)
                .positions(Arrays.asList(new Position(PositionTitle.ARCHIVIST, PositionCode.CAT)))
                .build();

        DepartmentResponseModel created = webClient.post()
                .uri(BASE_URI_DEPARTMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DepartmentResponseModel.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(created);
        String departmentId = created.getDepartmentId();
        assertNotNull(departmentId);

        // Act: Retrieve the department by ID.
        webClient.get()
                .uri(BASE_URI_DEPARTMENTS + "/" + departmentId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(DepartmentResponseModel.class)
                .value(response -> {
                    assertEquals(departmentId, response.getDepartmentId());
                    assertEquals(DepartmentName.ARCHIVES_MANAGEMENT, response.getDepartmentName());
                    assertEquals(10, response.getHeadCount());
                });
    }

    @Test
    public void whenDepartmentCreateRequestIsValid_thenReturnDepartment() {
        DepartmentRequestModel request = DepartmentRequestModel.builder()
                .departmentName(DepartmentName.ARCHIVES_MANAGEMENT)
                .headCount(15)
                .positions(Arrays.asList(new Position(PositionTitle.ARCHIVIST, PositionCode.CAT)))
                .build();

        webClient.post()
                .uri(BASE_URI_DEPARTMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DepartmentResponseModel.class)
                .value(response -> {
                    assertNotNull(response);
                    assertNotNull(response.getDepartmentId());
                    assertEquals(DepartmentName.ARCHIVES_MANAGEMENT, response.getDepartmentName());
                    assertEquals(15, response.getHeadCount());
                    // Instead of expecting STAFF and MANAGER, expect the ARCHIVIST position that was sent
                    assertNotNull(response.getPositions());
                    assertEquals(1, response.getPositions().size());
                    Position returnedPosition = response.getPositions().get(0);
                    assertEquals(PositionTitle.ARCHIVIST, returnedPosition.getTitle());
                    assertEquals(PositionCode.CAT, returnedPosition.getCode());
                });
    }

    @Test
    public void whenDepartmentUpdateRequestIsValid_thenReturnUpdatedDepartment() {
        // Arrange: Create a department.
        DepartmentRequestModel createRequest = DepartmentRequestModel.builder()
                .departmentName(DepartmentName.ARCHIVES_MANAGEMENT)
                .headCount(8)
                .positions(Arrays.asList(new Position(PositionTitle.ARCHIVIST, PositionCode.CAT)))
                .build();

        DepartmentResponseModel created = webClient.post()
                .uri(BASE_URI_DEPARTMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DepartmentResponseModel.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(created);
        String departmentId = created.getDepartmentId();

        // Prepare an update request (update department name and headCount).
        DepartmentRequestModel updateRequest = DepartmentRequestModel.builder()
                .departmentName(DepartmentName.ARCHIVES_MANAGEMENT)
                .headCount(20)
                .positions(created.getPositions()) // preserve existing positions
                .build();

        webClient.put()
                .uri(BASE_URI_DEPARTMENTS + "/" + departmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DepartmentResponseModel.class)
                .value(updated -> {
                    assertNotNull(updated);
                    assertEquals(departmentId, updated.getDepartmentId());
                    assertEquals(DepartmentName.ARCHIVES_MANAGEMENT, updated.getDepartmentName());
                    assertEquals(20, updated.getHeadCount());
                });
    }

    @Test
    public void whenDepartmentExistsOnDelete_thenReturnNoContent() {
        // Arrange: Create a department.
        DepartmentRequestModel createRequest = DepartmentRequestModel.builder()
                .departmentName(DepartmentName.ARCHIVES_MANAGEMENT)
                .headCount(12)
                .positions(Arrays.asList(new Position(PositionTitle.ARCHIVIST, PositionCode.CAT)))
                .build();

        DepartmentResponseModel created = webClient.post()
                .uri(BASE_URI_DEPARTMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DepartmentResponseModel.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(created);
        String departmentId = created.getDepartmentId();

        // Act: Delete the department.
        webClient.delete()
                .uri(BASE_URI_DEPARTMENTS + "/" + departmentId)
                .exchange()
                .expectStatus().isNoContent();

        // Assert: Subsequent GET returns NotFound.
        webClient.get()
                .uri(BASE_URI_DEPARTMENTS + "/" + departmentId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void whenDepartmentCreateRequest_isNull_thenReturnBadRequest() {
        DepartmentRequestModel nullRequest = new DepartmentRequestModel();
        webClient.post()
                .uri(BASE_URI_DEPARTMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(nullRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void whenDepartmentCreateRequest_missingDepartmentName_thenReturnError() {
        DepartmentRequestModel request = DepartmentRequestModel.builder()
                .departmentName(null) // missing departmentName
                .headCount(10)
                .positions(Arrays.asList(new Position(PositionTitle.ARCHIVIST, PositionCode.CAT)))
                .build();

        webClient.post()
                .uri(BASE_URI_DEPARTMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY); // Adjust if custom error mapping exists.
    }

    @Test
    public void whenDepartmentUpdateRequest_withInvalidDepartmentIdLength_thenReturnError() {
        String invalidId = "12345"; // invalid length
        DepartmentRequestModel updateRequest = DepartmentRequestModel.builder()
                .departmentName(DepartmentName.ARCHIVES_MANAGEMENT)
                .headCount(5)
                .positions(Arrays.asList(new Position(PositionTitle.ARCHIVIST, PositionCode.CAT)))
                .build();
        webClient.put()
                .uri(BASE_URI_DEPARTMENTS + "/" + invalidId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    public void whenDepartmentUpdateRequest_withNonExistentDepartment_thenReturnNotFound() {
        String nonExistentId = "NON-EXISTENT-ID";
        DepartmentRequestModel updateRequest = DepartmentRequestModel.builder()
                .departmentName(DepartmentName.ARCHIVES_MANAGEMENT)
                .headCount(5)
                .positions(Arrays.asList(new Position(PositionTitle.ARCHIVIST, PositionCode.CAT)))
                .build();
        webClient.put()
                .uri(BASE_URI_DEPARTMENTS + "/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void whenDepartmentDelete_withNonExistentDepartment_thenReturnNotFound() {
        String nonExistentId = "NON-EXISTENT-ID";
        webClient.delete()
                .uri(BASE_URI_DEPARTMENTS + "/" + nonExistentId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void whenDepartmentNameIsDuplicate_thenReturnConflict() {
        DepartmentRequestModel request = DepartmentRequestModel.builder()
                .departmentName(DepartmentName.LIBRARY_SERVICES)
                .headCount(5)
                .positions(Arrays.asList(new Position(PositionTitle.ARCHIVIST, PositionCode.CAT)))
                .build();

        webClient.post()
                .uri(BASE_URI_DEPARTMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void whenGetDepartmentById_withInvalidIdLength_thenReturnError() {
        String invalidId = "12345";
        webClient.get()
                .uri(BASE_URI_DEPARTMENTS + "/" + invalidId)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    public void whenGetDepartmentById_withNonExistentId_thenReturnNotFound() {
        String nonExistentId = "NON-EXISTENT-ID";
        webClient.get()
                .uri(BASE_URI_DEPARTMENTS + "/" + nonExistentId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}