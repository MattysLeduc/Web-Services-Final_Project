package com.leduc.apigateway.books.presentationLayer;

import com.leduc.apigateway.books.businessLayer.BooksService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("api/v1/books")
public class BooksController {

    private final BooksService booksService;

    public BooksController(BooksService booksService) {
        this.booksService = booksService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<BookResponseModel>> getAllBooks() {
        List<BookResponseModel> books = booksService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    @GetMapping(value = "/{bookId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookResponseModel> getBookById(@PathVariable String bookId) {
        log.debug("Request received in API-Gateway Books Controller: getBookById");
        BookResponseModel book = booksService.getBookById(bookId);
        return ResponseEntity.ok(book);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookResponseModel> createBook(@RequestBody BookRequestModel bookRequest) {
        log.debug("Request received in API-Gateway Books Controller: createBook");
        BookResponseModel createdBook = booksService.createBook(bookRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    @PutMapping(value = "/{bookId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookResponseModel> updateBook(@PathVariable String bookId,
                                                        @RequestBody BookRequestModel bookRequest) {
        log.debug("Request received in API-Gateway Books Controller: updateBook");
        BookResponseModel updatedBook = booksService.updateBook(bookId, bookRequest);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping(value = "/{bookId}")
    public ResponseEntity<Void> deleteBook(@PathVariable String bookId) {
        log.debug("Request received in API-Gateway Books Controller: deleteBook");
        booksService.deleteBook(bookId);
        return ResponseEntity.noContent().build();
    }
}
