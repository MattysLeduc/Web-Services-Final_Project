package com.leduc.books.dataAccessLayer;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Embeddable
@Getter
@AllArgsConstructor
public class BookIdentifier {
    private String bookId;

    public BookIdentifier() {
        this.bookId = UUID.randomUUID().toString();
    }
}
