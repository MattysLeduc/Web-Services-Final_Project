package com.leduc.books.businessLayer;

import com.leduc.books.dataAccessLayer.Book;
import com.leduc.books.dataAccessLayer.BookIdentifier;
import com.leduc.books.dataAccessLayer.BookRepository;
import com.leduc.books.dataAccessLayer.LoanStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceUnitTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    @Captor
    private ArgumentCaptor<Book> bookCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void updateBookCopies_bookNotFound_returnsFalse() {
        when(bookRepository.findBookByBookIdentifier_BookId("id")).thenReturn(null);

        boolean result = bookService.updateBookCopies("id", LoanStatus.CHECKED_OUT);

        assertThat(result).isFalse();
        verify(bookRepository, never()).save(any());
    }

    @Test
    void updateBookCopies_checkedOut_withAvailableCopies_returnsTrue() {
        Book existing = new Book();
        existing.setBookIdentifier(new BookIdentifier("id"));
        existing.setCopiesAvailable(5);
        when(bookRepository.findBookByBookIdentifier_BookId("id")).thenReturn(existing);
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean result = bookService.updateBookCopies("id", LoanStatus.CHECKED_OUT);

        assertThat(result).isTrue();
        verify(bookRepository).save(bookCaptor.capture());
        assertThat(bookCaptor.getValue().getCopiesAvailable()).isEqualTo(4);
    }

    @Test
    void updateBookCopies_checkedOut_noCopies_throwsIllegalStateException() {
        Book existing = new Book();
        existing.setBookIdentifier(new BookIdentifier("id"));
        existing.setCopiesAvailable(0);
        when(bookRepository.findBookByBookIdentifier_BookId("id")).thenReturn(existing);

        assertThatThrownBy(() -> bookService.updateBookCopies("id", LoanStatus.CHECKED_OUT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No copies available to check out");
        verify(bookRepository, never()).save(any());
    }

    @Test
    void updateBookCopies_returned_incrementsCopies_returnsTrue() {
        Book existing = new Book();
        existing.setBookIdentifier(new BookIdentifier("id"));
        existing.setCopiesAvailable(2);
        when(bookRepository.findBookByBookIdentifier_BookId("id")).thenReturn(existing);
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean result = bookService.updateBookCopies("id", LoanStatus.RETURNED);

        assertThat(result).isTrue();
        verify(bookRepository).save(bookCaptor.capture());
        assertThat(bookCaptor.getValue().getCopiesAvailable()).isEqualTo(3);
    }

    @Test
    void updateBookCopies_otherStatus_returnsFalse() {
        Book existing = new Book();
        existing.setBookIdentifier(new BookIdentifier("id"));
        existing.setCopiesAvailable(2);
        when(bookRepository.findBookByBookIdentifier_BookId("id")).thenReturn(existing);

        boolean result = bookService.updateBookCopies("id", null);

        assertThat(result).isFalse();
        verify(bookRepository, never()).save(any());
    }
}