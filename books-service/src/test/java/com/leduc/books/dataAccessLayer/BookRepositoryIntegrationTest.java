package com.leduc.books.dataAccessLayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class BookRepositoryIntegrationTest {

    @Autowired private BookRepository bookRepository;

    @BeforeEach void setup() { bookRepository.deleteAll(); }

    @Test
    public void whenBooksExists_thenReturnAllBooks() {
        Book b1 = new Book();
        BookIdentifier id1 = new BookIdentifier("42345678-1234-1234-1234-123456789012");
        Author author = new Author("Mattys","Leduc", "fghgfwertrherhtrew");
        b1.setBookIdentifier(id1);
        b1.setIsbn("97801234547-2");
        b1.setTitle("Book One");
        b1.setAuthor(author);
        b1.setGenre(GenreName.ART);
        b1.setPublicationDate(LocalDate.of(2020,1,1));
        b1.setBookType(BookType.COMIC_BOOK);
        b1.setAgeGroup(AgeGroup.ADULT);
        b1.setCopiesAvailable(7);

        Book b2 = new Book();
        BookIdentifier id2 = new BookIdentifier("52345678-1234-1234-1234-123456789012");
        b2.setBookIdentifier(id2);
        b2.setIsbn("978065432147-2");
        b2.setTitle("Book Two");
        b2.setAuthor(author);
        b2.setGenre(GenreName.ART);
        b2.setPublicationDate(LocalDate.of(2020,1,1));
        b2.setBookType(BookType.COMIC_BOOK);
        b2.setAgeGroup(AgeGroup.ADULT);
        b2.setCopiesAvailable(7);

        bookRepository.save(b1);
        bookRepository.save(b2);
        long afterSizeDB = bookRepository.count();


        List<Book> books = bookRepository.findAll();

        assertNotNull(books);
        assertNotEquals(0, afterSizeDB);
        assertEquals(afterSizeDB, books.size());
    }

    @Test
    public void whenBookExists_thenReturnBookByBookId(){
        Book b1 = new Book();
        BookIdentifier id1 = new BookIdentifier("42345678-1234-1234-1234-123456789012");
        Author author = new Author("Mattys","Leduc", "fghgfwertrherhtrew");
        b1.setBookIdentifier(id1);
        b1.setIsbn("97801234547-2");
        b1.setTitle("Book One");
        b1.setAuthor(author);
        b1.setGenre(GenreName.ART);
        b1.setPublicationDate(LocalDate.of(2020,1,1));
        b1.setBookType(BookType.COMIC_BOOK);
        b1.setAgeGroup(AgeGroup.ADULT);
        b1.setCopiesAvailable(7);

        Book b2 = new Book();
        BookIdentifier id2 = new BookIdentifier("52345678-1234-1234-1234-123456789012");
        b2.setBookIdentifier(id2);
        b2.setIsbn("978065432147-2");
        b2.setTitle("Book Two");
        b2.setAuthor(author);
        b2.setGenre(GenreName.ART);
        b2.setPublicationDate(LocalDate.of(2020,1,1));
        b2.setBookType(BookType.COMIC_BOOK);
        b2.setAgeGroup(AgeGroup.ADULT);
        b2.setCopiesAvailable(7);

        bookRepository.save(b1);
        bookRepository.save(b2);

        Book foundBook = bookRepository.findBookByBookIdentifier_BookId(
                b1.getBookIdentifier().getBookId()
        );

        assertNotNull(foundBook);
        assertEquals(b1.getBookIdentifier().getBookId(), foundBook.getBookIdentifier().getBookId());
        assertEquals(b1.getIsbn(), foundBook.getIsbn());
        assertEquals(b1.getTitle(), foundBook.getTitle());
        assertEquals(b1.getAuthor().getAuthorFirstName(), foundBook.getAuthor().getAuthorFirstName());
        assertEquals(b1.getAuthor().getAuthorLastName(), foundBook.getAuthor().getAuthorLastName());
        assertEquals(b1.getAuthor().getAuthorBiography(), foundBook.getAuthor().getAuthorBiography());
        assertEquals(b1.getGenre(), foundBook.getGenre());
        assertEquals(b1.getPublicationDate(), foundBook.getPublicationDate());
        assertEquals(b1.getBookType(), foundBook.getBookType());
        assertEquals(b1.getAgeGroup(), foundBook.getAgeGroup());
        assertEquals(b1.getCopiesAvailable(), foundBook.getCopiesAvailable());
    }


    // Negative Path

    @Test
    public void whenBookDoesNotExist_thenReturnNull(){
        final String NOT_FOUND_BOOK_ID = "c3540a89-cb47-4c96-888e-ff96708db4d8";

        Book foundBook = bookRepository.findBookByBookIdentifier_BookId(NOT_FOUND_BOOK_ID);

        assertNull(foundBook);
    }

    @Test
    public void whenBoosEntityIsValid_thenAddBook(){
        Book b1 = new Book();
        BookIdentifier id1 = new BookIdentifier("42345678-1234-1234-1234-123456789012");
        Author author = new Author("Mattys","Leduc", "fghgfwertrherhtrew");
        b1.setBookIdentifier(id1);
        b1.setIsbn("97801234547-2");
        b1.setTitle("Book One");
        b1.setAuthor(author);
        b1.setGenre(GenreName.ART);
        b1.setPublicationDate(LocalDate.of(2020,1,1));
        b1.setBookType(BookType.COMIC_BOOK);
        b1.setAgeGroup(AgeGroup.ADULT);
        b1.setCopiesAvailable(7);

        Book savedBook = bookRepository.save(b1);

        assertNotNull(savedBook);
        assertNotNull(savedBook.getId());
        assertNotNull(savedBook.getBookIdentifier());
        assertNotNull(savedBook.getBookIdentifier().getBookId());
        assertEquals(b1.getIsbn(), savedBook.getIsbn());
        assertEquals(b1.getTitle(), savedBook.getTitle());
        assertEquals(b1.getAuthor().getAuthorFirstName(), savedBook.getAuthor().getAuthorFirstName());
        assertEquals(b1.getAuthor().getAuthorLastName(), savedBook.getAuthor().getAuthorLastName());
        assertEquals(b1.getAuthor().getAuthorBiography(), savedBook.getAuthor().getAuthorBiography());
        assertEquals(b1.getGenre(), savedBook.getGenre());
        assertEquals(b1.getPublicationDate(), savedBook.getPublicationDate());
        assertEquals(b1.getBookType(), savedBook.getBookType());
        assertEquals(b1.getAgeGroup(), savedBook.getAgeGroup());
        assertEquals(b1.getCopiesAvailable(), savedBook.getCopiesAvailable());

    }

    @Test
    void whenBookRequestModelIsValid_thenUpdateBook() {
        Book b1 = new Book();
        BookIdentifier id1 = new BookIdentifier("42345678-1234-1234-1234-123456789012");
        Author author = new Author("Mattys","Leduc", "fghgfwertrherhtrew");
        b1.setBookIdentifier(id1);
        b1.setIsbn("97801234547-2");
        b1.setTitle("Book One");
        b1.setAuthor(author);
        b1.setGenre(GenreName.ART);
        b1.setPublicationDate(LocalDate.of(2020,1,1));
        b1.setBookType(BookType.COMIC_BOOK);
        b1.setAgeGroup(AgeGroup.ADULT);
        b1.setCopiesAvailable(7);

        Book savedBook = bookRepository.save(b1);
        assertNotNull(savedBook);

        String bookId = savedBook.getBookIdentifier().getBookId();

        savedBook.setBookType(BookType.EBOOK);
        savedBook.setAgeGroup(AgeGroup.CHILDREN);
        savedBook.setCopiesAvailable(10);

        Book updatedBook = bookRepository.save(savedBook);

        assertNotNull(updatedBook);
        assertEquals(bookId, updatedBook.getBookIdentifier().getBookId());
        assertEquals(BookType.EBOOK, updatedBook.getBookType());
        assertEquals(AgeGroup.CHILDREN, updatedBook.getAgeGroup());
        assertEquals(10, updatedBook.getCopiesAvailable());
    }

    @Test
    void whenBookExists_thenDeleteBook() {
        Book b1 = new Book();
        BookIdentifier id1 = new BookIdentifier("42345678-1234-1234-1234-123456789012");
        Author author = new Author("Mattys","Leduc", "fghgfwertrherhtrew");
        b1.setBookIdentifier(id1);
        b1.setIsbn("97801234547-2");
        b1.setTitle("Book One");
        b1.setAuthor(author);
        b1.setGenre(GenreName.ART);
        b1.setPublicationDate(LocalDate.of(2020,1,1));
        b1.setBookType(BookType.COMIC_BOOK);
        b1.setAgeGroup(AgeGroup.ADULT);
        b1.setCopiesAvailable(7);

        Book savedBook = bookRepository.save(b1);
        assertNotNull(savedBook);
        String bookId = savedBook.getBookIdentifier().getBookId();


        bookRepository.delete(savedBook);

        Book deletedBook = bookRepository.findBookByBookIdentifier_BookId(bookId);
        assertNull(deletedBook);
    }


    @Test
    public void whenBookIdIsNull_thenReturnNull() {
        Book foundBook = bookRepository.findBookByBookIdentifier_BookId(null);
        assertNull(foundBook);
    }

    @Test
    public void whenDeletingNull_thenThrowException() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            bookRepository.delete(null);
        });
    }

    @Test
    public void whenBookWithoutTitle_thenThrowException() {
        Book b1 = new Book();
        BookIdentifier id1 = new BookIdentifier("42345678-1234-1234-1234-123456789012");
        Author author = new Author("Mattys", "Leduc", "fghgfwertrherhtrew");
        b1.setBookIdentifier(id1);
        b1.setIsbn("97801234547-2");
        // Intentionally omit the title
        // b1.setTitle("Book One");
        b1.setAuthor(author);
        b1.setGenre(GenreName.ART);
        b1.setPublicationDate(LocalDate.of(2020, 1, 1));
        b1.setBookType(BookType.COMIC_BOOK);
        b1.setAgeGroup(AgeGroup.ADULT);
        b1.setCopiesAvailable(7);

        assertThrows(Exception.class, () -> {
            bookRepository.saveAndFlush(b1);
        });
    }

    @Test
    public void testBookConstructorSetsAllFieldsCorrectly() {
        String isbn = "9783161484100";
        String title = "Example Book Title";
        Author author = new Author("John", "Doe", "John Doe is a prolific author...");
        GenreName genreName = GenreName.FICTION;
        LocalDate publicationDate = LocalDate.of(2021, 1, 1);
        BookType bookType = BookType.PAPERBACK;
        AgeGroup ageGroup = AgeGroup.ADULT;
        Integer copiesAvailable = 5;

        Book book = new Book(isbn, title, author, genreName, publicationDate, bookType, ageGroup, copiesAvailable);

        assertNotNull(book, "Book instance should not be null");
        assertNotNull(book.getBookIdentifier(), "BookIdentifier should not be null");
        assertNotNull(book.getBookIdentifier().getBookId(), "BookId should not be null");
        assertEquals(36, book.getBookIdentifier().getBookId().length(), "BookId should be 36 characters long");

        assertEquals(isbn, book.getIsbn(), "ISBN should match the provided value");
        assertEquals(title, book.getTitle(), "Title should match the provided value");
        assertEquals(author, book.getAuthor(), "Author should match the provided instance");
        assertEquals(genreName, book.getGenre(), "GenreName should match the provided value");
        assertEquals(publicationDate, book.getPublicationDate(), "Publication date should match");
        assertEquals(bookType, book.getBookType(), "Book type should match the provided value");
        assertEquals(ageGroup, book.getAgeGroup(), "Age group should match the provided value");
        assertEquals(copiesAvailable, book.getCopiesAvailable(), "Copies available should match the provided value");
    }




}
