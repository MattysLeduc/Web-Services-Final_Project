package com.leduc.patrons.mappingLayer;

import com.leduc.patrons.dataAccessLayer.Patron;
import com.leduc.patrons.dataAccessLayer.PatronIdentifier;
import com.leduc.patrons.dataAccessLayer.PatronPhoneNumber;
import com.leduc.patrons.presentationLayer.PatronRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface PatronRequestMapper {

    @Mappings({
            @Mapping(source = "patronIdentifier", target = "patronIdentifier"),
            @Mapping(source = "patronRequestModel.firstName", target = "firstName"),
            @Mapping(source = "patronRequestModel.lastName", target = "lastName"),
            @Mapping(source = "patronRequestModel.email", target = "email"),
            @Mapping(source = "patronRequestModel.password", target = "password"),
            @Mapping(source = "patronRequestModel.memberShipType", target = "memberShipType"),

            // Mapping Address fields
            @Mapping(source = "patronRequestModel.streetAddress", target = "patronAddress.streetAddress"),
            @Mapping(source = "patronRequestModel.city", target = "patronAddress.city"),
            @Mapping(source = "patronRequestModel.province", target = "patronAddress.province"),
            @Mapping(source = "patronRequestModel.country", target = "patronAddress.country"),
            @Mapping(source = "patronRequestModel.postalCode", target = "patronAddress.postalCode"),

            @Mapping(source = "patronRequestModel.phoneNumbers", target = "phoneNumbers")
    })
    Patron toEntity(PatronRequestModel patronRequestModel, PatronIdentifier patronIdentifier, PatronPhoneNumber patronPhoneNumber);
}
