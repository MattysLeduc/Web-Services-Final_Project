package com.leduc.apigateway.patrons.businessLayer;

import com.leduc.apigateway.patrons.domainclientLayer.PatronPhoneNumber;
import com.leduc.apigateway.patrons.domainclientLayer.PhoneType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import com.leduc.apigateway.patrons.domainclientLayer.PatronsServiceClient;
import com.leduc.apigateway.patrons.presentationLayer.PatronRequestModel;
import com.leduc.apigateway.patrons.presentationLayer.PatronResponseModel;
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
class PatronsServiceUnitTest {
    @Mock private PatronsServiceClient patronsServiceClient;
    @InjectMocks private PatronsServiceImpl service;

    @Test
    void getPatrons_returnsListWithHateoasLinks() {
        PatronResponseModel p1 = new PatronResponseModel();
        p1.setPatronId(UUID.randomUUID().toString());
        when(patronsServiceClient.getAllPatrons())
                .thenReturn(Arrays.asList(p1));

        List<PatronResponseModel> list = service.getPatrons();
        assertEquals(1, list.size());
        assertTrue(list.get(0).getLink("self").isPresent());
        assertTrue(list.get(0).getLink("all-patrons").isPresent());
    }

    @Test
    void getPatronByPatronId_invalidId_throwsInvalidInputException() {
        assertThrows(InvalidInputException.class,
                () -> service.getPatronByPatronId("bad"));
    }

    @Test
    void getPatronByPatronId_notFound_throwsNotFoundException() {
        String id = UUID.randomUUID().toString();
        when(patronsServiceClient.getPatronByPatronId(id))
                .thenThrow(EntityNotFoundException.class);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.getPatronByPatronId(id));
        assertTrue(ex.getMessage().contains(id));
    }

    @Test
    void createPatron_nullRequest_throwsInvalidInputException() {
        assertThrows(InvalidInputException.class,
                () -> service.createPatron(null));
    }

    @Test
    void createPatron_success_returnsWithLinks() {
        PatronRequestModel req = new PatronRequestModel();
        PatronResponseModel created = new PatronResponseModel();
        created.setPatronId(UUID.randomUUID().toString());
        when(patronsServiceClient.createPatron(req)).thenReturn(created);

        PatronResponseModel result = service.createPatron(req);
        assertTrue(result.getLink("self").isPresent());
        assertTrue(result.getLink("all-patrons").isPresent());
    }

    @Test
    void updatePatron_invalidId_throwsInvalidInputException() {
        assertThrows(InvalidInputException.class,
                () -> service.updatePatron("bad", new PatronRequestModel()));
    }

    @Test
    void updatePatron_nullRequest_throwsInvalidInputException() {
        String id = UUID.randomUUID().toString();
        assertThrows(InvalidInputException.class,
                () -> service.updatePatron(id, null));
    }

    @Test
    void updatePatron_notFound_throwsNotFoundException() {
        String id = UUID.randomUUID().toString();
        PatronRequestModel req = new PatronRequestModel();
        when(patronsServiceClient.updatePatron(id, req))
                .thenThrow(EntityNotFoundException.class);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.updatePatron(id, req));
        assertTrue(ex.getMessage().contains(id));
    }

    @Test
    void deletePatron_invalidId_throwsInvalidInputException() {
        assertThrows(InvalidInputException.class,
                () -> service.deletePatron("bad"));
    }

    @Test
    void deletePatron_notFound_throwsNotFoundException() {
        String id = UUID.randomUUID().toString();
        doThrow(EntityNotFoundException.class)
                .when(patronsServiceClient).deletePatron(id);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.deletePatron(id));
        assertTrue(ex.getMessage().contains(id));
    }

    @Test
    void deletePatron_success_invokesClientDeleteOnce() {
        String id = UUID.randomUUID().toString();
        doNothing().when(patronsServiceClient).deletePatron(id);

        service.deletePatron(id);

        verify(patronsServiceClient, times(1)).deletePatron(id);
    }

    @Test
    void getPatronByPatronId_nullId_throwsInvalidInputException() {
        assertThrows(InvalidInputException.class,
                () -> service.getPatronByPatronId(null));
    }

    @Test
    void updatePatron_nullId_throwsInvalidInputException() {
        PatronRequestModel req = new PatronRequestModel();
        assertThrows(InvalidInputException.class,
                () -> service.updatePatron(null, req));
    }

    @Test
    void deletePatron_nullId_throwsInvalidInputException() {
        assertThrows(InvalidInputException.class,
                () -> service.deletePatron(null));
    }

    @Test
    void patronPhoneNumber_constructorAndGetters_shouldWork() {
        PatronPhoneNumber phone = new PatronPhoneNumber(PhoneType.MOBILE, "514-123-4567");
        assertEquals(PhoneType.MOBILE, phone.getType());
        assertEquals("514-123-4567", phone.getNumber());
    }
}