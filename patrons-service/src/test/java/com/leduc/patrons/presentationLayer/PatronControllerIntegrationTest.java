package com.leduc.patrons.presentationLayer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql({"/data-h2.sql"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PatronControllerIntegrationTest {

    @Autowired
    private WebTestClient webClient;

    private final String BASE_URI_PATRONS = "/api/v1/patrons";

    @Test
    public void whenPatronsExist_thenReturnAllPatrons() {
        // Arrange: create a patron
        PatronRequestModel patronRequest = PatronRequestModel.builder()
                .firstName("John")
                .lastName("Doe")
                .email("mattys@example.com")
                .password("secret")
                .memberShipType("REGULAR")
                .phoneNumbers(Collections.emptyList())
                .streetAddress("123 Main St")
                .city("Anytown")
                .province("SomeState")
                .country("USA")
                .postalCode("12345")
                .build();

        // Create a patron so that the collection is not empty.
        webClient.post()
                .uri(BASE_URI_PATRONS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(patronRequest)
                .exchange()
                .expectStatus().isCreated();

        // Act: Get all patrons
        webClient.get()
                .uri(BASE_URI_PATRONS)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(PatronResponseModel.class)
                .value(patrons -> {
                    assertNotNull(patrons);
                    assertFalse(patrons.isEmpty(), "Expected at least one patron in the response");
                });
    }

    @Test
    public void whenPatronIdIsValid_thenReturnPatron() {
        // Arrange: Create a patron.
        PatronRequestModel patronRequest = PatronRequestModel.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .password("password")
                .memberShipType("REGULAR")
                .phoneNumbers(Collections.emptyList())
                .streetAddress("456 Elm St")
                .city("Othertown")
                .province("OtherState")
                .country("USA")
                .postalCode("67890")
                .build();

        PatronResponseModel createdPatron = webClient.post()
                .uri(BASE_URI_PATRONS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(patronRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PatronResponseModel.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(createdPatron);
        String patronId = createdPatron.getPatronId();
        assertNotNull(patronId);

        // Act: Retrieve the patron.
        webClient.get()
                .uri(BASE_URI_PATRONS + "/" + patronId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PatronResponseModel.class)
                .value(response -> {
                    assertEquals(patronId, response.getPatronId());
                    assertEquals("Jane", response.getFirstName());
                    assertEquals("Doe", response.getLastName());
                    assertEquals("jane.doe@example.com", response.getEmail());
                });
    }

    @Test
    public void whenPatronCreateRequestIsValid_thenReturnPatron() {
        PatronRequestModel patronRequest = PatronRequestModel.builder()
                .firstName("Alice")
                .lastName("Smith")
                .email("alice.smith@example.com")
                .password("password123")
                .memberShipType("PREMIUM")
                .phoneNumbers(Collections.emptyList())
                .streetAddress("789 Oak St")
                .city("Cityville")
                .province("Stateland")
                .country("USA")
                .postalCode("54321")
                .build();

        webClient.post()
                .uri(BASE_URI_PATRONS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(patronRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(PatronResponseModel.class)
                .value(response -> {
                    assertNotNull(response);
                    assertNotNull(response.getPatronId());
                    assertEquals("Alice", response.getFirstName());
                    assertEquals("Smith", response.getLastName());
                    assertEquals("alice.smith@example.com", response.getEmail());
                    assertEquals("PREMIUM", response.getMemberShipType());
                    assertEquals("789 Oak St", response.getStreetAddress());
                });
    }

    @Test
    public void whenPatronUpdateRequestIsValid_thenReturnUpdatedPatron() {
        // Arrange: Create a patron.
        PatronRequestModel createRequest = PatronRequestModel.builder()
                .firstName("Bob")
                .lastName("Marley")
                .email("bob.marley@example.com")
                .password("oneLove")
                .memberShipType("REGULAR")
                .phoneNumbers(Collections.emptyList())
                .streetAddress("101 Reggae Ave")
                .city("Kingston")
                .province("St. Andrew")
                .country("Jamaica")
                .postalCode("00000")
                .build();

        PatronResponseModel createdPatron = webClient.post()
                .uri(BASE_URI_PATRONS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PatronResponseModel.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(createdPatron);
        String patronId = createdPatron.getPatronId();

        // Prepare an update request.
        PatronRequestModel updateRequest = PatronRequestModel.builder()
                .firstName("Robert")  // updated first name
                .lastName(createdPatron.getLastName())  // unchanged
                .email(createdPatron.getEmail())          // unchanged
                .password(createdPatron.getPassword())    // unchanged
                .memberShipType("PREMIUM")                // updated membership type
                .phoneNumbers(createdPatron.getPhoneNumbers()) // unchanged
                .streetAddress("202 Updated Ave")         // updated address
                .city(createdPatron.getCity())            // unchanged
                .province(createdPatron.getProvince())    // unchanged
                .country(createdPatron.getCountry())      // unchanged
                .postalCode(createdPatron.getPostalCode())// unchanged
                .build();

        webClient.put()
                .uri(BASE_URI_PATRONS + "/" + patronId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(PatronResponseModel.class)
                .value(updatedPatron -> {
                    assertNotNull(updatedPatron);
                    assertEquals(patronId, updatedPatron.getPatronId());
                    assertEquals("Robert", updatedPatron.getFirstName());
                    assertEquals("PREMIUM", updatedPatron.getMemberShipType());
                    assertEquals("202 Updated Ave", updatedPatron.getStreetAddress());
                });
    }

    @Test
    public void whenPatronExistsOnDelete_thenReturnNoFound() {
        // Arrange: Create a patron.
        PatronRequestModel createRequest = PatronRequestModel.builder()
                .firstName("Charlie")
                .lastName("Brown")
                .email("charlie.brown@example.com")
                .password("snoopy")
                .memberShipType("REGULAR")
                .phoneNumbers(Collections.emptyList())
                .streetAddress("303 Peanuts St")
                .city("Minneapolis")
                .province("MN")
                .country("USA")
                .postalCode("55401")
                .build();

        PatronResponseModel createdPatron = webClient.post()
                .uri(BASE_URI_PATRONS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PatronResponseModel.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(createdPatron);
        String patronId = createdPatron.getPatronId();

        // Act: Delete the patron.
        webClient.delete()
                .uri(BASE_URI_PATRONS + "/" + patronId)
                .exchange()
                .expectStatus().isNoContent();

        // Assert: Subsequent GET should return NotFound.
        webClient.get()
                .uri(BASE_URI_PATRONS + "/" + patronId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void whenPatronCreateRequest_missingEmail_thenReturnError() {
        PatronRequestModel requestMissingEmail = PatronRequestModel.builder()
                .firstName("David")
                .lastName("Gilmour")
                .email(null)   // missing email
                .password("shineOn")
                .memberShipType("REGULAR")
                .phoneNumbers(Collections.emptyList())
                .streetAddress("404 Pink Floyd Ave")
                .city("London")
                .province("UK")
                .country("UK")
                .postalCode("SW1A")
                .build();

        webClient.post()
                .uri(BASE_URI_PATRONS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(requestMissingEmail)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    public void whenPatronUpdateRequest_withInvalidPatronIdLength_thenReturnUnprocessableEntity() {
        String invalidPatronId = "12345"; // wrong length
        PatronRequestModel updateRequest = PatronRequestModel.builder()
                .firstName("Updated")
                .build();
        webClient.put()
                .uri(BASE_URI_PATRONS + "/" + invalidPatronId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void whenPatronUpdateRequest_withNonExistentPatron_thenReturnNotFound() {
        String nonExistentPatronId = "11111111-1111-1111-1111-111111111111";
        PatronRequestModel updateRequest = PatronRequestModel.builder()
                .firstName("Updated")
                .build();
        webClient.put()
                .uri(BASE_URI_PATRONS + "/" + nonExistentPatronId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void whenPatronDelete_withNonExistentPatron_thenReturnNotFound() {
        String nonExistentPatronId = "11111111-1111-1111-1111-111111111111";
        webClient.delete()
                .uri(BASE_URI_PATRONS + "/" + nonExistentPatronId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void whenCreatingDuplicatePatron_thenReturnUnprocessableEntity() {
        // Arrange: Prepare a valid patron request.
        PatronRequestModel request = PatronRequestModel.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password123")
                .memberShipType("REGULAR")
                .streetAddress("123 Main St")
                .city("Anytown")
                .province("Anystate")
                .country("USA")
                .postalCode("12345")
                .phoneNumbers(Collections.emptyList())
                .build();

        // Act & Assert: Create a duplicate and expect a conflict (HTTP 409).
        webClient.post()
                .uri(BASE_URI_PATRONS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void whenCreatingDuplicatePatronByEmail_thenReturnUnprocessableEntity() {
        // Arrange: Build a patron creation request.
        PatronRequestModel request = PatronRequestModel.builder()
                .firstName("John")
                .lastName("Doe")
                .email("duplicate@example.com")
                .password("password123")
                .memberShipType("REGULAR")
                .streetAddress("123 Main St")
                .city("Anytown")
                .province("SomeState")
                .country("USA")
                .postalCode("12345")
                .phoneNumbers(Collections.emptyList())
                .build();

        // First creation.
        PatronResponseModel firstCreated = webClient.post()
                .uri(BASE_URI_PATRONS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PatronResponseModel.class)
                .returnResult()
                .getResponseBody();
        assertNotNull(firstCreated);

        // Act & Assert: Attempt to create a second patron with the same email.
        webClient.post()
                .uri(BASE_URI_PATRONS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void whenGetPatronById_withInvalidIdLength_thenReturnNotFound() {
        String invalidPatronId = "12345"; // not 36 characters long
        webClient.get()
                .uri(BASE_URI_PATRONS + "/" + invalidPatronId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void whenGetPatronById_withNonExistentId_thenReturnNotFound() {
        String nonExistentId = "11111111-1111-1111-1111-111111111111";
        webClient.get()
                .uri(BASE_URI_PATRONS + "/" + nonExistentId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
}