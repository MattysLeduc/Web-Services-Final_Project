package com.leduc.books.mappingLayer;


import com.leduc.books.dataAccessLayer.Book;
import com.leduc.books.dataAccessLayer.BookIdentifier;
import com.leduc.books.presentationLayer.BookRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface BookRequestMapper {

    @Mappings({
            @Mapping(source = "bookIdentifier", target = "bookIdentifier"),
            @Mapping(source = "bookRequestModel.isbn", target = "isbn"),
            @Mapping(source = "bookRequestModel.title", target = "title"),
            @Mapping(source = "bookRequestModel.genre", target = "genre"),
            @Mapping(source = "bookRequestModel.bookType", target = "bookType"),
            @Mapping(source = "bookRequestModel.ageGroup", target = "ageGroup"),
            @Mapping(source = "bookRequestModel.publicationDate", target = "publicationDate"),
            @Mapping(source = "bookRequestModel.copiesAvailable", target = "copiesAvailable"),

            // Mapping Author fields separately
            @Mapping(target = "author.authorFirstName", source = "bookRequestModel.authorFirstName"),
            @Mapping(target = "author.authorLastName", source = "bookRequestModel.authorLastName"),
            @Mapping(target = "author.authorBiography", source = "bookRequestModel.authorBiography"),

            @Mapping(target = "id", ignore = true)
    })
    Book toEntity(BookRequestModel bookRequestModel, BookIdentifier bookIdentifier);

}