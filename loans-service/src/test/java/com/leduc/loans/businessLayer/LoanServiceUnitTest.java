package com.leduc.loans.businessLayer;

import com.leduc.loans.dataAccessLayer.Loan;
import com.leduc.loans.dataAccessLayer.LoanIdentifier;
import com.leduc.loans.dataAccessLayer.LoanRepository;
import com.leduc.loans.dataAccessLayer.LoanStatus;
import com.leduc.loans.domainclientLayer.books.BookModel;
import com.leduc.loans.domainclientLayer.books.BooksServiceClient;
import com.leduc.loans.domainclientLayer.patrons.PatronModel;
import com.leduc.loans.domainclientLayer.patrons.PatronsServiceClient;
import com.leduc.loans.domainclientLayer.staff.EmployeeModel;
import com.leduc.loans.domainclientLayer.staff.EmployeesServiceClient;
import com.leduc.loans.mappingLayer.LoanRequestMapper;
import com.leduc.loans.mappingLayer.LoanResponseMapper;
import com.leduc.loans.presentationLayer.LoanRequestModel;
import com.leduc.loans.presentationLayer.LoanResponseModel;
import com.leduc.loans.utils.exceptions.InvalidInputException;
import com.leduc.loans.utils.exceptions.NotFoundException;
import com.leduc.loans.utils.exceptions.TooManyLoansException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration")
class LoanServiceUnitTest {

    @Autowired
    private LoanServiceImpl loanService;

    @MockitoBean
    private LoanRepository loanRepository;

    @MockitoBean
    private PatronsServiceClient patronsServiceClient;

    @MockitoBean
    private EmployeesServiceClient employeesServiceClient;

    @MockitoBean
    private BooksServiceClient booksServiceClient;

    @MockitoSpyBean
    private LoanRequestMapper loanRequestMapper;

    @MockitoSpyBean
    private LoanResponseMapper loanResponseMapper;

    private final String VALID_PATRON = "11111111-1111-1111-1111-111111111111";
    private final String VALID_LOAN   = "22222222-2222-2222-2222-222222222222";

