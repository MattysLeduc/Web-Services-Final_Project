package com.leduc.books.dataAccessLayer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Integer> {
 Book findBookByBookIdentifier_BookId(String bookId);
 Book findByIsbn(String isbn);
}
