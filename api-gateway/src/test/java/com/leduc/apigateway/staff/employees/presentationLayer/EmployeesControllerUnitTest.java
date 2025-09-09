package com.leduc.apigateway.staff.employees.presentationLayer;

import com.leduc.apigateway.staff.employees.businessLayer.EmployeesService;
import com.leduc.apigateway.utils.exceptions.InvalidInputException;
import com.leduc.apigateway.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class EmployeesControllerUnitTest {

    @Autowired
    private EmployeesController controller;

    @MockitoBean
    private EmployeesService service;

    private final String VALID_ID   = "123e4567-e89b-12d3-a456-426614174000";
    private final String MISSING_ID = "00000000-0000-0000-0000-000000000000";
    private final String BAD_ID     = "bad-id";

    @Test
    void getAllEmployees_thenOk() {
        when(service.getAllEmployees()).thenReturn(Collections.emptyList());

        ResponseEntity<List<EmployeeResponseModel>> resp = controller.getAllEmployees();
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().isEmpty());
        verify(service, times(1)).getAllEmployees();
    }

    @Test
    void getEmployeeById_thenOk() {
        var emp = new EmployeeResponseModel();
        emp.setEmployeeId(VALID_ID);
        when(service.getEmployeeByEmployeeId(VALID_ID)).thenReturn(emp);

        ResponseEntity<EmployeeResponseModel> resp = controller.getEmployeeByEmployeeId(VALID_ID);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(VALID_ID, resp.getBody().getEmployeeId());
    }

    @Test
    void getEmployeeById_notFound_throwsNotFound() {
        when(service.getEmployeeByEmployeeId(MISSING_ID))
                .thenThrow(new NotFoundException("Employee not found"));

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                controller.getEmployeeByEmployeeId(MISSING_ID)
        );
        assertEquals("Employee not found", ex.getMessage());
    }

    @Test
    void getEmployeeById_invalid_throwsInvalidInput() {
        when(service.getEmployeeByEmployeeId(BAD_ID))
                .thenThrow(new InvalidInputException("Employee ID must be exactly 36 characters long"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                controller.getEmployeeByEmployeeId(BAD_ID)
        );
        assertEquals("Employee ID must be exactly 36 characters long", ex.getMessage());
    }

    @Test
    void createEmployee_thenCreated() {
        var req = new EmployeeRequestModel();
        req.setFirstName("Carol");
        req.setLastName("Bertrand");

        var created = new EmployeeResponseModel();
        created.setEmployeeId(VALID_ID);
        when(service.createEmployee(req)).thenReturn(created);

        ResponseEntity<EmployeeResponseModel> resp = controller.createEmployee(req);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertEquals(VALID_ID, resp.getBody().getEmployeeId());
    }

    @Test
    void createEmployee_null_throwsInvalidInput() {
        when(service.createEmployee(null))
                .thenThrow(new InvalidInputException("EmployeeRequestModel must not be null"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                controller.createEmployee(null)
        );
        assertEquals("EmployeeRequestModel must not be null", ex.getMessage());
    }

    @Test
    void updateEmployee_thenOk() {
        var req = new EmployeeRequestModel();
        req.setFirstName("Dave");
        req.setLastName("Leduc");

        var updated = new EmployeeResponseModel();
        updated.setEmployeeId(VALID_ID);
        when(service.updateEmployee(VALID_ID, req)).thenReturn(updated);

        ResponseEntity<EmployeeResponseModel> resp = controller.updateEmployee(VALID_ID, req);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(VALID_ID, resp.getBody().getEmployeeId());
    }

    @Test
    void updateEmployee_notFound_throwsNotFound() {
        var req = new EmployeeRequestModel();
        when(service.updateEmployee(MISSING_ID, req))
                .thenThrow(new NotFoundException("Employee not found"));

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                controller.updateEmployee(MISSING_ID, req)
        );
        assertEquals("Employee not found", ex.getMessage());
    }

    @Test
    void updateEmployee_invalidId_throwsInvalidInput() {
        var req = new EmployeeRequestModel();
        when(service.updateEmployee(BAD_ID, req))
                .thenThrow(new InvalidInputException("Employee ID must be exactly 36 characters long"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                controller.updateEmployee(BAD_ID, req)
        );
        assertEquals("Employee ID must be exactly 36 characters long", ex.getMessage());
    }

    @Test
    void updateEmployee_nullRequest_throwsInvalidInput() {
        when(service.updateEmployee(VALID_ID, null))
                .thenThrow(new InvalidInputException("EmployeeRequestModel must not be null"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                controller.updateEmployee(VALID_ID, null)
        );
        assertEquals("EmployeeRequestModel must not be null", ex.getMessage());
    }

    @Test
    void deleteEmployee_thenNoContent() {
        doNothing().when(service).deleteEmployee(VALID_ID);

        ResponseEntity<Void> resp = controller.deleteEmployee(VALID_ID);
        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
    }

    @Test
    void deleteEmployee_notFound_throwsNotFound() {
        doThrow(new NotFoundException("Employee not found"))
                .when(service).deleteEmployee(MISSING_ID);

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                controller.deleteEmployee(MISSING_ID)
        );
        assertEquals("Employee not found", ex.getMessage());
    }

    @Test
    void deleteEmployee_invalid_throwsInvalidInput() {
        doThrow(new InvalidInputException("Employee ID must be exactly 36 characters long"))
                .when(service).deleteEmployee(BAD_ID);

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                controller.deleteEmployee(BAD_ID)
        );
        assertEquals("Employee ID must be exactly 36 characters long", ex.getMessage());
    }
}
