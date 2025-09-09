package com.leduc.apigateway.loans.domainclientLayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leduc.apigateway.loans.presentationLayer.LoanRequestModel;
import com.leduc.apigateway.loans.presentationLayer.LoanResponseModel;
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
public class LoansServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String LOANS_SERVICE_BASE_URL;

    public LoansServiceClient(RestTemplate restTemplate,
                              ObjectMapper mapper,
                              @Value("${app.loans-service.host}") String loansServiceHost,
                              @Value("${app.loans-service.port}") String loansServicePort) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.LOANS_SERVICE_BASE_URL =
                "http://" + loansServiceHost + ":" + loansServicePort + "/api/v1/patrons";
    }

    public List<LoanResponseModel> getAllLoans(String patronId) {
        log.debug("Retrieving all loans for patron {} via LoansServiceClient", patronId);
        if (patronId == null || patronId.length() != 36) {
            throw new InvalidInputException("Patron ID must be exactly 36 characters long");
        }
        try {
            String url = LOANS_SERVICE_BASE_URL + "/" + patronId + "/loans";
            LoanResponseModel[] array = restTemplate.getForObject(url, LoanResponseModel[].class);
            return array != null ? Arrays.asList(array) : Collections.emptyList();
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public LoanResponseModel getLoanById(String patronId, String loanId) {
        log.debug("Retrieving loan {} for patron {}", loanId, patronId);
        if (patronId == null || patronId.length() != 36 || loanId == null || loanId.length() != 36) {
            throw new InvalidInputException("Invalid ID");
        }
        try {
            String url = LOANS_SERVICE_BASE_URL + "/" + patronId + "/loans/" + loanId;
            return restTemplate.getForObject(url, LoanResponseModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public LoanResponseModel createLoan(String patronId, LoanRequestModel loanRequest) {
        log.debug("Creating loan for patron {}", patronId);
        if (loanRequest == null) {
            throw new IllegalArgumentException("LoanRequestModel must not be null");
        }
        try {
            String url = LOANS_SERVICE_BASE_URL + "/" + patronId + "/loans";
            return restTemplate.postForObject(url, loanRequest, LoanResponseModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public LoanResponseModel updateLoan(String patronId, String loanId, LoanRequestModel loanRequest) {
        log.debug("Updating loan {} for patron {}", loanId, patronId);
        if (loanRequest == null) {
            throw new IllegalArgumentException("LoanRequestModel must not be null");
        }
        try {
            String url = LOANS_SERVICE_BASE_URL + "/" + patronId + "/loans/" + loanId;
            restTemplate.put(url, loanRequest);
            return getLoanById(patronId, loanId);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public void deleteLoan(String patronId, String loanId) {
        log.debug("Deleting loan {} for patron {}", loanId, patronId);
        try {
            String url = LOANS_SERVICE_BASE_URL + "/" + patronId + "/loans/" + loanId;
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        if (ex.getStatusCode() == NOT_FOUND) {
            return new NotFoundException(getErrorMessage(ex));
        }
        if (ex.getStatusCode() == UNPROCESSABLE_ENTITY) {
            return new InvalidInputException(getErrorMessage(ex));
        }
        log.warn("Unexpected HTTP error: {}. Body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return ex;
    }
}

