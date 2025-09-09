package com.leduc.books.presentationLayer;

import com.leduc.books.dataAccessLayer.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Sql({"/data-h2.sql"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookControllerIntegrationTest {


    @Autowired
    private WebTestClient webClient;

    @Autowired
    private BookRepository bookRepository;

    private final String BASE_URI_BOOKS = "/api/v1/books";

    private final String VALID_BOOK_ID = "6fa459ea-ee8a-3ca4-894e-d6f1d55b4f2a";

    private final String INVALID_BOOK_ID = "6fa459ea-ee8a-3ca4-894e-d6f1d55b4f2";

    @Test
    public void whenBooksExist_thenReturnAllBooks() {
        long sizeDb = bookRepository.count();

        webClient.get()
                .uri(BASE_URI_BOOKS)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(BookResponseModel.class)
                .value((books) -> {
                    assertNotNull(books);
                    assertNotEquals(0, books.size());
                    assertEquals(sizeDb, books.size());
                });
    }

    @Test
    public void whenBookIdIsValid_thenReturnBook() {
        webClient.get()
                .uri(BASE_URI_BOOKS + "/" + VALID_BOOK_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BookResponseModel.class)
                .value((bookResponseModel) -> {
                    assertNotNull(bookResponseModel);
                    assertNotNull(bookResponseModel.getBookId());
                });
    }

    @Test
    public void whenGetBookById_whenInvalidIdLength_thenReturnNotFound() {
        String invalidBookId = "12345";
        webClient.get()
                .uri(BASE_URI_BOOKS + "/" + invalidBookId)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    public void whenBookCreateRequestIsValid_thenReturnBook() {
        BookRequestModel bookRequest = BookRequestModel.builder()
                .isbn("9780132350884")
                .title("Clean Code")
                .authorFirstName("Robert")
                .authorLastName("Martin")
                .authorBiography("Software craftsmanship expert")
                .genre("FICTION")
                .publicationDate(LocalDate.of(2008, 8, 1))
                .bookType("PAPERBACK")
                .ageGroup("ADULT")
                .copiesAvailable(7)
                .build();
        webClient.post()
                .uri(BASE_URI_BOOKS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(bookRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BookResponseModel.class)
                .value((bookResponseModel) -> {
                    assertNotNull(bookResponseModel);
                    assertNotNull(bookResponseModel.getBookId());
                    assertEquals(bookRequest.getIsbn(), bookResponseModel.getIsbn());
                    assertEquals(bookRequest.getTitle(), bookResponseModel.getTitle());
                    assertEquals(bookRequest.getAuthorFirstName(), bookResponseModel.getAuthorFirstName());
                    assertEquals(bookRequest.getAuthorLastName(), bookResponseModel.getAuthorLastName());
                    assertEquals(bookRequest.getAuthorBiography(), bookResponseModel.getAuthorBiography());
                    assertEquals(bookRequest.getGenre(), bookResponseModel.getGenre());
                    assertEquals(bookRequest.getPublicationDate(), bookResponseModel.getPublicationDate());
                    assertEquals(bookRequest.getAgeGroup(), bookResponseModel.getAgeGroup());
                    assertEquals(bookRequest.getCopiesAvailable(), bookResponseModel.getCopiesAvailable());
                    assertEquals(bookRequest.getBookType(), bookResponseModel.getBookType());
                });
    }

    @Test
    public void whenBookUpdateRequestIsValid_thenReturnBook() {
        BookRequestModel createRequest = BookRequestModel.builder()
                .isbn("9780321125217")
                .title("Refactoring")
                .authorFirstName("Martin")
                .authorLastName("Fowler")
                .authorBiography("Refactoring expert")
                .genre("FICTION")
                .publicationDate(LocalDate.of(1999, 7, 8))
                .bookType("HARDCOVER")
                .ageGroup("ADULT")
                .copiesAvailable(3)
                .build();

        BookResponseModel createdBook = webClient.post()
                .uri(BASE_URI_BOOKS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BookResponseModel.class)
                .returnResult()
                .getResponseBody();
        assertNotNull(createdBook);
        String bookId = createdBook.getBookId();

        BookRequestModel updateRequest = BookRequestModel.builder()
                .isbn("9780321125218") // updated
                .title("Refactoring: Improving the Design of Existing Code")
                .authorFirstName(createdBook.getAuthorFirstName())
                .authorLastName(createdBook.getAuthorLastName())
                .authorBiography(createdBook.getAuthorBiography())
                .genre(createdBook.getGenre())
                .publicationDate(createdBook.getPublicationDate())
                .bookType(createdBook.getBookType())
                .ageGroup(createdBook.getAgeGroup())
                .copiesAvailable(5)
                .build();

        webClient.put()
                .uri(BASE_URI_BOOKS + "/" + bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BookResponseModel.class)
                .value(updatedBook -> {
                    assertNotNull(updatedBook);
                    assertEquals(bookId, updatedBook.getBookId());
                    assertEquals("Refactoring: Improving the Design of Existing Code", updatedBook.getTitle());
                    assertEquals("9780321125218", updatedBook.getIsbn());
                    assertEquals(createdBook.getAuthorFirstName(), updatedBook.getAuthorFirstName());
                    assertEquals(createdBook.getAuthorLastName(), updatedBook.getAuthorLastName());
                    assertEquals(createdBook.getAuthorBiography(), updatedBook.getAuthorBiography());
                    assertEquals(createdBook.getPublicationDate(), updatedBook.getPublicationDate());
                    assertEquals(createdBook.getGenre(), updatedBook.getGenre());
                    assertEquals(Integer.valueOf(5), updatedBook.getCopiesAvailable());
                });
    }

    @Test
    public void whenBookExistsOnDelete_thenReturnNoContent(){
        webClient.delete()
                .uri(BASE_URI_BOOKS + "/" + VALID_BOOK_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        webClient.get()
                .uri(BASE_URI_BOOKS + "/" + VALID_BOOK_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound() // Supposed to be 404
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    @Test
    public void whenBookIdIsInvalidOnDelete_thenReturnUnprocessableEntity(){
        webClient.delete()
                .uri(BASE_URI_BOOKS + "/" + INVALID_BOOK_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    @Test
    public void whenGetBookById_withNonExistentValidId_thenReturnNotFound() {
        String nonExistentBookId = "11111111-1111-1111-1111-111111111111";
        webClient.get()
                .uri(BASE_URI_BOOKS + "/" + nonExistentBookId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void whenBookCreateRequest_isNull_thenReturnBadRequest() {
        BookRequestModel bookRequestModel = new BookRequestModel();
        webClient.post()
                .uri(BASE_URI_BOOKS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(bookRequestModel)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    public void whenBookCreateRequest_missingTitle_thenReturnError() {
        BookRequestModel bookRequest = BookRequestModel.builder()
                .isbn("9780132350884")
                .title(null) // missing title
                .authorFirstName("Robert")
                .authorLastName("Martin")
                .authorBiography("Software craftsmanship expert")
                .genre("FICTION")
                .publicationDate(LocalDate.of(2008, 8, 1))
                .bookType("PAPERBACK")
                .ageGroup("ADULT")
                .copiesAvailable(7)
                .build();

        webClient.post()
                .uri(BASE_URI_BOOKS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(bookRequest)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    public void whenBookCreateRequest_duplicateIsbn_thenReturnError() {
        // First create a book with a given ISBN.
        BookRequestModel bookRequest = BookRequestModel.builder()
                .isbn("9780132350884")
                .title("Clean Code")
                .authorFirstName("Robert")
                .authorLastName("Martin")
                .authorBiography("Software craftsmanship expert")
                .genre("FICTION")
                .publicationDate(LocalDate.of(2008, 8, 1))
                .bookType("PAPERBACK")
                .ageGroup("ADULT")
                .copiesAvailable(7)
                .build();
        webClient.post()
                .uri(BASE_URI_BOOKS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(bookRequest)
                .exchange()
                .expectStatus().isCreated();
        BookRequestModel duplicateRequest = BookRequestModel.builder()
                .isbn("9780132350884")
                .title("Clean Code Duplicate")
                .authorFirstName("Robert")
                .authorLastName("Martin")
                .authorBiography("Software craftsmanship expert")
                .genre("FICTION")
                .publicationDate(LocalDate.of(2008, 8, 1))
                .bookType("PAPERBACK")
                .ageGroup("ADULT")
                .copiesAvailable(7)
                .build();
        webClient.post()
                .uri(BASE_URI_BOOKS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(duplicateRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void whenBookUpdateRequest_withInvalidBookIdLength_thenReturnError() {
        String invalidBookId = "12345";
        BookRequestModel updateRequest = BookRequestModel.builder()
                .title("Updated Title")
                .build();
        webClient.put()
                .uri(BASE_URI_BOOKS + "/" + invalidBookId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    public void whenBookUpdateRequest_withNonExistentBook_thenReturnNotFound() {
        String nonExistentBookId = "11111111-1111-1111-1111-111111111111";
        BookRequestModel updateRequest = BookRequestModel.builder()
                .title("Updated Title")
                .build();
        webClient.put()
                .uri(BASE_URI_BOOKS + "/" + nonExistentBookId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void whenBookDelete_withNonExistentBook_thenReturnNotFound() {
        String nonExistentBookId = "11111111-1111-1111-1111-111111111111";
        webClient.delete()
                .uri(BASE_URI_BOOKS + "/" + nonExistentBookId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }


}