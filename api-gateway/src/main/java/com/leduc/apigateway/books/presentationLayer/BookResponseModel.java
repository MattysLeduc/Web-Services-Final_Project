package com.leduc.apigateway.books.presentationLayer;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookResponseModel extends RepresentationModel<BookResponseModel> {

    String bookId;
    String isbn;
    String title;
    String authorFirstName;
    String authorLastName;
    String authorBiography;
    String genre;
    LocalDate publicationDate;
    String bookType;
    String ageGroup;
    Integer copiesAvailable;
}
