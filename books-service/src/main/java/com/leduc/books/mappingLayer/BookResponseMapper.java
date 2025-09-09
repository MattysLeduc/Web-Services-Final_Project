package com.leduc.books.mappingLayer;


import com.leduc.books.dataAccessLayer.Book;
import com.leduc.books.presentationLayer.BookController;
import com.leduc.books.presentationLayer.BookResponseModel;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Mapper(componentModel = "spring")
public interface BookResponseMapper {

    @Mappings({
            @Mapping(target = "bookId", source = "bookIdentifier.bookId"),
            @Mapping(target = "authorFirstName", source = "author.authorFirstName"),
            @Mapping(target = "authorLastName", source = "author.authorLastName"),
            @Mapping(target = "authorBiography", source = "author.authorBiography"),
            @Mapping(target = "genre", source = "genre"),
            @Mapping(target = "bookType", source = "bookType"),
            @Mapping(target = "ageGroup", source = "ageGroup"),
            @Mapping(target = "publicationDate", source = "publicationDate"),
            @Mapping(target = "isbn", source = "isbn"),
            @Mapping(target = "title", source = "title"),
            @Mapping(target = "copiesAvailable", source = "copiesAvailable")
    })
    BookResponseModel entityToResponseModel(Book book);

    List<BookResponseModel> entityToResponseModelList(List<Book> books);


}
