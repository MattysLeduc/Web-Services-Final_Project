package com.leduc.patrons.presentationLayer;

import com.leduc.patrons.dataAccessLayer.PatronPhoneNumber;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatronRequestModel {

    String firstName;
    String lastName;
    String email;
    String password;
    String memberShipType;
    List<PatronPhoneNumber> phoneNumbers;

    String streetAddress;
    String city;
    String province;
    String country;
    String postalCode;
}
