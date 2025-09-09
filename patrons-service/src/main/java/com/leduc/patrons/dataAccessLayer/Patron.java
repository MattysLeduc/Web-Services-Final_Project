package com.leduc.patrons.dataAccessLayer;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Entity
@Table(name = "patrons")
@Data
@NoArgsConstructor
public class Patron {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    private PatronIdentifier patronIdentifier;

    private String firstName;

    private String lastName;

    @Column(name = "email_address", nullable = false)
    private String email;

    private String password;

    @ElementCollection
    @CollectionTable(name = "patron_phonenumbers", joinColumns = @JoinColumn(name = "patron_id"))
    private List<PatronPhoneNumber> phoneNumbers;

    @Embedded
    private PatronAddress patronAddress;

    @Enumerated(EnumType.STRING)
    private MemberShipType memberShipType;

    public Patron(String firstName, String lastName,
                  String email, String password, List<PatronPhoneNumber> phoneNumbersList,
                  PatronAddress patronAddress, MemberShipType memberShipType) {

        this.patronIdentifier = new PatronIdentifier();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phoneNumbers = phoneNumbersList;
        this.patronAddress = patronAddress;
        this.memberShipType = memberShipType;
    }
}
