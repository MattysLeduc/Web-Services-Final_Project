package com.leduc.apigateway.patrons.presentationLayer;

import com.leduc.apigateway.patrons.businessLayer.PatronsService;
import com.leduc.apigateway.utils.exceptions.InvalidInputException;
import com.leduc.apigateway.utils.exceptions.NotFoundException;
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
class PatronsControllerUnitTest {

    @Autowired
    private PatronsController patronsController;

    @MockitoBean
    private PatronsService patronsService;

    private final String VALID_ID   = "123e4567-e89b-12d3-a456-426614174000";
    private final String MISSING_ID = "00000000-0000-0000-0000-000000000000";
    private final String BAD_ID     = "short-id";

    @Test
    void getAllPatrons_thenOk() {
        when(patronsService.getPatrons()).thenReturn(Collections.emptyList());

        ResponseEntity<List<PatronResponseModel>> resp = patronsController.getAllPatrons();
        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody().isEmpty());
        verify(patronsService, times(1)).getPatrons();
    }

    @Test
    void getPatronById_thenOk() {
        var patron = new PatronResponseModel();
        patron.setPatronId(VALID_ID);
        when(patronsService.getPatronByPatronId(VALID_ID)).thenReturn(patron);

        ResponseEntity<PatronResponseModel> resp = patronsController.getPatronByPatronId(VALID_ID);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(VALID_ID, resp.getBody().getPatronId());
    }

    @Test
    void getPatronById_notFound_throwsNotFound() {
        when(patronsService.getPatronByPatronId(MISSING_ID))
                .thenThrow(new NotFoundException("Patron not found"));

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                patronsController.getPatronByPatronId(MISSING_ID)
        );
        assertEquals("Patron not found", ex.getMessage());
    }

    @Test
    void getPatronById_invalid_throwsInvalidInput() {
        when(patronsService.getPatronByPatronId(BAD_ID))
                .thenThrow(new InvalidInputException("Patron ID must be exactly 36 characters long"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                patronsController.getPatronByPatronId(BAD_ID)
        );
        assertEquals("Patron ID must be exactly 36 characters long", ex.getMessage());
    }

    @Test
    void createPatron_thenCreated() {
        var req = new PatronRequestModel();
        req.setFirstName("Carol");
        req.setLastName("Bertrand");

        var created = new PatronResponseModel();
        created.setPatronId(VALID_ID);
        when(patronsService.createPatron(req)).thenReturn(created);

        ResponseEntity<PatronResponseModel> resp = patronsController.createPatron(req);
        assertEquals(201, resp.getStatusCodeValue());
        assertEquals(VALID_ID, resp.getBody().getPatronId());
    }

    @Test
    void createPatron_null_throwsInvalidInput() {
        when(patronsService.createPatron(null))
                .thenThrow(new InvalidInputException("PatronRequestModel must not be null"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                patronsController.createPatron(null)
        );
        assertEquals("PatronRequestModel must not be null", ex.getMessage());
    }

    @Test
    void updatePatron_thenOk() {
        var req = new PatronRequestModel();
        req.setFirstName("Dave");
        req.setLastName("Leduc");

        var updated = new PatronResponseModel();
        updated.setPatronId(VALID_ID);
        when(patronsService.updatePatron(VALID_ID, req)).thenReturn(updated);

        ResponseEntity<PatronResponseModel> resp = patronsController.updatePatron(VALID_ID, req);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(VALID_ID, resp.getBody().getPatronId());
    }

    @Test
    void updatePatron_notFound_throwsNotFound() {
        var req = new PatronRequestModel();
        when(patronsService.updatePatron(MISSING_ID, req))
                .thenThrow(new NotFoundException("Patron not found"));

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                patronsController.updatePatron(MISSING_ID, req)
        );
        assertEquals("Patron not found", ex.getMessage());
    }

    @Test
    void updatePatron_invalidId_throwsInvalidInput() {
        var req = new PatronRequestModel();
        when(patronsService.updatePatron(BAD_ID, req))
                .thenThrow(new InvalidInputException("Patron ID must be exactly 36 characters long"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                patronsController.updatePatron(BAD_ID, req)
        );
        assertEquals("Patron ID must be exactly 36 characters long", ex.getMessage());
    }

    @Test
    void updatePatron_nullRequest_throwsInvalidInput() {
        when(patronsService.updatePatron(VALID_ID, null))
                .thenThrow(new InvalidInputException("PatronRequestModel must not be null"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                patronsController.updatePatron(VALID_ID, null)
        );
        assertEquals("PatronRequestModel must not be null", ex.getMessage());
    }

    @Test
    void deletePatron_thenNoContent() {
        doNothing().when(patronsService).deletePatron(VALID_ID);

        ResponseEntity<Void> resp = patronsController.deletePatron(VALID_ID);
        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void deletePatron_notFound_throwsNotFound() {
        doThrow(new NotFoundException("Patron not found"))
                .when(patronsService).deletePatron(MISSING_ID);

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                patronsController.deletePatron(MISSING_ID)
        );
        assertEquals("Patron not found", ex.getMessage());
    }

    @Test
    void deletePatron_invalid_throwsInvalidInput() {
        doThrow(new InvalidInputException("Patron ID must be exactly 36 characters long"))
                .when(patronsService).deletePatron(BAD_ID);

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                patronsController.deletePatron(BAD_ID)
        );
        assertEquals("Patron ID must be exactly 36 characters long", ex.getMessage());
    }
}