    @Test
    void whenGetAllLoans_withUnknownPatron_thenInvalidInput() {
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON)).thenReturn(null);
        assertThrows(InvalidInputException.class, () -> loanService.getAllLoans(VALID_PATRON));
    }

    @Test
    void whenGetAllLoans_withMixedLoans_returnsOnlyMatching() {
        // Arrange
        PatronModel patron = PatronModel.builder()
                .patronId(VALID_PATRON)
                .firstName("John")
                .lastName("Doe")
                .build();
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON))
                .thenReturn(patron);

        Loan loanForMe = new Loan();
        loanForMe.setLoanIdentifier(new LoanIdentifier("L-1"));
        loanForMe.setPatronModel(patron);

        PatronModel other = PatronModel.builder().patronId("other").build();
        Loan loanForOther = new Loan();
        loanForOther.setLoanIdentifier(new LoanIdentifier("L-2"));
        loanForOther.setPatronModel(other);

        when(loanRepository.findAll())
                .thenReturn(List.of(loanForMe, loanForOther));

        LoanResponseModel dtoMe = new LoanResponseModel();
        dtoMe.setLoanId("L-1");
        dtoMe.setPatronId(VALID_PATRON);
        LoanResponseModel dtoOther = new LoanResponseModel();
        dtoOther.setLoanId("L-2");
        dtoOther.setPatronId("other");

        when(loanResponseMapper.toResponse(loanForMe)).thenReturn(dtoMe);
        when(loanResponseMapper.toResponse(loanForOther)).thenReturn(dtoOther);

        List<LoanResponseModel> results = loanService.getAllLoans(VALID_PATRON);

        assertEquals(1, results.size());
        assertEquals(dtoMe, results.get(0));
    }

    @Test
    void whenGetAllLoans_withNoLoans_thenReturnEmpty() {
        var patron = PatronModel.builder()
                .patronId(VALID_PATRON)
                .firstName("John")
                .lastName("Doe")
                .build();
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON)).thenReturn(patron);
        when(loanRepository.findAll()).thenReturn(Collections.emptyList());

        List<LoanResponseModel> list = loanService.getAllLoans(VALID_PATRON);

        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void whenGetLoanById_notFound_thenNotFoundException() {
        var patron = PatronModel.builder()
                .patronId(VALID_PATRON)
                .firstName("John")
                .lastName("Doe")
                .build();
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON)).thenReturn(patron);
        when(loanRepository.getLoanByLoanIdentifier_LoanId(VALID_LOAN)).thenReturn(null);

        assertThrows(NotFoundException.class, () ->
                loanService.getLoanById(VALID_PATRON, VALID_LOAN));
    }


    //////// Test for Custom Exception //////////
    @Test
    void whenAddLoan_moreThanThree_thenTooManyLoans() {
        var patron = PatronModel.builder()
                .patronId(VALID_PATRON)
                .firstName("John")
                .lastName("Doe")
                .build();
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON)).thenReturn(patron);
        when(loanRepository.countByPatronModel_PatronId(VALID_PATRON)).thenReturn(3);

        LoanRequestModel req = new LoanRequestModel();
        req.setPatronId(VALID_PATRON);

        assertThrows(TooManyLoansException.class, () ->
                loanService.addLoan(req, VALID_PATRON));
    }

    //////////////////////////////////////////////

    @Test
    void whenDeleteLoan_notFound_thenInvalidInput() {
        var patron = PatronModel.builder()
                .patronId(VALID_PATRON)
                .firstName("John")
                .lastName("Doe")
                .build();
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON)).thenReturn(patron);
        when(loanRepository
                .findLoanByPatronModel_PatronIdAndLoanIdentifier_LoanId(VALID_PATRON, VALID_LOAN))
                .thenReturn(null);

        assertThrows(InvalidInputException.class, () ->
                loanService.deleteLoan(VALID_PATRON, VALID_LOAN));
    }

    @Test
    void whenGetLoanById_wrongPatron_thenInvalidInput() {
        var patron = PatronModel.builder()
                .patronId(VALID_PATRON)
                .firstName("John").lastName("Doe")
                .build();
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON))
                .thenReturn(patron);

        Loan existing = new Loan();
        existing.setLoanIdentifier(new LoanIdentifier(VALID_LOAN));
        existing.setPatronModel(
                PatronModel.builder().patronId("other-patron").build()
        );
        when(loanRepository.getLoanByLoanIdentifier_LoanId(VALID_LOAN))
                .thenReturn(existing);
        when(loanRepository
                .findLoanByPatronModel_PatronIdAndLoanIdentifier_LoanId(VALID_PATRON, VALID_LOAN))
                .thenReturn(null);

        assertThrows(NullPointerException.class, () ->
                loanService.getLoanById(VALID_PATRON, VALID_LOAN));
    }

    @Test
    void whenAddLoan_bookNotFound_thenNotFound() {
        var patron = PatronModel.builder()
                .patronId(VALID_PATRON)
                .firstName("John").lastName("Doe")
                .build();
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON))
                .thenReturn(patron);
        when(loanRepository.countByPatronModel_PatronId(VALID_PATRON))
                .thenReturn(0);

        when(booksServiceClient.getBookByBookId("B1")).thenReturn(null);

        LoanRequestModel req = new LoanRequestModel();
        req.setPatronId(VALID_PATRON);
        req.setBookId("B1");
        req.setEmployeeId("E1");
        req.setIssueDate(LocalDate.now());
        req.setCheckoutDate(LocalDate.now());
        req.setReturnDate(LocalDate.now().plusDays(7));
        req.setStatus(LoanStatus.CHECKED_OUT);

        assertThrows(InvalidInputException.class, () ->
                loanService.addLoan(req, VALID_PATRON));
    }

    @Test
    void whenAddLoan_employeeNotFound_thenNotFound() {
        var patron = PatronModel.builder()
                .patronId(VALID_PATRON)
                .firstName("John").lastName("Doe")
                .build();
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON))
                .thenReturn(patron);
        when(loanRepository.countByPatronModel_PatronId(VALID_PATRON))
                .thenReturn(0);

        when(booksServiceClient.getBookByBookId("B1"))
                .thenReturn(BookModel.builder().bookId("B1").build());
        when(employeesServiceClient.getEmployeeByEmployeeId("E1"))
                .thenReturn(null);

        LoanRequestModel req = new LoanRequestModel();
        req.setPatronId(VALID_PATRON);
        req.setBookId("B1");
        req.setEmployeeId("E1");
        req.setIssueDate(LocalDate.now());
        req.setCheckoutDate(LocalDate.now());
        req.setReturnDate(LocalDate.now().plusDays(7));
        req.setStatus(LoanStatus.CHECKED_OUT);

        assertThrows(InvalidInputException.class, () ->
                loanService.addLoan(req, VALID_PATRON));
    }

    @Test
    void whenUpdateLoan_invalidIdLength_thenInvalidInput() {
        LoanRequestModel req = new LoanRequestModel();
        assertThrows(InvalidInputException.class, () ->
                loanService.updateLoan(req, VALID_PATRON, "short-id"));
    }

    @Test
    void whenUpdateLoan_notFound_thenNotFound() {
        var patron = PatronModel.builder()
                .patronId(VALID_PATRON)
                .firstName("John").lastName("Doe")
                .build();
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON))
                .thenReturn(patron);

        LoanRequestModel req = new LoanRequestModel();
        assertThrows(NotFoundException.class, () ->
                loanService.updateLoan(req, VALID_PATRON, VALID_LOAN));
    }

    @Test
    void whenUpdateLoan_wrongPatron_thenInvalidInput() {
        var patron = PatronModel.builder()
                .patronId(VALID_PATRON)
                .firstName("John").lastName("Doe")
                .build();
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON))
                .thenReturn(patron);

        Loan existing = new Loan();
        existing.setLoanIdentifier(new LoanIdentifier(VALID_LOAN));
        existing.setPatronModel(
                PatronModel.builder().patronId("other").build()
        );
        when(loanRepository
                .findLoanByPatronModel_PatronIdAndLoanIdentifier_LoanId(VALID_PATRON, VALID_LOAN))
                .thenReturn(existing);

        LoanRequestModel req = new LoanRequestModel();
        assertThrows(InvalidInputException.class, () ->
                loanService.updateLoan(req, VALID_PATRON, VALID_LOAN));
    }

    @Test
    void whenUpdateLoan_bookNotFound_thenNotFound() {
        var patron = PatronModel.builder()
                .patronId(VALID_PATRON)
                .firstName("John").lastName("Doe")
                .build();
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON))
                .thenReturn(patron);

        Loan existing = new Loan();
        existing.setLoanIdentifier(new LoanIdentifier(VALID_LOAN));
        existing.setPatronModel(patron);
        when(loanRepository
                .findLoanByPatronModel_PatronIdAndLoanIdentifier_LoanId(VALID_PATRON, VALID_LOAN))
                .thenReturn(existing);

        when(booksServiceClient.getBookByBookId("B1")).thenReturn(null);

        LoanRequestModel req = new LoanRequestModel();
        req.setBookId("B1");
        assertThrows(InvalidInputException.class, () ->
                loanService.updateLoan(req, VALID_PATRON, VALID_LOAN));
    }

    @Test
    void whenUpdateLoan_employeeNotFound_thenNotFound() {
        var patron = PatronModel.builder()
                .patronId(VALID_PATRON)
                .firstName("John").lastName("Doe")
                .build();
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON))
                .thenReturn(patron);

        Loan existing = new Loan();
        existing.setLoanIdentifier(new LoanIdentifier(VALID_LOAN));
        existing.setPatronModel(patron);
        when(loanRepository
                .findLoanByPatronModel_PatronIdAndLoanIdentifier_LoanId(VALID_PATRON, VALID_LOAN))
                .thenReturn(existing);

        when(booksServiceClient.getBookByBookId("B1"))
                .thenReturn(BookModel.builder().bookId("B1").build());

        when(employeesServiceClient.getEmployeeByEmployeeId("E1"))
                .thenReturn(null);

        LoanRequestModel req = new LoanRequestModel();
        req.setBookId("B1");
        req.setEmployeeId("E1");

        assertThrows(InvalidInputException.class, () ->
                loanService.updateLoan(req, VALID_PATRON, VALID_LOAN));
    }

    @Test
    void whenGetAllLoans_patronNotFound_thenInvalidInput() {
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON))
                .thenReturn(null);

        assertThrows(InvalidInputException.class, () ->
                loanService.getAllLoans(VALID_PATRON));
    }

    @Test
    void whenGetLoanById_patronNotFound_thenInvalidInput() {
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON))
                .thenReturn(null);

        assertThrows(InvalidInputException.class, () ->
                loanService.getLoanById(VALID_PATRON, VALID_LOAN));
    }

    @Test
    void whenAddLoan_initialPatronNotFound_thenNotFound() {
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON))
                .thenReturn(null);

        LoanRequestModel req = new LoanRequestModel();
        req.setPatronId(VALID_PATRON);

        assertThrows(NotFoundException.class, () ->
                loanService.addLoan(req, VALID_PATRON));
    }

    @Test
    void whenAddLoan_requestPatronNotFound_thenInvalidInput() {
        PatronModel found = PatronModel.builder().patronId(VALID_PATRON).build();
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON))
                .thenReturn(found);
        when(loanRepository.countByPatronModel_PatronId(VALID_PATRON))
                .thenReturn(0);

        LoanRequestModel req = new LoanRequestModel();
        req.setPatronId("other-patron-id");

        when(patronsServiceClient.getPatronByPatronId("other-patron-id"))
                .thenReturn(null);

        assertThrows(InvalidInputException.class, () ->
                loanService.addLoan(req, VALID_PATRON));
    }

    @Test
    void whenAddLoan_bookLookupFails_thenInvalidInput() {
        PatronModel found = PatronModel.builder().patronId(VALID_PATRON).build();
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON))
                .thenReturn(found);
        when(loanRepository.countByPatronModel_PatronId(VALID_PATRON))
                .thenReturn(0);
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON))
                .thenReturn(found);

        LoanRequestModel req = new LoanRequestModel();
        req.setPatronId(VALID_PATRON);
        req.setBookId("missing-book");

        when(booksServiceClient.getBookByBookId("missing-book"))
                .thenReturn(null);

        assertThrows(InvalidInputException.class, () ->
                loanService.addLoan(req, VALID_PATRON));
    }

    @Test
    void whenAddLoan_employeeLookupFails_thenInvalidInput() {
        PatronModel found = PatronModel.builder().patronId(VALID_PATRON).build();
        BookModel   book  = BookModel.builder().bookId("book-xyz").build();

        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON))
                .thenReturn(found);
        when(loanRepository.countByPatronModel_PatronId(VALID_PATRON))
                .thenReturn(0);
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON))
                .thenReturn(found);
        when(booksServiceClient.getBookByBookId("book-xyz"))
                .thenReturn(book);

        LoanRequestModel req = new LoanRequestModel();
        req.setPatronId(VALID_PATRON);
        req.setBookId("book-xyz");
        req.setEmployeeId("missing-emp");

        when(employeesServiceClient.getEmployeeByEmployeeId("missing-emp"))
                .thenReturn(null);

        assertThrows(InvalidInputException.class, () ->
                loanService.addLoan(req, VALID_PATRON));
    }

    @Test
    void whenGetLoanById_patronNotFound_throwsInvalidInput() {
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON)).thenReturn(null);

        assertThrows(
                InvalidInputException.class,
                () -> loanService.getLoanById(VALID_PATRON, VALID_LOAN),
                "Unknown patron id: " + VALID_PATRON
        );
    }

    @Test
    void whenGetLoanById_wrongPatron_throwsInvalidInput() {
        PatronModel patron = PatronModel.builder().patronId(VALID_PATRON).build();
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON)).thenReturn(patron);

        when(loanRepository.getLoanByLoanIdentifier_LoanId(VALID_LOAN))
                .thenReturn(new Loan());

        Loan loan = new Loan();
        loan.setPatronModel(PatronModel.builder().patronId("other").build());
        when(loanRepository.findLoanByPatronModel_PatronIdAndLoanIdentifier_LoanId(
                VALID_PATRON, VALID_LOAN))
                .thenReturn(loan);

        LoanResponseModel dto = new LoanResponseModel();
        dto.setPatronId("other");
        when(loanResponseMapper.toResponse(loan)).thenReturn(dto);

        assertThrows(
                InvalidInputException.class,
                () -> loanService.getLoanById(VALID_PATRON, VALID_LOAN),
                "Loan " + VALID_LOAN + " does not belong to patron " + VALID_PATRON
        );
    }

    @Test
    void whenDeleteLoan_patronNotFound_throwsInvalidInput() {
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON)).thenReturn(null);

        assertThrows(
                InvalidInputException.class,
                () -> loanService.deleteLoan(VALID_PATRON, VALID_LOAN),
                "Unknown patron id: " + VALID_PATRON
        );
    }

    @Test
    void whenDeleteLoan_existingNotFound_throwsInvalidInput() {
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON))
                .thenReturn(PatronModel.builder().patronId(VALID_PATRON).build());
        when(loanRepository.findLoanByPatronModel_PatronIdAndLoanIdentifier_LoanId(
                VALID_PATRON, VALID_LOAN))
                .thenReturn(null);

        assertThrows(
                InvalidInputException.class,
                () -> loanService.deleteLoan(VALID_PATRON, VALID_LOAN),
                "Loan not found: " + VALID_LOAN
        );
    }

    @Test
    void whenDeleteLoan_identifierNotFound_throwsNotFound() {
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON))
                .thenReturn(PatronModel.builder().patronId(VALID_PATRON).build());

        Loan loaded = new Loan();
        when(loanRepository.findLoanByPatronModel_PatronIdAndLoanIdentifier_LoanId(
                VALID_PATRON, VALID_LOAN))
                .thenReturn(loaded);

        when(loanRepository.getLoanByLoanIdentifier_LoanId(VALID_LOAN))
                .thenReturn(null);

        assertThrows(
                NotFoundException.class,
                () -> loanService.deleteLoan(VALID_PATRON, VALID_LOAN),
                "Loan not found with id: " + VALID_LOAN
        );
    }

}
