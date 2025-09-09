package com.leduc.books.dataAccessLayer;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;


import java.time.LocalDate;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    private BookIdentifier bookIdentifier;

    @NotNull
    private String isbn;

    @NotNull
    private String title;

    @Embedded
    private Author author;

    @Column(name = "genre_name")
    @Enumerated(EnumType.STRING)
    private GenreName genre;

    private LocalDate publicationDate;

    @Enumerated(EnumType.STRING)
    private BookType bookType;

    @Enumerated(EnumType.STRING)
    private AgeGroup ageGroup;

    private Integer copiesAvailable;

    public Book(String isbn, String title, Author author, GenreName genreName,
                LocalDate publicationDate, BookType bookType, AgeGroup ageGroup,
                Integer copiesAvailable) {
        this.bookIdentifier = new BookIdentifier();
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.genre = genreName;
        this.publicationDate = publicationDate;
        this.bookType = bookType;
        this.ageGroup = ageGroup;
        this.copiesAvailable = copiesAvailable;
    }
}
