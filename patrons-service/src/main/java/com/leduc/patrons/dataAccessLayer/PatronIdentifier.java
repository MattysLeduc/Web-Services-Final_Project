package com.leduc.patrons.dataAccessLayer;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Embeddable
@Getter
@AllArgsConstructor
public class PatronIdentifier {
    private String patronId;


    public PatronIdentifier() {
        this.patronId = UUID.randomUUID().toString();
    }
}
