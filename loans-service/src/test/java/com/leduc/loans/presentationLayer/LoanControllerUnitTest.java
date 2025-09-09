package com.leduc.loans.presentationLayer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.leduc.loans.businessLayer.LoanService;
import com.leduc.loans.domainclientLayer.patrons.PatronModel;
import com.leduc.loans.domainclientLayer.patrons.PatronsServiceClient;
import com.leduc.loans.utils.exceptions.InvalidInputException;
import com.leduc.loans.utils.exceptions.NotFoundException;
import com.leduc.loans.utils.exceptions.TooManyLoansException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
class
LoanControllerUnitTest {

    @Autowired
    private LoanController loanController;

    @MockitoBean
    private LoanService loanService;

    @MockitoBean
    private PatronsServiceClient patronsServiceClient;

    private final String VALID_PATRON = "11111111-1111-1111-1111-111111111111";
    private final String INVALID_PATRON = "short-id";
    private final String VALID_LOAN   = "22222222-2222-2222-2222-222222222222";

    @BeforeEach
    void setup() {
        PatronModel patron = PatronModel.builder()
                .patronId(VALID_PATRON)
                .firstName("John")
                .lastName("Doe")
                .build();
        when(patronsServiceClient.getPatronByPatronId(VALID_PATRON))
                .thenReturn(patron);
    }

    @Test
    void whenGetAllLoans_thenReturnsEmptyList() {
        when(loanService.getAllLoans(VALID_PATRON)).thenReturn(Collections.emptyList());

        ResponseEntity<List<LoanResponseModel>> response =
                loanController.getAllLoans(VALID_PATRON);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void whenGetAllLoans_withInvalidPatron_thenThrows() {
        assertThrows(InvalidInputException.class, () ->
                loanController.getAllLoans(INVALID_PATRON)
        );
        verify(loanService, never()).getAllLoans(any());
    }

    @Test
    void whenGetLoanById_notFound_thenThrowsNotFound() {
        when(loanService.getLoanById(VALID_PATRON, VALID_LOAN))
                .thenThrow(new NotFoundException("not found"));

        assertThrows(NotFoundException.class, () ->
                loanController.getLoanById(VALID_PATRON, VALID_LOAN)
        );
    }

    @Test
    void whenDeleteLoan_thenNoContent() {
        doNothing().when(loanService).deleteLoan(VALID_PATRON, VALID_LOAN);

        ResponseEntity<Void> resp = loanController.deleteLoan(VALID_PATRON, VALID_LOAN);
        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(loanService).deleteLoan(VALID_PATRON, VALID_LOAN);
    }

    @Test
    void whenGetLoanById_withValidIds_returnsDto() {
        LoanResponseModel dto = new LoanResponseModel();
        when(loanService.getLoanById(VALID_PATRON, VALID_LOAN)).thenReturn(dto);

        ResponseEntity<LoanResponseModel> response =
                loanController.getLoanById(VALID_PATRON, VALID_LOAN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void whenGetLoanById_withInvalidPatronId_thenThrowsInvalidInput() {
        assertThrows(InvalidInputException.class, () ->
                loanController.getLoanById(INVALID_PATRON, VALID_LOAN)
        );
        verify(loanService, never()).getLoanById(any(), any());
    }

    @Test
    void whenGetLoanById_withInvalidLoanId_thenThrowsInvalidInput() {
        assertThrows(InvalidInputException.class, () ->
                loanController.getLoanById(VALID_PATRON, "bad")
        );
        verify(loanService, never()).getLoanById(any(), any());
    }

    @Test
    void whenProcessPatronLoan_withValidData_returnsCreated() {
        LoanRequestModel req = new LoanRequestModel(
                VALID_PATRON, "B1", "E1",
                LocalDate.now(), LocalDate.now(),
                LocalDate.now().plusDays(7), null
        );
        LoanResponseModel dto = new LoanResponseModel();
        when(loanService.addLoan(req, VALID_PATRON)).thenReturn(dto);

        ResponseEntity<LoanResponseModel> response =
                loanController.processPatronLoan(req, VALID_PATRON);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void whenProcessPatronLoan_withInvalidPatronId_thenThrowsInvalidInput() {
        assertThrows(InvalidInputException.class, () ->
                loanController.processPatronLoan(new LoanRequestModel(), INVALID_PATRON)
        );
        verify(loanService, never()).addLoan(any(), any());
    }

    @Test
    void whenUpdateLoan_withValidData_returnsOk() {
        LoanRequestModel req = new LoanRequestModel(
                null, "B2", "E2",
                LocalDate.now(), LocalDate.now(),
                LocalDate.now().plusDays(5), null
        );
        LoanResponseModel dto = new LoanResponseModel();
        when(loanService.updateLoan(req, VALID_PATRON, VALID_LOAN))
                .thenReturn(dto);

        ResponseEntity<LoanResponseModel> response =
                loanController.updateLoan(req, VALID_PATRON, VALID_LOAN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void whenUpdateLoan_withInvalidIds_thenThrowsInvalidInput() {
        assertThrows(InvalidInputException.class, () ->
                loanController.updateLoan(new LoanRequestModel(), INVALID_PATRON, VALID_LOAN)
        );
        assertThrows(InvalidInputException.class, () ->
                loanController.updateLoan(new LoanRequestModel(), VALID_PATRON, "bad")
        );
        verify(loanService, never()).updateLoan(any(), any(), any());
    }

    @Test
    void whenDeleteLoan_withInvalidIds_thenThrowsInvalidInput() {
        assertThrows(InvalidInputException.class, () ->
                loanController.deleteLoan(INVALID_PATRON, VALID_LOAN)
        );
        assertThrows(InvalidInputException.class, () ->
                loanController.deleteLoan(VALID_PATRON, "bad")
        );
        verify(loanService, never()).deleteLoan(any(), any());
    }

    @Test
    void postLoan_withValidData_returnsCreated() {
        // Arrange
        LoanRequestModel req = new LoanRequestModel(
                VALID_PATRON,
                "B1",
                "E1",
                LocalDate.now(),
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                null
        );
        LoanResponseModel dto = new LoanResponseModel();
        dto.setLoanId(UUID.randomUUID().toString());

        when(loanService.addLoan(req, VALID_PATRON)).thenReturn(dto);


        ResponseEntity<LoanResponseModel> response =
                loanController.processPatronLoan(req, VALID_PATRON);


        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void postLoan_whenTooManyLoans_thenThrowsTooManyLoansException() {
        // Arrange
        LoanRequestModel req = new LoanRequestModel(
                VALID_PATRON,
                "B1",
                "E1",
                LocalDate.now(),
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                null
        );
        when(loanService.addLoan(any(LoanRequestModel.class), eq(VALID_PATRON)))
                .thenThrow(new TooManyLoansException("Patron has too many outstanding loans"));

        // Act & Assert
        assertThrows(TooManyLoansException.class, () ->
                loanController.processPatronLoan(req, VALID_PATRON)
        );
    }
}
