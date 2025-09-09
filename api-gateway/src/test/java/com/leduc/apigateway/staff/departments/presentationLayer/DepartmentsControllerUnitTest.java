package com.leduc.apigateway.staff.departments.presentationLayer;

import com.leduc.apigateway.staff.departments.businessLayer.DepartmentsService;
import com.leduc.apigateway.staff.departments.domainclientLayer.DepartmentName;
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
class DepartmentsControllerUnitTest {

    @Autowired
    private DepartmentsController controller;

    @MockitoBean
    private DepartmentsService service;

    private final String VALID_ID   = "123e4567-e89b-12d3-a456-426614174000";
    private final String MISSING_ID = "00000000-0000-0000-0000-000000000000";
    private final String BAD_ID     = "short-id";

    @Test
    void getDepartments_thenOk() {
        when(service.getDepartments()).thenReturn(Collections.emptyList());

        ResponseEntity<List<DepartmentResponseModel>> resp = controller.getDepartments();
        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody().isEmpty());
        verify(service).getDepartments();
    }

    @Test
    void getDepartmentById_thenOk() {
        var dept = new DepartmentResponseModel();
        dept.setDepartmentId(VALID_ID);
        when(service.getDepartmentById(VALID_ID)).thenReturn(dept);

        ResponseEntity<DepartmentResponseModel> resp = controller.getDepartmentById(VALID_ID);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(VALID_ID, resp.getBody().getDepartmentId());
    }

    @Test
    void getDepartmentById_notFound_throwsNotFoundException() {
        when(service.getDepartmentById(MISSING_ID))
                .thenThrow(new NotFoundException("Department not found"));

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                controller.getDepartmentById(MISSING_ID)
        );
        assertEquals("Department not found", ex.getMessage());
    }

    @Test
    void getDepartmentById_invalid_throwsInvalidInputException() {
        when(service.getDepartmentById(BAD_ID))
                .thenThrow(new InvalidInputException("Department ID must be exactly 36 characters long"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                controller.getDepartmentById(BAD_ID)
        );
        assertEquals("Department ID must be exactly 36 characters long", ex.getMessage());
    }

    @Test
    void createDepartment_thenCreated() {
        var req = new DepartmentRequestModel();
        req.setDepartmentName(DepartmentName.DIGITAL_RESOURCES);

        var created = new DepartmentResponseModel();
        created.setDepartmentId(VALID_ID);
        when(service.createDepartment(req)).thenReturn(created);

        ResponseEntity<DepartmentResponseModel> resp = controller.createDepartment(req);
        assertEquals(201, resp.getStatusCodeValue());
        assertEquals(VALID_ID, resp.getBody().getDepartmentId());
    }

    @Test
    void createDepartment_null_throwsInvalidInputException() {
        when(service.createDepartment(null))
                .thenThrow(new InvalidInputException("DepartmentRequestModel must not be null"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                controller.createDepartment(null)
        );
        assertEquals("DepartmentRequestModel must not be null", ex.getMessage());
    }

    @Test
    void updateDepartment_thenOk() {
        var req = new DepartmentRequestModel();
        req.setDepartmentName(DepartmentName.ARCHIVES_MANAGEMENT);

        var updated = new DepartmentResponseModel();
        updated.setDepartmentId(VALID_ID);
        when(service.updateDepartment(VALID_ID, req)).thenReturn(updated);

        ResponseEntity<DepartmentResponseModel> resp = controller.updateDepartment(VALID_ID, req);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(VALID_ID, resp.getBody().getDepartmentId());
    }

    @Test
    void updateDepartment_notFound_throwsNotFoundException() {
        var req = new DepartmentRequestModel();
        when(service.updateDepartment(MISSING_ID, req))
                .thenThrow(new NotFoundException("Department not found"));

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                controller.updateDepartment(MISSING_ID, req)
        );
        assertEquals("Department not found", ex.getMessage());
    }

    @Test
    void updateDepartment_invalidId_throwsInvalidInputException() {
        var req = new DepartmentRequestModel();
        when(service.updateDepartment(BAD_ID, req))
                .thenThrow(new InvalidInputException("Department ID must be exactly 36 characters long"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                controller.updateDepartment(BAD_ID, req)
        );
        assertEquals("Department ID must be exactly 36 characters long", ex.getMessage());
    }

    @Test
    void updateDepartment_nullRequest_throwsInvalidInputException() {
        when(service.updateDepartment(VALID_ID, null))
                .thenThrow(new InvalidInputException("DepartmentRequestModel must not be null"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                controller.updateDepartment(VALID_ID, null)
        );
        assertEquals("DepartmentRequestModel must not be null", ex.getMessage());
    }

    @Test
    void deleteDepartment_thenNoContent() {
        doNothing().when(service).deleteDepartment(VALID_ID);

        ResponseEntity<Void> resp = controller.deleteDepartment(VALID_ID);
        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void deleteDepartment_notFound_throwsNotFoundException() {
        doThrow(new NotFoundException("Department not found"))
                .when(service).deleteDepartment(MISSING_ID);

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                controller.deleteDepartment(MISSING_ID)
        );
        assertEquals("Department not found", ex.getMessage());
    }

    @Test
    void deleteDepartment_invalid_throwsInvalidInputException() {
        doThrow(new InvalidInputException("Department ID must be exactly 36 characters long"))
                .when(service).deleteDepartment(BAD_ID);

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                controller.deleteDepartment(BAD_ID)
        );
        assertEquals("Department ID must be exactly 36 characters long", ex.getMessage());
    }


}
