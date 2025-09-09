package com.leduc.books.dataAccessLayer;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor
@Builder
public class Author {

    private String authorFirstName;
    private String authorLastName;
    private String authorBiography;

    public Author(String authorFirstName, String authorLastName, String authorBiography) {
        this.authorFirstName = authorFirstName;
        this.authorLastName = authorLastName;
        this.authorBiography = authorBiography;
    }

}
