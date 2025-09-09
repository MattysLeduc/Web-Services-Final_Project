package com.leduc.loans.domainclientLayer.books;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leduc.loans.dataAccessLayer.LoanStatus;
import com.leduc.loans.utils.HttpErrorInfo;
import com.leduc.loans.utils.exceptions.InvalidInputException;
import com.leduc.loans.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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
public class BooksServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String BOOKS_SERVICE_BASE_URL;

    public BooksServiceClient(RestTemplate restTemplate,
                              ObjectMapper mapper,
                              @Value("${app.books-service.host}") String booksServiceHost,
                              @Value("${app.books-service.port}") String booksServicePort) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.BOOKS_SERVICE_BASE_URL = "http://" + booksServiceHost + ":" + booksServicePort + "/api/v1/books";
    }

    public List<BookModel> getAllBooks() {
        log.debug("Retrieving all books via BooksServiceClient");
        try {
            String url = BOOKS_SERVICE_BASE_URL;
            log.debug("Books-Service URL for GET all: {}", url);
            BookModel[] array = restTemplate.getForObject(url, BookModel[].class);
            return array != null ? Arrays.asList(array) : Collections.emptyList();
        } catch (HttpClientErrorException ex) {
            log.debug("Error response received in getAllBooks: {}", ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }

    public BookModel getBookByBookId(String bookId) {
        if (bookId == null || bookId.length() != 36) {
            throw new InvalidInputException("Book ID must be exactly 36 characters long");
        }
        log.debug("Retrieving book via BooksServiceClient for id: {}", bookId);
        try {
            String url = BOOKS_SERVICE_BASE_URL + "/" + bookId;
            log.debug("Books-Service URL for GET by id: {}", url);
            return restTemplate.getForObject(url, BookModel.class);
        } catch (HttpClientErrorException ex) {
            log.debug("Error response in getBookByBookId: {}", ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }


    public BookModel createBook(BookModel bookRequest) {
        if (bookRequest == null) {
            throw new IllegalArgumentException("BookModel must not be null");
        }
        log.debug("Creating book via BooksServiceClient");
        try {
            String url = BOOKS_SERVICE_BASE_URL;
            log.debug("Books-Service URL for POST: {}", url);
            return restTemplate.postForObject(url, bookRequest, BookModel.class);
        } catch (HttpClientErrorException ex) {
            log.debug("Error response in createBook: {}", ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }

    public BookModel updateBook(String bookId, BookModel bookRequest) {
        if (bookId == null || bookId.length() != 36) {
            throw new IllegalArgumentException("Book ID must be exactly 36 characters long");
        }
        if (bookRequest == null) {
            throw new IllegalArgumentException("BookModel must not be null");
        }
        log.debug("Updating book via BooksServiceClient for id: {}", bookId);
        try {
            String url = BOOKS_SERVICE_BASE_URL + "/" + bookId;
            log.debug("Books-Service URL for PUT: {}", url);
            restTemplate.put(url, bookRequest);
            return getBookByBookId(bookId);
        } catch (HttpClientErrorException ex) {
            log.debug("Error response in updateBook: {}", ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }

    public void deleteBook(String bookId) {
        if (bookId == null || bookId.length() != 36) {
            throw new IllegalArgumentException("Book ID must be exactly 36 characters long");
        }
        log.debug("Deleting book via BooksServiceClient for id: {}", bookId);
        try {
            String url = BOOKS_SERVICE_BASE_URL + "/" + bookId;
            log.debug("Books-Service URL for DELETE: {}", url);
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            log.debug("Error response in deleteBook: {}", ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }

    public void patchBookCopiesAvailableByBookId(String bookId, LoanStatus status) {
        String url = String.format("%s/%s/copiesAvailable?status=%s",
                BOOKS_SERVICE_BASE_URL, bookId, status.name());

        try {
            restTemplate.exchange(url,
                    HttpMethod.PATCH,
                    HttpEntity.EMPTY,
                    Void.class);
        } catch (HttpClientErrorException ex) {
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
        log.warn("Unexpected HTTP error: {}. Body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return ex;
    }
}
