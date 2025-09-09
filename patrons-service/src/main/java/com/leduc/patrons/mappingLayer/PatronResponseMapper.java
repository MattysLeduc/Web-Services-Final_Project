package com.leduc.patrons.mappingLayer;

import com.leduc.patrons.dataAccessLayer.Patron;
import com.leduc.patrons.presentationLayer.PatronController;
import com.leduc.patrons.presentationLayer.PatronResponseModel;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Mapper(componentModel = "spring")
public interface PatronResponseMapper {

    @Mappings({
            @Mapping(source = "patron.patronIdentifier.patronId", target = "patronId"),
            @Mapping(source = "patron.firstName", target = "firstName"),
            @Mapping(source = "patron.lastName", target = "lastName"),
            @Mapping(source = "patron.email", target = "email"),
            @Mapping(source = "patron.memberShipType", target = "memberShipType"),

            @Mapping(source = "patron.patronAddress.streetAddress", target = "streetAddress"),
            @Mapping(source = "patron.patronAddress.city", target = "city"),
            @Mapping(source = "patron.patronAddress.province", target = "province"),
            @Mapping(source = "patron.patronAddress.country", target = "country"),
            @Mapping(source = "patron.patronAddress.postalCode", target = "postalCode"),
    })
    PatronResponseModel toResponseModel(Patron patron);

    List<PatronResponseModel> toResponseModelList(List<Patron> patrons);

}
