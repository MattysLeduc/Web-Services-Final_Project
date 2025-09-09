package com.leduc.patrons.dataAccessLayer;

import org.springframework.data.jpa.repository.JpaRepository;


public interface PatronRepository extends JpaRepository<Patron, Integer> {
    Patron findPatronsByPatronIdentifier_PatronId(String patronIdentifier);
    Patron findByEmail(String email);
}
