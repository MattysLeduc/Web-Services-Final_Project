package com.leduc.apigateway.patrons.domainclientLayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leduc.apigateway.patrons.presentationLayer.PatronResponseModel;
import com.leduc.apigateway.patrons.presentationLayer.PatronRequestModel;
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
public class PatronsServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String PATRONS_SERVICE_BASE_URL;

    public PatronsServiceClient(RestTemplate restTemplate, ObjectMapper mapper,
                                @Value("${app.patrons-service.host}") String patronServiceHost,
                                @Value("${app.patrons-service.port}") String patronServicePort) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        PATRONS_SERVICE_BASE_URL = "http://" + patronServiceHost + ":" + patronServicePort + "/api/v1/patrons";
    }

    public List<PatronResponseModel> getAllPatrons() {
        log.debug("Retrieving all patrons via PatronsServiceClient");
        try {
            String url = PATRONS_SERVICE_BASE_URL;
            log.debug("Patrons-Service URL for GET all: " + url);
            PatronResponseModel[] array = restTemplate.getForObject(url, PatronResponseModel[].class);
            log.debug("Successfully retrieved {} patrons", array != null ? array.length : 0);
            return array != null ? Arrays.asList(array) : Collections.emptyList();
        } catch (HttpClientErrorException ex) {
            log.debug("Error response received in getAllPatrons");
            throw handleHttpClientException(ex);
        }
    }

    public PatronResponseModel getPatronByPatronId(String patronId) {
        if (patronId == null || patronId.length() != 36) {
            throw new IllegalArgumentException("Patron ID must be exactly 36 characters long");
        }
        log.debug("Retrieving patron via PatronsServiceClient for id: {}", patronId);
        try {
            String url = PATRONS_SERVICE_BASE_URL + "/" + patronId;
            log.debug("Patrons-Service URL: " + url);
            PatronResponseModel response = restTemplate.getForObject(url, PatronResponseModel.class);
            log.debug("Successfully retrieved patron with id: {}", response.getPatronId());
            return response;
        } catch (HttpClientErrorException ex) {
            log.debug("Error response in getPatronByPatronId");
            throw handleHttpClientException(ex);
        }
    }

    public PatronResponseModel createPatron(PatronRequestModel patronRequest) {
        if (patronRequest == null) {
            throw new IllegalArgumentException("PatronRequestModel must not be null");
        }
        log.debug("Creating patron via PatronsServiceClient");
        try {
            String url = PATRONS_SERVICE_BASE_URL;
            log.debug("Patrons-Service URL for POST: " + url);
            PatronResponseModel response = restTemplate.postForObject(url, patronRequest, PatronResponseModel.class);
            log.debug("Successfully created patron with id: {}", response != null ? response.getPatronId() : "unknown");
            return response;
        } catch (HttpClientErrorException ex) {
            log.debug("Error response in createPatron");
            throw handleHttpClientException(ex);
        }
    }

    public PatronResponseModel updatePatron(String patronId, PatronRequestModel patronRequest) {
        if (patronId == null || patronId.length() != 36) {
            throw new IllegalArgumentException("Patron ID must be exactly 36 characters long");
        }
        if (patronRequest == null) {
            throw new IllegalArgumentException("PatronRequestModel must not be null");
        }
        log.debug("Updating patron via PatronsServiceClient for id: {}", patronId);
        try {
            String url = PATRONS_SERVICE_BASE_URL + "/" + patronId;
            log.debug("Patrons-Service URL for PUT: " + url);
            restTemplate.put(url, patronRequest);
            return getPatronByPatronId(patronId);
        } catch (HttpClientErrorException ex) {
            log.debug("Error response in updatePatron");
            throw handleHttpClientException(ex);
        }
    }

    public void deletePatron(String patronId) {
        if (patronId == null || patronId.length() != 36) {
            throw new IllegalArgumentException("Patron ID must be exactly 36 characters long");
        }
        log.debug("Deleting patron via PatronsServiceClient for id: {}", patronId);
        try {
            String url = PATRONS_SERVICE_BASE_URL + "/" + patronId;
            log.debug("Patrons-Service URL for DELETE: " + url);
            restTemplate.delete(url);
            log.debug("Successfully deleted patron with id: {}", patronId);
        } catch (HttpClientErrorException ex) {
            log.debug("Error response in deletePatron");
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
