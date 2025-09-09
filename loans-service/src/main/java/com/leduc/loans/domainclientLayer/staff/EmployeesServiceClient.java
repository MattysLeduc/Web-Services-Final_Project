package com.leduc.loans.domainclientLayer.staff;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leduc.loans.utils.HttpErrorInfo;
import com.leduc.loans.utils.exceptions.InvalidInputException;
import com.leduc.loans.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Slf4j
@Component
public class EmployeesServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String EMPLOYEES_SERVICE_BASE_URL;

    public EmployeesServiceClient(RestTemplate restTemplate, ObjectMapper mapper,
                                  @Value("${app.staff-service.host}") String employeeServiceHost,
                                  @Value("${app.staff-service.port}") String employeeServicePort) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        EMPLOYEES_SERVICE_BASE_URL = "http://" + employeeServiceHost + ":" + employeeServicePort + "/api/v1/staff";
    }

    public List<EmployeeModel> getAllEmployees() {
        log.debug("Retrieving all employees via EmployeesServiceClient");
        try {
            String url = EMPLOYEES_SERVICE_BASE_URL;
            log.debug("Employees-Service URL for GET all: " + url);
            ResponseEntity<EmployeeModel[]> responseEntity =
                    restTemplate.getForEntity(url, EmployeeModel[].class);
            EmployeeModel[] array = responseEntity.getBody();
            log.debug("Successfully retrieved employees");
            return array != null ? Arrays.asList(array) : new ArrayList<>();
        } catch (HttpClientErrorException ex) {
            log.debug("Error response received in getAllEmployees");
            throw handleHttpClientException(ex);
        }
    }

    public EmployeeModel getEmployeeByEmployeeId(String employeeId) {
        if (employeeId == null || employeeId.length() != 36) {
            throw new InvalidInputException("Employee ID must be exactly 36 characters long");
        }
        log.debug("Retrieving employee via EmployeesServiceClient for id: {}", employeeId);
        try {
            String url = EMPLOYEES_SERVICE_BASE_URL + "/" + employeeId;
            log.debug("Employees-Service URL: " + url);
            EmployeeModel response = restTemplate.getForObject(url, EmployeeModel.class);
            log.debug("Successfully retrieved employee with id: {}", response.getEmployeeId());
            return response;
        } catch (HttpClientErrorException ex) {
            log.debug("Error response received in getEmployeeByEmployeeId");
            throw handleHttpClientException(ex);
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ioex.getMessage();
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        if (ex.getStatusCode() == NOT_FOUND) {
            return new NotFoundException(getErrorMessage(ex));
        }
        if (ex.getStatusCode() == UNPROCESSABLE_ENTITY) {
            return new InvalidInputException(getErrorMessage(ex));
        }
        log.warn("Unexpected HTTP error: {}. Error body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return ex;
    }
}
