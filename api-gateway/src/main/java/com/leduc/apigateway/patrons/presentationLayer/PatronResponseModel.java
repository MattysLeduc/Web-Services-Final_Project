package com.leduc.apigateway.patrons.presentationLayer;


import com.leduc.apigateway.patrons.domainclientLayer.PatronPhoneNumber;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatronResponseModel extends RepresentationModel<PatronResponseModel> {

    String patronId;
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
