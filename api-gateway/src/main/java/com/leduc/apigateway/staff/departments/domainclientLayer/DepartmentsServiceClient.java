package com.leduc.apigateway.staff.departments.domainclientLayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leduc.apigateway.staff.departments.presentationLayer.DepartmentRequestModel;
import com.leduc.apigateway.staff.departments.presentationLayer.DepartmentResponseModel;
import com.leduc.apigateway.utils.HttpErrorInfo;
import com.leduc.apigateway.utils.exceptions.InvalidInputException;
import com.leduc.apigateway.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Slf4j
@Component
public class DepartmentsServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String DEPARTMENTS_SERVICE_BASE_URL;

    public DepartmentsServiceClient(RestTemplate restTemplate, ObjectMapper mapper,
                                    @Value("${app.staff-service.host}") String departmentsServiceHost,
                                    @Value("${app.staff-service.port}") String departmentsServicePort) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        DEPARTMENTS_SERVICE_BASE_URL = "http://" + departmentsServiceHost + ":" + departmentsServicePort + "/api/v1/departments";
    }

    public List<DepartmentResponseModel> getAllDepartments() {
        log.debug("Retrieving all departments via DepartmentsServiceClient");
        try {
            String url = DEPARTMENTS_SERVICE_BASE_URL;
            log.debug("Departments-Service URL for GET all: {}", url);
            DepartmentResponseModel[] responseArray =
                    restTemplate.getForObject(url, DepartmentResponseModel[].class);
            log.debug("Successfully retrieved {} departments",
                    responseArray != null ? responseArray.length : 0);
            return responseArray != null ? Arrays.asList(responseArray) : Collections.emptyList();
        } catch (HttpClientErrorException ex) {
            log.debug("Error response received in getAllDepartments");
            throw handleHttpClientException(ex);
        }
    }

    public DepartmentResponseModel getDepartmentById(String departmentId) {
        if (departmentId == null || departmentId.length() != 36) {
            throw new IllegalArgumentException("Department ID must be exactly 36 characters long");
        }
        log.debug("Retrieving department with id: {}", departmentId);
        try {
            String url = DEPARTMENTS_SERVICE_BASE_URL + "/" + departmentId;
            log.debug("Departments-Service URL for GET by id: {}", url);
            return restTemplate.getForObject(url, DepartmentResponseModel.class);
        } catch (HttpClientErrorException ex) {
            log.debug("Error response received in getDepartmentById");
            throw handleHttpClientException(ex);
        }
    }

    public DepartmentResponseModel createDepartment(DepartmentRequestModel departmentRequest) {
        if (departmentRequest == null) {
            throw new IllegalArgumentException("DepartmentRequestModel must not be null");
        }
        log.debug("Creating a new department via DepartmentsServiceClient");
        try {
            String url = DEPARTMENTS_SERVICE_BASE_URL;
            log.debug("Departments-Service URL for POST: {}", url);
            return restTemplate.postForObject(url, departmentRequest, DepartmentResponseModel.class);
        } catch (HttpClientErrorException ex) {
            log.debug("Error response received in createDepartment");
            throw handleHttpClientException(ex);
        }
    }

    public DepartmentResponseModel updateDepartment(String departmentId, DepartmentRequestModel departmentRequest) {
        if (departmentId == null || departmentId.length() != 36) {
            throw new IllegalArgumentException("Department ID must be exactly 36 characters long");
        }
        if (departmentRequest == null) {
            throw new IllegalArgumentException("DepartmentRequestModel must not be null");
        }
        log.debug("Updating department with id: {}", departmentId);
        try {
            String url = DEPARTMENTS_SERVICE_BASE_URL + "/" + departmentId;
            log.debug("Departments-Service URL for PUT: {}", url);
            restTemplate.put(url, departmentRequest);
            return getDepartmentById(departmentId);
        } catch (HttpClientErrorException ex) {
            log.debug("Error response received in updateDepartment");
            throw handleHttpClientException(ex);
        }
    }

    public void deleteDepartment(String departmentId) {
        if (departmentId == null || departmentId.length() != 36) {
            throw new IllegalArgumentException("Department ID must be exactly 36 characters long");
        }
        log.debug("Deleting department with id: {}", departmentId);
        try {
            String url = DEPARTMENTS_SERVICE_BASE_URL + "/" + departmentId;
            log.debug("Departments-Service URL for DELETE: {}", url);
            restTemplate.delete(url);
            log.debug("Successfully deleted department with id: {}", departmentId);
        } catch (HttpClientErrorException ex) {
            log.debug("Error response received in deleteDepartment");
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
