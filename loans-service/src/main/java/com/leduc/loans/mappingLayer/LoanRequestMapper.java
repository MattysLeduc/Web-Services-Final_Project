package com.leduc.loans.mappingLayer;

import com.leduc.loans.dataAccessLayer.Loan;
import com.leduc.loans.dataAccessLayer.LoanIdentifier;
import com.leduc.loans.domainclientLayer.books.BookModel;
import com.leduc.loans.domainclientLayer.patrons.PatronModel;
import com.leduc.loans.domainclientLayer.staff.EmployeeModel;
import com.leduc.loans.presentationLayer.LoanRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanRequestMapper {

    @Mapping(source = "loanRequestModel.issueDate", target = "issueDate")
    @Mapping(source = "loanRequestModel.checkoutDate", target = "checkoutDate")
    @Mapping(source = "loanRequestModel.returnDate", target = "returnDate")
    @Mapping(source = "loanRequestModel.status", target = "status")
    Loan requestModelToEntity(LoanRequestModel loanRequestModel,
                                          LoanIdentifier loanIdentifier,
                                          BookModel bookModel,
                                          EmployeeModel employeeModel,
                                          PatronModel patronModel
                                          );
}
