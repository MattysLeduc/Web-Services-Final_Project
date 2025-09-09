package com.leduc.loans.dataAccessLayer;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Getter
@AllArgsConstructor
public class LoanIdentifier {
    private String loanId;


    public LoanIdentifier() {
        this.loanId = UUID.randomUUID().toString();
    }
}
