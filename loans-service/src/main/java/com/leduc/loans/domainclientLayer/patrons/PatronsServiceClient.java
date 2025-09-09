package com.leduc.loans.domainclientLayer.patrons;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leduc.loans.utils.HttpErrorInfo;
import com.leduc.loans.utils.exceptions.InvalidInputException;
import com.leduc.loans.utils.exceptions.NotFoundException;
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

    public List<PatronModel> getAllPatrons() {
        log.debug("Retrieving all patrons via PatronsServiceClient");
        try {
            String url = PATRONS_SERVICE_BASE_URL;
            log.debug("Patrons-Service URL for GET all: " + url);
            PatronModel[] array = restTemplate.getForObject(url, PatronModel[].class);
            log.debug("Successfully retrieved {} patrons", array != null ? array.length : 0);
            return array != null ? Arrays.asList(array) : Collections.emptyList();
        } catch (HttpClientErrorException ex) {
            log.debug("Error response received in getAllPatrons");
            throw handleHttpClientException(ex);
        }
    }

    public PatronModel getPatronByPatronId(String patronId) {
        if (patronId == null || patronId.length() != 36) {
            throw new InvalidInputException("Patron ID must be exactly 36 characters long");
        }
        log.debug("Retrieving patron via PatronsServiceClient for id: {}", patronId);
        try {
            String url = PATRONS_SERVICE_BASE_URL + "/" + patronId;
            log.debug("Patrons-Service URL: " + url);
            PatronModel response = restTemplate.getForObject(url, PatronModel.class);
            log.debug("Successfully retrieved patron with id: {}", response.getPatronId());
            return response;
        } catch (HttpClientErrorException ex) {
            log.debug("Error response in getPatronByPatronId");
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
