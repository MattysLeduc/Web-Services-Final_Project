package com.leduc.loans.domainclientLayer.books;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class BookModel {
    String bookId;
    String isbn;
    String title;
    String authorFirstName;
    String authorLastName;
    String bookType;
    String genre;

    public BookModel(String isbn, String title, String authorFirstName, String authorLastName, String genre, String bookType) {

    }
}
