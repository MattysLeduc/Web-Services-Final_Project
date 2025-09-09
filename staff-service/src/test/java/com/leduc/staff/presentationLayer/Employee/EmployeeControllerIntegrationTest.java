package com.leduc.staff.presentationLayer.Employee;

import com.leduc.staff.dataAccessLayer.Department.PositionTitle;
import com.leduc.staff.dataAccessLayer.Employee.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Sql({"/data-h2.sql"}) // adjust script as needed
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EmployeeControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private EmployeeRepository employeeRepository;

    private final String BASE_URI = "/api/v1/staff";

    @BeforeEach
    public void setup() {
        // Clear out the employee repository before each test.
        employeeRepository.deleteAll();
    }

    @Test
    public void whenEmployeesExist_thenReturnAllEmployees() {
        // First, create an employee using the POST endpoint.
        EmployeeRequestModel employeeRequest = EmployeeRequestModel.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumbers(Collections.emptyList())
                .streetAddress("123 Main St")
                .city("Anytown")
                .province("Anystate")
                .country("USA")
                .postalCode("12345")
                .salary(new BigDecimal("50000"))
                // Assumed that a valid departmentId (a 36-character UUID) is required.
                // Replace "00000000-0000-0000-0000-000000000000" with an actual valid value from your test dataset.
                .departmentId("1048b354-c18f-4109-8282-2a85485bfa5a")
                .positionTitle(PositionTitle.LIBRARIAN)
                .build();

        EmployeeResponseModel createdEmployee = webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(employeeRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(EmployeeResponseModel.class)
                .returnResult()
                .getResponseBody();
        assertThat(createdEmployee).isNotNull();

        // Retrieve all employees and verify that we receive at least the one we just created.
        webTestClient.get()
                .uri(BASE_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EmployeeResponseModel.class)
                .value(list -> {
                    assertThat(list).isNotEmpty();
                    assertThat(list.size()).isEqualTo(1);
                });
    }

    @Test
    public void whenEmployeeIdIsValid_thenReturnEmployee() {
        // Create a new employee first
        EmployeeRequestModel employeeRequest = EmployeeRequestModel.builder()
                .firstName("Alice")
                .lastName("Smith")
                .email("alice.smith@example.com")
                .phoneNumbers(Collections.emptyList())
                .streetAddress("456 Elm St")
                .city("Othertown")
                .province("Otherstate")
                .country("USA")
                .postalCode("67890")
                .salary(new BigDecimal("60000"))
                .departmentId("1048b354-c18f-4109-8282-2a85485bfa5a")
                .positionTitle(PositionTitle.LIBRARIAN)
                .build();

        EmployeeResponseModel created = webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(employeeRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(EmployeeResponseModel.class)
                .returnResult()
                .getResponseBody();
        assertNotNull(created);

        // Now retrieve the employee by ID.
        webTestClient.get()
                .uri(BASE_URI + "/" + created.getEmployeeId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EmployeeResponseModel.class)
                .value(response -> {
                    assertThat(response.getEmployeeId()).isEqualTo(created.getEmployeeId());
                    assertThat(response.getFirstName()).isEqualTo("Alice");
                    assertThat(response.getLastName()).isEqualTo("Smith");
                });
    }

    @Test
    public void whenUpdateEmployee_withValidData_thenReturnUpdatedEmployee() {
        // Create an employee first.
        EmployeeRequestModel createRequest = EmployeeRequestModel.builder()
                .firstName("Carol")
                .lastName("White")
                .email("carol.white@example.com")
                .phoneNumbers(Collections.emptyList())
                .streetAddress("321 Maple St")
                .city("Townsville")
                .province("Stateland")
                .country("USA")
                .postalCode("98765")
                .salary(new BigDecimal("70000"))
                .departmentId("1048b354-c18f-4109-8282-2a85485bfa5a")
                .positionTitle(PositionTitle.LIBRARIAN)
                .build();

        EmployeeResponseModel created = webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(EmployeeResponseModel.class)
                .returnResult()
                .getResponseBody();
        assertThat(created).isNotNull();
        String employeeId = created.getEmployeeId();

        // Prepare an update request (change lastName and salary for example).
        EmployeeRequestModel updateRequest = EmployeeRequestModel.builder()
                .firstName("Carol")
                .lastName("Black")
                .email("carol.white@example.com")
                .phoneNumbers(Collections.emptyList())
                .streetAddress("321 Maple St")
                .city("Townsville")
                .province("Stateland")
                .country("USA")
                .postalCode("98765")
                .salary(new BigDecimal("75000"))
                .departmentId("1048b354-c18f-4109-8282-2a85485bfa5a")
                .positionTitle(PositionTitle.LIBRARIAN)
                .build();

        webTestClient.put()
                .uri(BASE_URI + "/" + employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EmployeeResponseModel.class)
                .value(updated -> {
                    assertThat(updated.getEmployeeId()).isEqualTo(employeeId);
                    assertThat(updated.getLastName()).isEqualTo("Black");
                    assertThat(updated.getSalary()).isEqualByComparingTo(new BigDecimal("75000"));
                });
    }

    @Test
    public void whenDeleteEmployee_withValidId_thenReturnNoContent() {
        // First create an employee.
        EmployeeRequestModel createRequest = EmployeeRequestModel.builder()
                .firstName("Eve")
                .lastName("Black")
                .email("eve.black@example.com")
                .phoneNumbers(Collections.emptyList())
                .streetAddress("987 Cedar Ln")
                .city("Oldtown")
                .province("Oldstate")
                .country("USA")
                .postalCode("11223")
                .salary(new BigDecimal("65000"))
                .departmentId("1048b354-c18f-4109-8282-2a85485bfa5a")
                .positionTitle(PositionTitle.LIBRARIAN)
                .build();

        EmployeeResponseModel created = webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(EmployeeResponseModel.class)
                .returnResult()
                .getResponseBody();
        assertThat(created).isNotNull();
        String employeeId = created.getEmployeeId();

        // Delete the employee.
        webTestClient.delete()
                .uri(BASE_URI + "/" + employeeId)
                .exchange()
                .expectStatus().isNoContent();

        // Subsequent GET should return 404 (not found).
        webTestClient.get()
                .uri(BASE_URI + "/" + employeeId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void whenGetEmployeeById_withInvalidIdLength_thenReturnError() {
        String invalidEmployeeId = "12345";
        webTestClient.get()
                .uri(BASE_URI + "/" + invalidEmployeeId)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    public void whenCreateEmployee_withNullRequest_thenReturnServerError() {
        // Posting a null request should result in an error (adjust expected status if you have custom error handling).
        EmployeeRequestModel employeeRequest = new EmployeeRequestModel();
        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(employeeRequest)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    public void whenCreateEmployee_withMissingDepartmentId_thenReturnError() {
        // DepartmentId is required; a missing or empty departmentId should trigger an error.
        EmployeeRequestModel request = EmployeeRequestModel.builder()
                .firstName("Bob")
                .lastName("Jones")
                .email("bob.jones@example.com")
                .phoneNumbers(Collections.emptyList())
                .streetAddress("789 Oak Ave")
                .city("Sometown")
                .province("Somestate")
                .country("USA")
                .postalCode("34567")
                .salary(new BigDecimal("55000"))
                .departmentId("") // missing department id
                .positionTitle(PositionTitle.ARCHIVIST)
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    public void whenUpdateEmployee_withNonExistentId_thenReturnNotFound() {
        String nonExistentId = "11111111-1111-1111-1111-111111111111";
        EmployeeRequestModel updateRequest = EmployeeRequestModel.builder()
                .firstName("Donna")
                .lastName("Green")
                .email("donna.green@example.com")
                .phoneNumbers(Collections.emptyList())
                .streetAddress("654 Pine St")
                .city("Newcity")
                .province("Newstate")
                .country("USA")
                .postalCode("54321")
                .salary(new BigDecimal("80000"))
                .departmentId("2e5fb921-d16f-4dd9-b74b-8044cdb629c5")
                .positionTitle(PositionTitle.ARCHIVIST)
                .build();

        webTestClient.put()
                .uri(BASE_URI + "/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void whenDeleteEmployee_withNonExistentId_thenReturnNotFound() {
        String nonExistentId = "11111111-1111-1111-1111-111111111111";
        webTestClient.delete()
                .uri(BASE_URI + "/" + nonExistentId)
                .exchange()
                .expectStatus().isNotFound();
    }
}