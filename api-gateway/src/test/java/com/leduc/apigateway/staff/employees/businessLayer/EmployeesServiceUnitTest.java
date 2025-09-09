package com.leduc.apigateway.staff.employees.businessLayer;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import com.leduc.apigateway.staff.employees.domainclientLayer.EmployeesServiceClient;
import com.leduc.apigateway.staff.employees.presentationLayer.EmployeeRequestModel;
import com.leduc.apigateway.staff.employees.presentationLayer.EmployeeResponseModel;
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
class EmployeesServiceUnitTest {
    @Mock private EmployeesServiceClient employeesServiceClient;
    @InjectMocks private EmployeesServiceImpl service;

    @Test
    void getAllEmployees_returnsListWithLinks() {
        EmployeeResponseModel e = new EmployeeResponseModel();
        e.setEmployeeId(UUID.randomUUID().toString());
        when(employeesServiceClient.getAllEmployees())
                .thenReturn(Arrays.asList(e));

        List<EmployeeResponseModel> list = service.getAllEmployees();
        assertEquals(1, list.size());
        assertTrue(list.get(0).getLink("self").isPresent());
        assertTrue(list.get(0).getLink("all-employees").isPresent());
    }

    @Test
    void getEmployeeByEmployeeId_invalidId_throwsInvalidInputException() {
        assertThrows(InvalidInputException.class,
                () -> service.getEmployeeByEmployeeId("bad"));
    }

    @Test
    void getEmployeeByEmployeeId_notFound_throwsNotFoundException() {
        String id = UUID.randomUUID().toString();
        when(employeesServiceClient.getEmployeeByEmployeeId(id))
                .thenThrow(EntityNotFoundException.class);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.getEmployeeByEmployeeId(id));
        assertTrue(ex.getMessage().contains(id));
    }

    @Test
    void createEmployee_nullRequest_throwsInvalidInputException() {
        assertThrows(InvalidInputException.class,
                () -> service.createEmployee(null));
    }

    @Test
    void createEmployee_success_returnsWithLinks() {
        EmployeeRequestModel req = new EmployeeRequestModel();
        EmployeeResponseModel created = new EmployeeResponseModel();
        created.setEmployeeId(UUID.randomUUID().toString());
        when(employeesServiceClient.createEmployee(req)).thenReturn(created);

        EmployeeResponseModel result = service.createEmployee(req);
        assertTrue(result.getLink("self").isPresent());
        assertTrue(result.getLink("all-employees").isPresent());
    }

    @Test
    void updateEmployee_invalidId_throwsInvalidInputException() {
        assertThrows(InvalidInputException.class,
                () -> service.updateEmployee("bad", new EmployeeRequestModel()));
    }

    @Test
    void updateEmployee_nullRequest_throwsInvalidInputException() {
        String id = UUID.randomUUID().toString();
        assertThrows(InvalidInputException.class,
                () -> service.updateEmployee(id, null));
    }

    @Test
    void updateEmployee_notFound_throwsNotFoundException() {
        String id = UUID.randomUUID().toString();
        EmployeeRequestModel req = new EmployeeRequestModel();
        when(employeesServiceClient.updateEmployee(id, req))
                .thenThrow(EntityNotFoundException.class);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.updateEmployee(id, req));
        assertTrue(ex.getMessage().contains(id));
    }

    @Test
    void deleteEmployee_invalidId_throwsInvalidInputException() {
        assertThrows(InvalidInputException.class,
                () -> service.deleteEmployee("bad"));
    }

    @Test
    void deleteEmployee_notFound_throwsNotFoundException() {
        String id = UUID.randomUUID().toString();
        doThrow(EntityNotFoundException.class)
                .when(employeesServiceClient).deleteEmployee(id);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.deleteEmployee(id));
        assertTrue(ex.getMessage().contains(id));
    }

    @Test
    void deleteEmployee_success_invokesClientDeleteOnce() {
        String id = UUID.randomUUID().toString();
        doNothing().when(employeesServiceClient).deleteEmployee(id);

        service.deleteEmployee(id);

        verify(employeesServiceClient, times(1)).deleteEmployee(id);
    }
}