package com.leduc.apigateway.patrons.businessLayer;

import com.leduc.apigateway.patrons.presentationLayer.PatronRequestModel;
import com.leduc.apigateway.patrons.presentationLayer.PatronResponseModel;

import java.util.List;

public interface PatronsService {
    List<PatronResponseModel> getPatrons();
    PatronResponseModel getPatronByPatronId(String patronId);
    PatronResponseModel createPatron(PatronRequestModel requestModel);
    PatronResponseModel updatePatron(String patronId, PatronRequestModel requestModel);
    void deletePatron(String patronId);
}
