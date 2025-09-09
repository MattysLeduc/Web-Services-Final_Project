package com.leduc.books.presentationLayer;

import com.leduc.books.businessLayer.BookService;
import com.leduc.books.dataAccessLayer.LoanStatus;
import com.leduc.books.utils.exceptions.InvalidInputException;
import com.leduc.books.utils.exceptions.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<BookResponseModel> getAllBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookResponseModel> getBookById(@PathVariable String bookId) {
        if (bookId == null || bookId.length() != 36) {
            throw new NotFoundException("Book ID must be exactly 36 characters long");
        }
        return ResponseEntity.ok(bookService.getBookById(bookId));
    }

    @PostMapping
    public ResponseEntity<BookResponseModel> addBook(@RequestBody BookRequestModel bookRequestModel) {
        BookResponseModel createdBook = bookService.createBook(bookRequestModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<BookResponseModel> updateBook(
            @PathVariable String bookId,
            @RequestBody BookRequestModel bookRequestModel) {
        if (bookId == null || bookId.length() != 36) {
            throw new InvalidInputException("Book ID must be exactly 36 characters long");
        }
        BookResponseModel updatedBook = bookService.updateBook(bookId, bookRequestModel);
        return ResponseEntity.ok(updatedBook);
    }

    @PatchMapping("/{bookId}/copiesAvailable")
    public ResponseEntity<Void> adjustCopies(
            @PathVariable String bookId,
            @RequestParam LoanStatus status
    ) {
        boolean ok = bookService.updateBookCopies(bookId, status);
        if (!ok) {
            throw new NotFoundException("Book with ID '" + bookId + "' not found");
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteBook(@PathVariable String bookId) {
        if (bookId == null || bookId.length() != 36) {
            throw new InvalidInputException("Book ID must be exactly 36 characters long");
        }
        bookService.deleteBook(bookId);
        return ResponseEntity.noContent().build();
    }
}
