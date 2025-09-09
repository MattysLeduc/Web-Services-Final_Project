package com.leduc.loans.domainclientLayer.patrons;

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
        "app.patrons-service.host=localhost",
        "app.patrons-service.port=7002"
})
class PatronsServiceClientTest {

    @Autowired private RestTemplate restTemplate;
    @Autowired private ObjectMapper mapper;
    @Autowired private PatronsServiceClient client;

    private MockRestServiceServer server;
    private final String BASE = "http://localhost:7002/api/v1/patrons";

    @BeforeEach
    void init() {
        server = MockRestServiceServer.createServer(restTemplate);
    }



    @Test
    void getAllPatrons_whenFound_returnsList() throws Exception {
        // Arrange
        var p1 = PatronModel.builder().patronId("223e4567-e89b-12d3-a456-426614174001").firstName("F1").lastName("L1").build();
        var p2 = PatronModel.builder().patronId("323e4567-e89b-12d3-a456-426614174002").firstName("F2").lastName("L2").build();
        var list = List.of(p1, p2);

        server.expect(requestTo(BASE))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(list), MediaType.APPLICATION_JSON));

        List<PatronModel> result = client.getAllPatrons();

        assertThat(result).hasSize(2)
                .usingElementComparatorOnFields("patronId", "firstName", "lastName")
                .containsExactly(p1, p2);
    }

    @Test
    void getPatronById_whenFound_returnsModel() throws Exception {
        var p = PatronModel.builder()
                .patronId("123e4567-e89b-12d3-a456-426614174000").firstName("F").lastName("L")
                .build();
        server.expect(requestTo(BASE + "/123e4567-e89b-12d3-a456-426614174000"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(p), MediaType.APPLICATION_JSON));

        assertThat(client.getPatronByPatronId("123e4567-e89b-12d3-a456-426614174000"))
                .usingRecursiveComparison().isEqualTo(p);
    }

    @Test
    void getPatronById_when404_throwsNotFound() {
        server.expect(requestTo(BASE + "/123e4567-e89b-12d3-a456-426614174001"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(org.springframework.http.HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.getPatronByPatronId("123e4567-e89b-12d3-a456-426614174001"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getPatronById_when422_throwsInvalidInput() {
        server.expect(requestTo(BASE + "/123e4567-e89b-12d3-a456-42661417400"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY));

        assertThatThrownBy(() -> client.getPatronByPatronId("123e4567-e89b-12d3-a456-42661417400"))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void getAllPatrons_when404_throwsNotFound() {
        server.expect(requestTo(BASE))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"No patrons here\"}"));

        assertThatThrownBy(client::getAllPatrons)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No patrons here");
    }

    @Test
    void getAllPatrons_when422_throwsInvalidInput() {
        server.expect(requestTo(BASE))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Bad patrons request\"}"));

        assertThatThrownBy(client::getAllPatrons)
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Bad patrons request");
    }


}