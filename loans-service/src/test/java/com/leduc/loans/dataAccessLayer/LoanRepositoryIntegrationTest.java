package com.leduc.loans.dataAccessLayer;

import com.leduc.loans.domainclientLayer.books.BookModel;
import com.leduc.loans.domainclientLayer.patrons.PatronModel;
import com.leduc.loans.domainclientLayer.staff.EmployeeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class LoanRepositoryIntegrationTest {

    @Autowired
    private LoanRepository loanRepository;

    @BeforeEach
    void setup() {
        loanRepository.deleteAll();
    }

    @Test
    void whenSaveAndFindByPatronId_thenReturnLoans() {

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

        Loan loan = new Loan();
        loan.setLoanIdentifier(new LoanIdentifier("33333333-3333-3333-3333-333333333333"));
        loan.setPatronModel(patronModel);
        loan.setBookModel(bookModel);
        loan.setEmployeeModel(employeeModel);
        loan.setIssueDate(LocalDate.now());
        loan.setCheckoutDate(LocalDate.now());
        loan.setReturnDate(LocalDate.now().plusDays(7));
        loan.setStatus(LoanStatus.CHECKED_OUT);
        loanRepository.save(loan);

        List<Loan> loans = loanRepository.findAllByPatronModel_PatronId(patronModel.getPatronId());
        assertNotNull(loans);
        assertEquals(1, loans.size());
    }

    @Test
    void whenNoLoansForPatron_thenReturnEmpty() {
        List<Loan> loans = loanRepository.findAllByPatronModel_PatronId("no-such");
        assertNotNull(loans);
        assertTrue(loans.isEmpty());
    }
}