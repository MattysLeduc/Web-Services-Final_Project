package com.leduc.apigateway.loans.presentationLayer;

import com.leduc.apigateway.loans.businessLayer.LoansService;
import com.leduc.apigateway.loans.domainclientLayer.LoanStatus;
import com.leduc.apigateway.utils.exceptions.InvalidInputException;
import com.leduc.apigateway.utils.exceptions.NotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class LoansControllerUnitTest {

    @Autowired
    private LoansController loansController;

    @MockitoBean
    private LoansService loansService;

    private final String PATRON_ID       = "123e4567-e89b-12d3-a456-426614174000";
    private final String LOAN_ID         = "987e6543-e21b-12d3-a456-426655440000";
    private final String MISSING_LOAN_ID = "00000000-0000-0000-0000-000000000000";
    private final String BAD_ID          = "bad-id";

    @Test
    void getAllLoans_thenOk() {
        when(loansService.getAllLoans(PATRON_ID)).thenReturn(Collections.emptyList());

        ResponseEntity<List<LoanResponseModel>> resp = loansController.getAllLoans(PATRON_ID);
        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody().isEmpty());
        verify(loansService).getAllLoans(PATRON_ID);
    }

    @Test
    void getLoanById_thenOk() {
        var loan = new LoanResponseModel();
        loan.setPatronId(PATRON_ID);
        loan.setLoanId(LOAN_ID);
        when(loansService.getLoanById(PATRON_ID, LOAN_ID)).thenReturn(loan);

        ResponseEntity<LoanResponseModel> resp = loansController.getLoanById(PATRON_ID, LOAN_ID);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(LOAN_ID, resp.getBody().getLoanId());
    }

    @Test
    void getLoanById_notFound_throwsEntityNotFound() {
        when(loansService.getLoanById(PATRON_ID, MISSING_LOAN_ID)).thenReturn(null);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                loansController.getLoanById(PATRON_ID, MISSING_LOAN_ID)
        );
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void getLoanById_invalid_throwsInvalidInput() {
        when(loansService.getLoanById(PATRON_ID, BAD_ID))
                .thenThrow(new InvalidInputException("Invalid ID"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                loansController.getLoanById(PATRON_ID, BAD_ID)
        );
        assertEquals("Invalid ID", ex.getMessage());
    }

    @Test
    void createLoan_thenCreated() {
        var req = new LoanRequestModel();
        req.setBookId("222e6543-e21b-12d3-a456-426655440000");
        req.setStatus(LoanStatus.CHECKED_OUT);

        var created = new LoanResponseModel();
        created.setPatronId(PATRON_ID);
        created.setLoanId(LOAN_ID);
        when(loansService.addLoan(req, PATRON_ID)).thenReturn(created);

        ResponseEntity<LoanResponseModel> resp = loansController.createLoan(PATRON_ID, req);
        assertEquals(201, resp.getStatusCodeValue());
        assertEquals(LOAN_ID, resp.getBody().getLoanId());
    }

    @Test
    void createLoan_null_throwsInvalidInput() {
        when(loansService.addLoan(null, PATRON_ID))
                .thenThrow(new InvalidInputException("LoanRequestModel must not be null"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                loansController.createLoan(PATRON_ID, null)
        );
        assertEquals("LoanRequestModel must not be null", ex.getMessage());
    }

    @Test
    void updateLoan_thenOk() {
        var req = new LoanRequestModel();
        req.setStatus(LoanStatus.RETURNED);

        var updated = new LoanResponseModel();
        updated.setPatronId(PATRON_ID);
        updated.setLoanId(LOAN_ID);
        updated.setStatus(LoanStatus.RETURNED);

        when(loansService.getLoanById(PATRON_ID, LOAN_ID)).thenReturn(updated);
        when(loansService.updateLoan(req, PATRON_ID, LOAN_ID)).thenReturn(updated);

        ResponseEntity<LoanResponseModel> resp =
                loansController.updateLoan(PATRON_ID, LOAN_ID, req);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(LoanStatus.RETURNED, resp.getBody().getStatus());
    }

    @Test
    void updateLoan_notFound_throwsNotFound() {
        var req = new LoanRequestModel();
        when(loansService.getLoanById(PATRON_ID, MISSING_LOAN_ID)).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                loansController.updateLoan(PATRON_ID, MISSING_LOAN_ID, req)
        );
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void updateLoan_invalid_throwsInvalidInput() {
        var req = new LoanRequestModel();
        when(loansService.getLoanById(PATRON_ID, BAD_ID))
                .thenThrow(new InvalidInputException("Invalid ID"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                loansController.updateLoan(PATRON_ID, BAD_ID, req)
        );
        assertEquals("Invalid ID", ex.getMessage());
    }

    @Test
    void updateLoan_nullRequest_throwsInvalidInput() {
        // first, service finds the loan
        var existing = new LoanResponseModel();
        existing.setPatronId(PATRON_ID);
        existing.setLoanId(LOAN_ID);
        when(loansService.getLoanById(PATRON_ID, LOAN_ID)).thenReturn(existing);

        when(loansService.updateLoan(null, PATRON_ID, LOAN_ID))
                .thenThrow(new InvalidInputException("LoanRequestModel must not be null"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                loansController.updateLoan(PATRON_ID, LOAN_ID, null)
        );
        assertEquals("LoanRequestModel must not be null", ex.getMessage());
    }

    @Test
    void deleteLoan_thenNoContent() {
        var existing = new LoanResponseModel();
        existing.setPatronId(PATRON_ID);
        existing.setLoanId(LOAN_ID);
        when(loansService.getLoanById(PATRON_ID, LOAN_ID)).thenReturn(existing);
        doNothing().when(loansService).removeLoan(PATRON_ID, LOAN_ID);

        ResponseEntity<Void> resp = loansController.deleteLoan(PATRON_ID, LOAN_ID);
        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void deleteLoan_notFound_throwsEntityNotFound() {
        when(loansService.getLoanById(PATRON_ID, MISSING_LOAN_ID)).thenReturn(null);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                loansController.deleteLoan(PATRON_ID, MISSING_LOAN_ID)
        );
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void deleteLoan_invalid_throwsInvalidInput() {
        when(loansService.getLoanById(PATRON_ID, BAD_ID))
                .thenThrow(new InvalidInputException("Invalid ID"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                loansController.deleteLoan(PATRON_ID, BAD_ID)
        );
        assertEquals("Invalid ID", ex.getMessage());
    }

    @Test
    void updateLoan_nullId_throwsInvalidInput() {
        var req = new LoanRequestModel();
        req.setStatus(LoanStatus.RETURNED);

        when(loansService.getLoanById(PATRON_ID, null))
                .thenThrow(new InvalidInputException("Loan ID must be exactly 36 characters long"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                loansController.updateLoan(PATRON_ID, null, req)
        );
        assertEquals("Loan ID must be exactly 36 characters long", ex.getMessage());
    }

    @Test
    void deleteLoan_nullId_throwsInvalidInput() {
        when(loansService.getLoanById(PATRON_ID, null))
                .thenThrow(new InvalidInputException("Loan ID must be exactly 36 characters long"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                loansController.deleteLoan(PATRON_ID, null)
        );
        assertEquals("Loan ID must be exactly 36 characters long", ex.getMessage());
    }

    @Test
    void updateLoan_nullRequestBody_throwsInvalidInput() {
        var existing = new LoanResponseModel();
        existing.setPatronId(PATRON_ID);
        existing.setLoanId(LOAN_ID);
        when(loansService.getLoanById(PATRON_ID, LOAN_ID)).thenReturn(existing);

        when(loansService.updateLoan(null, PATRON_ID, LOAN_ID))
                .thenThrow(new InvalidInputException("LoanRequestModel must not be null"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                loansController.updateLoan(PATRON_ID, LOAN_ID, null)
        );
        assertEquals("LoanRequestModel must not be null", ex.getMessage());
    }

    @Test
    void deleteLoan_nullRequestId_throwsInvalidInput() {
        // existence check first:
        when(loansService.getLoanById(PATRON_ID, null))
                .thenThrow(new InvalidInputException("Loan ID must be exactly 36 characters long"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                loansController.deleteLoan(PATRON_ID, null)
        );
        assertEquals("Loan ID must be exactly 36 characters long", ex.getMessage());
    }


}
