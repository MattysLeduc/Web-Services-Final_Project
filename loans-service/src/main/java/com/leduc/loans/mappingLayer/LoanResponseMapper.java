package com.leduc.loans.mappingLayer;

import com.leduc.loans.dataAccessLayer.Loan;
import com.leduc.loans.presentationLayer.LoanController;
import com.leduc.loans.presentationLayer.LoanResponseModel;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Mapper(componentModel = "spring")
public interface LoanResponseMapper {

    @Mapping(source = "loanIdentifier.loanId",               target = "loanId")
    @Mapping(source = "patronModel.patronId", target = "patronId")
    @Mapping(source = "patronModel.firstName",              target = "patronFirstName")
    @Mapping(source = "patronModel.lastName",               target = "patronLastName")

    @Mapping(source = "employeeModel.employeeId", target = "employeeId")
    @Mapping(source = "employeeModel.firstName",            target = "employeeFirstName")
    @Mapping(source = "employeeModel.lastName",             target = "employeeLastName")

    @Mapping(source = "bookModel.bookId",    target = "bookId")
    @Mapping(source = "bookModel.isbn",                     target = "isbn")
    @Mapping(source = "bookModel.title",                    target = "title")
    @Mapping(source = "bookModel.authorFirstName",                    target = "authorFirstName")
    @Mapping(source = "bookModel.authorLastName",                    target = "authorLastName")
    @Mapping(source = "bookModel.genre",                    target = "genre")
    @Mapping(source = "bookModel.bookType",                 target = "bookType")

    @Mapping(source = "issueDate",                          target = "issueDate")
    @Mapping(source = "checkoutDate",                       target = "checkoutDate")
    @Mapping(source = "returnDate",                         target = "returnDate")
    @Mapping(source = "status",                             target = "status")
    LoanResponseModel toResponse(Loan loan);

    List<LoanResponseModel> loanEntityListToLoanResponseModelList(List<Loan> loans);

}
