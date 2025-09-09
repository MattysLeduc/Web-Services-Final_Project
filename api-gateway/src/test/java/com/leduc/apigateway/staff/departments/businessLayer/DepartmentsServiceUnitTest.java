package com.leduc.apigateway.staff.departments.businessLayer;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import com.leduc.apigateway.staff.departments.domainclientLayer.DepartmentsServiceClient;
import com.leduc.apigateway.staff.departments.presentationLayer.DepartmentRequestModel;
import com.leduc.apigateway.staff.departments.presentationLayer.DepartmentResponseModel;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration")
class DepartmentsServiceUnitTest {

    @Mock private DepartmentsServiceClient departmentsServiceClient;
    @InjectMocks private DepartmentsServiceImpl service;

    @Test
    void getDepartments_returnsListWithLinks() {
        DepartmentResponseModel d = new DepartmentResponseModel();
        d.setDepartmentId(UUID.randomUUID().toString());
        when(departmentsServiceClient.getAllDepartments())
                .thenReturn(Arrays.asList(d));

        List<DepartmentResponseModel> list = service.getDepartments();
        assertEquals(1, list.size());
        assertTrue(list.get(0).getLink("self").isPresent());
        assertTrue(list.get(0).getLink("all-departments").isPresent());
    }

    @Test
    void getDepartmentById_invalidId_throwsInvalidInputException() {
        assertThrows(InvalidInputException.class,
                () -> service.getDepartmentById("short"));
    }

    @Test
    void getDepartmentById_notFound_throwsNotFoundException() {
        String id = UUID.randomUUID().toString();
        when(departmentsServiceClient.getDepartmentById(id))
                .thenThrow(EntityNotFoundException.class);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.getDepartmentById(id));
        assertTrue(ex.getMessage().contains(id));
    }

    @Test
    void createDepartment_nullRequest_throwsInvalidInputException() {
        assertThrows(InvalidInputException.class,
                () -> service.createDepartment(null));
    }

    @Test
    void createDepartment_success_returnsWithLinks() {
        DepartmentRequestModel req = new DepartmentRequestModel();
        DepartmentResponseModel created = new DepartmentResponseModel();
        created.setDepartmentId(UUID.randomUUID().toString());
        when(departmentsServiceClient.createDepartment(req)).thenReturn(created);

        DepartmentResponseModel result = service.createDepartment(req);
        assertTrue(result.getLink("self").isPresent());
        assertTrue(result.getLink("all-departments").isPresent());
    }

    @Test
    void updateDepartment_invalidId_throwsInvalidInputException() {
        assertThrows(InvalidInputException.class,
                () -> service.updateDepartment("bad", new DepartmentRequestModel()));
    }

    @Test
    void updateDepartment_nullRequest_throwsInvalidInputException() {
        String id = UUID.randomUUID().toString();
        assertThrows(InvalidInputException.class,
                () -> service.updateDepartment(id, null));
    }

    @Test
    void updateDepartment_notFound_throwsNotFoundException() {
        String id = UUID.randomUUID().toString();
        DepartmentRequestModel req = new DepartmentRequestModel();
        when(departmentsServiceClient.updateDepartment(id, req))
                .thenThrow(EntityNotFoundException.class);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.updateDepartment(id, req));
        assertTrue(ex.getMessage().contains(id));
    }

    @Test
    void deleteDepartment_invalidId_throwsInvalidInputException() {
        assertThrows(InvalidInputException.class,
                () -> service.deleteDepartment("bad"));
    }

    @Test
    void deleteDepartment_notFound_throwsNotFoundException() {
        String id = UUID.randomUUID().toString();
        doThrow(EntityNotFoundException.class)
                .when(departmentsServiceClient).deleteDepartment(id);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.deleteDepartment(id));
        assertTrue(ex.getMessage().contains(id));
    }

    @Test
    void deleteDepartment_success_invokesClientDeleteOnce() {
        String id = UUID.randomUUID().toString();
        doNothing().when(departmentsServiceClient).deleteDepartment(id);

        service.deleteDepartment(id);

        verify(departmentsServiceClient, times(1)).deleteDepartment(id);
    }
}