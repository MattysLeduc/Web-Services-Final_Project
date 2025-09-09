package com.leduc.apigateway.loans.businessLayer;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import com.leduc.apigateway.books.domainclientLayer.BooksServiceClient;
import com.leduc.apigateway.loans.domainclientLayer.LoansServiceClient;
import com.leduc.apigateway.loans.presentationLayer.LoanRequestModel;
import com.leduc.apigateway.loans.presentationLayer.LoanResponseModel;
import com.leduc.apigateway.utils.exceptions.InvalidInputException;
import com.leduc.apigateway.utils.exceptions.NotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;


import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration")
class LoansServiceUnitTest {

    @Mock private LoansServiceClient loansServiceClient;
    @Mock private BooksServiceClient booksServiceClient;
    @InjectMocks private LoansServiceImpl service;

    @Test
    void getAllLoans_returnsListWithHateoasLinks() {
        String patronId = UUID.randomUUID().toString();
        LoanResponseModel l = new LoanResponseModel();
        l.setPatronId(patronId);
        l.setLoanId("11111111-1111-1111-1111-111111111111");
        when(loansServiceClient.getAllLoans(patronId))
                .thenReturn(Arrays.asList(l));

        List<LoanResponseModel> result = service.getAllLoans(patronId);

        assertEquals(1, result.size());
        LoanResponseModel r = result.get(0);
        assertTrue(r.getLink("self").isPresent());
        assertTrue(r.getLink("all-loans").isPresent());
    }

    @Test
    void getLoanById_withInvalidLoanId_throwsInvalidInputException() {
        String patronId = UUID.randomUUID().toString();
        assertThrows(InvalidInputException.class,
                () -> service.getLoanById(patronId, "too-short"));
    }

    @Test
    void getLoanById_notFound_throwsNotFoundException() {
        String patronId = UUID.randomUUID().toString();
        String loanId = UUID.randomUUID().toString();
        when(loansServiceClient.getLoanById(patronId, loanId))
                .thenThrow(new NotFoundException("not found"));
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.getLoanById(patronId, loanId));
        assertTrue(ex.getMessage().contains(loanId));
    }

    @Test
    void addLoan_withNullRequest_throwsInvalidInputException() {
        String patronId = UUID.randomUUID().toString();
        assertThrows(InvalidInputException.class,
                () -> service.addLoan(null, patronId));
    }

    @Test
    void addLoan_success_returnsLoanWithLinks() {
        String patronId = UUID.randomUUID().toString();
        LoanRequestModel req = new LoanRequestModel();
        LoanResponseModel created = new LoanResponseModel();
        created.setPatronId(patronId);
        created.setLoanId(UUID.randomUUID().toString());
        when(loansServiceClient.createLoan(patronId, req)).thenReturn(created);

        LoanResponseModel result = service.addLoan(req, patronId);

        assertEquals(created.getLoanId(), result.getLoanId());
        assertTrue(result.getLink("self").isPresent());
        assertTrue(result.getLink("all-loans").isPresent());
    }

    @Test
    void updateLoan_withInvalidId_throwsInvalidInputException() {
        String patronId = UUID.randomUUID().toString();
        assertThrows(InvalidInputException.class,
                () -> service.updateLoan(new LoanRequestModel(), patronId, "bad-id"));
    }

    @Test
    void updateLoan_withNullRequest_throwsInvalidInputException() {
        String patronId = UUID.randomUUID().toString();
        String loanId   = UUID.randomUUID().toString();
        assertThrows(InvalidInputException.class,
                () -> service.updateLoan(null, patronId, loanId));
    }

    @Test
    void updateLoan_notFound_throwsNotFoundException() {
        String patronId = UUID.randomUUID().toString();
        String loanId   = UUID.randomUUID().toString();
        LoanRequestModel req = new LoanRequestModel();
        when(loansServiceClient.updateLoan(patronId, loanId, req))
                .thenThrow(new NotFoundException("not found"));
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.updateLoan(req, patronId, loanId));
        assertTrue(ex.getMessage().contains(loanId));
    }

    @Test
    void updateLoan_success_returnsLoanWithLinks() {
        String patronId = UUID.randomUUID().toString();
        String loanId   = UUID.randomUUID().toString();
        LoanRequestModel req = new LoanRequestModel();
        LoanResponseModel updated = new LoanResponseModel();
        updated.setPatronId(patronId);
        updated.setLoanId(loanId);
        when(loansServiceClient.updateLoan(patronId, loanId, req)).thenReturn(updated);

        LoanResponseModel result = service.updateLoan(req, patronId, loanId);

        assertEquals(loanId, result.getLoanId());
        assertTrue(result.getLink("self").isPresent());
        assertTrue(result.getLink("all-loans").isPresent());
    }

    @Test
    void removeLoan_withInvalidId_throwsInvalidInputException() {
        String patronId = UUID.randomUUID().toString();
        assertThrows(InvalidInputException.class,
                () -> service.removeLoan(patronId, "short"));
    }

    @Test
    void removeLoan_notFound_throwsNotFoundException() {
        String patronId = UUID.randomUUID().toString();
        String loanId   = UUID.randomUUID().toString();
        doThrow(new NotFoundException("not found"))
                .when(loansServiceClient).deleteLoan(patronId, loanId);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.removeLoan(patronId, loanId));
        assertTrue(ex.getMessage().contains(loanId));
    }

    @Test
    void removeLoan_success_invokesClientDeleteOnce() {
        String patronId = UUID.randomUUID().toString();
        String loanId   = UUID.randomUUID().toString();
        doNothing().when(loansServiceClient).deleteLoan(patronId, loanId);

        service.removeLoan(patronId, loanId);

        verify(loansServiceClient, times(1)).deleteLoan(patronId, loanId);
    }

    @Test
    void getLoanById_nullLoanId_throwsInvalidInputException() {
        String patronId = UUID.randomUUID().toString();
        assertThrows(InvalidInputException.class,
                () -> service.getLoanById(patronId, null));
    }

    @Test
    void updateLoan_nullLoanId_throwsInvalidInputException() {
        String patronId = UUID.randomUUID().toString();
        LoanRequestModel req = new LoanRequestModel();
        assertThrows(InvalidInputException.class,
                () -> service.updateLoan(req, patronId, null));
    }

    @Test
    void removeLoan_nullLoanId_throwsInvalidInputException() {
        String patronId = UUID.randomUUID().toString();
        assertThrows(InvalidInputException.class,
                () -> service.removeLoan(patronId, null));
    }
}