package com.leduc.loans.domainclientLayer.staff;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leduc.loans.utils.exceptions.InvalidInputException;
import com.leduc.loans.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.employees-service.host=localhost",
        "app.employees-service.port=7003"
})
class EmployeesServiceClientTest {

    @Autowired private RestTemplate restTemplate;
    @Autowired private ObjectMapper mapper;
    @Autowired private EmployeesServiceClient client;

    private MockRestServiceServer server;
    private final String BASE = "http://localhost:7003/api/v1/staff";

    @BeforeEach
    void init() {
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void getAllEmployees_whenFound_returnsList() throws Exception {
        var e1 = EmployeeModel.builder().employeeId("e8a17e76-1c9f-4a6a-9342-488b7e99f0f7").firstName("X1").lastName("Y1").build();
        var e2 = EmployeeModel.builder().employeeId("95e3de7a-c9bc-45cf-b6fc-b8f4c1157369").firstName("X2").lastName("Y2").build();
        var list = List.of(e1, e2);

        server.expect(requestTo(BASE))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(list), MediaType.APPLICATION_JSON));

        List<EmployeeModel> result = client.getAllEmployees();

        assertThat(result).hasSize(2)
                .usingElementComparatorOnFields("employeeId", "firstName", "lastName")
                .containsExactly(e1, e2);
    }

    @Test
    void getEmployeeById_whenFound_returnsModel() throws Exception {
        var e = EmployeeModel.builder()
                .employeeId("e8a17e76-1c9f-4a6a-9342-488b7e99f0f7").firstName("X").lastName("Y")
                .build();
        server.expect(requestTo(BASE + "/e8a17e76-1c9f-4a6a-9342-488b7e99f0f7"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(e), MediaType.APPLICATION_JSON));

        assertThat(client.getEmployeeByEmployeeId("e8a17e76-1c9f-4a6a-9342-488b7e99f0f7"))
                .usingRecursiveComparison().isEqualTo(e);
    }

    @Test
    void getEmployeeById_when404_throwsNotFound() {
        server.expect(requestTo(BASE + "/e8a17e76-1c9f-4a6a-9342-488b7e99f0f0"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(org.springframework.http.HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.getEmployeeByEmployeeId("e8a17e76-1c9f-4a6a-9342-488b7e99f0f0"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getEmployeeById_when422_throwsInvalidInput() {
        server.expect(requestTo(BASE + "/e8a17e76-1c9f-4a6a-9342-488b7e99f0f"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY));

        assertThatThrownBy(() -> client.getEmployeeByEmployeeId("e8a17e76-1c9f-4a6a-9342-488b7e99f0f"))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void getAllEmployees_when404_throwsNotFound() {
        server.expect(requestTo(BASE))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Staff not there\"}"));

        assertThatThrownBy(client::getAllEmployees)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Staff not there");
    }

    @Test
    void getAllEmployees_when422_throwsInvalidInput() {
        server.expect(requestTo(BASE))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Bad staff query\"}"));

        assertThatThrownBy(client::getAllEmployees)
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Bad staff query");
    }
}