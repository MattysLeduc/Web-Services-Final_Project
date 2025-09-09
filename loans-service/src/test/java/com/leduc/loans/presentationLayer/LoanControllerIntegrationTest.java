package com.leduc.loans.presentationLayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leduc.loans.businessLayer.LoanService;
import com.leduc.loans.dataAccessLayer.Loan;
import com.leduc.loans.dataAccessLayer.LoanIdentifier;
import com.leduc.loans.dataAccessLayer.LoanRepository;
import com.leduc.loans.dataAccessLayer.LoanStatus;
import com.leduc.loans.domainclientLayer.books.BookModel;
import com.leduc.loans.domainclientLayer.patrons.PatronModel;
import com.leduc.loans.domainclientLayer.patrons.PatronsServiceClient;
import com.leduc.loans.domainclientLayer.staff.EmployeeModel;
import com.leduc.loans.utils.exceptions.TooManyLoansException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LoanControllerIntegrationTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoanRepository loanRepository;



    private MockRestServiceServer mockServer;
    private ObjectMapper mapper = new ObjectMapper();

    private final String BASE_URI = "/api/v1/patrons";

    private final String BASE_URI_BOOKS = "http://localhost:7001/api/v1/books";

    private final String BASE_URI_PATRONS = "http://localhost:7002/api/v1/patrons";

    private final String BASE_URI_STAFF = "http://localhost:7003/api/v1/staff";

    private final String VALID_PATRON = "11111111-1111-1111-1111-111111111111";
    private final String NOT_FOUND_PATRON = "22222222-2222-2222-2222-222222222222";
    private final String VALID_BOOK   = "33333333-3333-3333-3333-333333333333";
    private final String VALID_EMP    = "44444444-4444-4444-4444-444444444444";

    @BeforeEach
    void init() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        loanRepository.deleteAll();
        mockServer = MockRestServiceServer
                .bindTo(restTemplate)
                .ignoreExpectOrder(true)
                .build();
    }

    private void mockPatronLookup(String patronId) throws Exception {
        PatronModel p = PatronModel.builder()
                .patronId(patronId)
                .firstName("John").lastName("Doe").build();
        mockServer.expect(requestTo(new URI("http://localhost:7002/api/v1/patrons/" + patronId)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(p), MediaType.APPLICATION_JSON));
    }

    private void mockBookLookup(String bookId) throws Exception {
        BookModel b = BookModel.builder()
                .bookId(bookId)
                .isbn("978")
                .title("Sample")
                .authorFirstName("A")
                .authorLastName("B")
                .bookType("EBOOK")
                .genre("FICTION")
                .build();
        mockServer.expect(requestTo(new URI("http://localhost:7001/api/v1/books/" + bookId)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(b), MediaType.APPLICATION_JSON));
    }

    private void mockEmployeeLookup(String empId) throws Exception {
        EmployeeModel e = EmployeeModel.builder()
                .employeeId(empId)
                .firstName("Jane")
                .lastName("Smith")
                .build();
        mockServer.expect(requestTo(new URI("http://localhost:7003/api/v1/staff/" + empId)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(e), MediaType.APPLICATION_JSON));
    }

    @Test
    void whenGetAllLoans_thenReturnsOk() throws Exception {
        PatronModel patron = PatronModel.builder()
                .patronId(VALID_PATRON)
                .firstName("John")
                .lastName("Doe")
                .build();

        mockServer.expect(requestTo(new URI("http://localhost:7002/api/v1/patrons/" + VALID_PATRON)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(patron), MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(new URI("http://localhost:7002/api/v1/patrons/" + VALID_PATRON)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(patron), MediaType.APPLICATION_JSON));

        webClient.get()
                .uri(BASE_URI + "/" + VALID_PATRON + "/loans")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(LoanResponseModel.class)
                .hasSize(0);
    }

    @Test
    void whenPatronNotFound_thenReturnsNotFound() throws Exception {
        mockServer.expect(requestTo(new URI("http://localhost:7002/api/v1/patrons/" + NOT_FOUND_PATRON)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.get()
                .uri(BASE_URI + "/" + NOT_FOUND_PATRON + "/loans")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void postLoan_thenCreatedAndRetrievable() throws JsonProcessingException, URISyntaxException {
        var bookModel = BookModel.builder()
                .bookId("6fa459ea-ee8a-3ca4-894e-d6f1d55b4f2a")
                .isbn("978-0-7432-7356-5")
                .title("A Brief History of Time")
                .authorFirstName("Stephen")
                .authorLastName("Hawking")
                .bookType("PAPERBACK")
                .genre("SCIENCE")
                .build();

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(BASE_URI_BOOKS + "/" + bookModel.getBookId())))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(bookModel)));

        var patronModel = PatronModel.builder()
                .patronId("123e4567-e89b-12d3-a456-426614174000")
                .firstName("John")
                .lastName("Doe")
                .build();

        mockServer.expect(ExpectedCount.twice(),
                        requestTo(new URI(BASE_URI_PATRONS + "/" + patronModel.getPatronId())))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(patronModel)));

        var employeeModel = EmployeeModel.builder()
                .employeeId("e8a17e76-1c9f-4a6a-9342-488b7e99f0f7")
                .firstName("Vilma")
                .lastName("Chawner")
                .build();

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_STAFF + "/" + employeeModel.getEmployeeId())))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(employeeModel)));

        LoanRequestModel loanRequestModel = createLoanRequestModel();

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_BOOKS + "/" + bookModel.getBookId()
                                + "/copiesAvailable?status=" + loanRequestModel.getStatus().name())))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withStatus(HttpStatus.OK));

        webClient.post()
                .uri(BASE_URI + "/" + patronModel.getPatronId() + "/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loanRequestModel)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LoanResponseModel.class)
                .value((response) -> {
                    assertNotNull(response);
                    assertNotNull(response.getLoanId());
                    assertEquals(patronModel.getPatronId(), response.getPatronId());
                    assertEquals(patronModel.getFirstName(), response.getPatronFirstName());
                    assertEquals(patronModel.getLastName(), response.getPatronLastName());
                    assertEquals(employeeModel.getEmployeeId(), response.getEmployeeId());
                    assertEquals(employeeModel.getFirstName(), response.getEmployeeFirstName());
                    assertEquals(employeeModel.getLastName(), response.getEmployeeLastName());
                    assertEquals(bookModel.getBookId(), response.getBookId());
                    assertEquals(bookModel.getIsbn(), response.getIsbn());
                    assertEquals(bookModel.getTitle(), response.getTitle());
                    assertEquals(bookModel.getAuthorFirstName(), response.getAuthorFirstName());
                    assertEquals(bookModel.getAuthorLastName(), response.getAuthorLastName());
                    assertEquals(bookModel.getGenre(), response.getGenre());
                    assertEquals(bookModel.getBookType(), response.getBookType());
                    assertEquals(loanRequestModel.getIssueDate(), response.getIssueDate());
                    assertEquals(loanRequestModel.getCheckoutDate(), response.getCheckoutDate());
                    assertEquals(loanRequestModel.getReturnDate(), response.getReturnDate());
                    assertEquals(loanRequestModel.getStatus(), response.getStatus());
                });
    }

    @Test
    void putLoan_thenUpdatedFields() throws Exception {
        var loanIdentifier1 = new LoanIdentifier("2d8d1a47-08d8-4598-8b9d-6b2ec67dee1d");
        var bookModel = BookModel.builder()
                .bookId("6fa459ea-ee8a-3ca4-894e-d6f1d55b4f2a")
                .isbn("978-0-7432-7356-5")
                .title("A Brief History of Time")
                .authorFirstName("Stephen")
                .authorLastName("Hawking")
                .bookType("PAPERBACK")
                .genre("SCIENCE")
                .build();

        var patronModel = PatronModel.builder()
                .patronId("123e4567-e89b-12d3-a456-426614174000")
                .firstName("John")
                .lastName("Doe")
                .build();

        var employeeModel = EmployeeModel.builder()
                .employeeId("e8a17e76-1c9f-4a6a-9342-488b7e99f0f7")
                .firstName("Vilma")
                .lastName("Chawner")
                .build();

        Loan existing = Loan.builder()
                .loanIdentifier(loanIdentifier1)
                .patronModel(patronModel)
                .bookModel(bookModel)
                .employeeModel(employeeModel)
                .issueDate(LocalDate.of(2025, 5, 11))
                .checkoutDate(LocalDate.of(2025, 5, 11))
                .returnDate(LocalDate.of(2025, 5, 14))
                .status(LoanStatus.CHECKED_OUT)
                .build();
        loanRepository.save(existing);

        mockPatronLookup(  patronModel.getPatronId());
        mockBookLookup(    bookModel.getBookId());
        mockEmployeeLookup(employeeModel.getEmployeeId());

        LoanRequestModel update = LoanRequestModel.builder()
                .patronId(   patronModel.getPatronId())
                .bookId(     bookModel.getBookId())
                .employeeId( employeeModel.getEmployeeId())
                .issueDate(      existing.getIssueDate())
                .checkoutDate(   existing.getCheckoutDate())
                .returnDate(     LocalDate.of(2025, 5, 15))  // changed
                .status(         LoanStatus.RETURNED)        // changed
                .build();

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_BOOKS + "/" + bookModel.getBookId()
                                + "/copiesAvailable?status=" + update.getStatus().name())))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withStatus(HttpStatus.OK));

        webClient.put()
                .uri("/api/v1/patrons/{patronId}/loans/{loanId}",
                        patronModel.getPatronId(),
                        loanIdentifier1.getLoanId())
                .bodyValue(update)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoanResponseModel.class)
                .value(response -> {
                    assertEquals(update.getReturnDate(), response.getReturnDate());
                    assertEquals(update.getStatus(),     response.getStatus());
                });
    }

    @Test
    void deleteLoan_thenNoContentAndGone() throws Exception {
        mockPatronLookup(VALID_PATRON);
        Loan loan = new Loan();
        String loanId = UUID.randomUUID().toString();
        loan.setLoanIdentifier(new LoanIdentifier(loanId));
        loan.setPatronModel(PatronModel.builder().patronId(VALID_PATRON).build());
        loanRepository.save(loan);

        mockPatronLookup(VALID_PATRON);
        webClient.delete()
                .uri(BASE_URI + "/" + VALID_PATRON + "/loans/" + loanId)
                .exchange()
                .expectStatus().isNoContent();

        assertThat(loanRepository.getLoanByLoanIdentifier_LoanId(loan.getLoanIdentifier().getLoanId()))
                .isNull();
    }



    private LoanRequestModel createLoanRequestModel(){
        var loanRequestModel = LoanRequestModel.builder()
                .patronId("123e4567-e89b-12d3-a456-426614174000")
                .bookId("6fa459ea-ee8a-3ca4-894e-d6f1d55b4f2a")
                .employeeId("e8a17e76-1c9f-4a6a-9342-488b7e99f0f7")
                .issueDate(LocalDate.of(2025, 4, 10))
                .returnDate(LocalDate.of(2025, 4, 17))
                .checkoutDate(LocalDate.of(2025, 4, 10))
                .status(LoanStatus.CHECKED_OUT)
                .build();
        return loanRequestModel;
    }


}

