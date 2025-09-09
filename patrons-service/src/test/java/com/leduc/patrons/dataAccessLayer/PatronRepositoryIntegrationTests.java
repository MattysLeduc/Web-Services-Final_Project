package com.leduc.patrons.dataAccessLayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PatronRepositoryIntegrationTests {

    @Autowired
    private PatronRepository patronRepository;

    @BeforeEach
    void setupDb() {
        patronRepository.deleteAll();
    }

    @Test
    void whenPatronEntityIsValid_thenSaveAndRetrievePatron() {
        // Arrange
        Patron patron = new Patron();
        // Create a specific PatronIdentifier with a valid 36-character UUID string.
        PatronIdentifier identifier = new PatronIdentifier("12345678-1234-1234-1234-123456789012");
        patron.setPatronIdentifier(identifier);
        patron.setFirstName("John");
        patron.setLastName("Doe");
        patron.setEmail("john.doe@example.com");
        patron.setPassword("password123");
        // Use an empty list for phone numbers.
        patron.setPhoneNumbers(Collections.emptyList());
        PatronAddress address = new PatronAddress("123 Main St", "Anytown", "SomeState", "USA", "12345");
        patron.setPatronAddress(address);
        patron.setMemberShipType(MemberShipType.REGULAR);

        // Act
        Patron savedPatron = patronRepository.save(patron);
        Patron foundPatron = patronRepository.findPatronsByPatronIdentifier_PatronId(identifier.getPatronId());

        // Assert
        assertNotNull(savedPatron);
        assertNotNull(foundPatron);
        assertEquals(identifier.getPatronId(), foundPatron.getPatronIdentifier().getPatronId());
        assertEquals("John", foundPatron.getFirstName());
        assertEquals("Doe", foundPatron.getLastName());
        assertEquals("john.doe@example.com", foundPatron.getEmail());
    }

    @Test
    void whenPatronDoesNotExist_thenReturnNull() {
        // Arrange: use a non-existent ID.
        String nonExistentId = "00000000-0000-0000-0000-000000000000";

        // Act
        Patron foundPatron = patronRepository.findPatronsByPatronIdentifier_PatronId(nonExistentId);

        // Assert
        assertNull(foundPatron);
    }

    @Test
    void whenPatronIsUpdated_thenReturnUpdatedPatron() {
        // Arrange
        Patron patron = new Patron();
        PatronIdentifier identifier = new PatronIdentifier("22345678-1234-1234-1234-123456789012");
        patron.setPatronIdentifier(identifier);
        patron.setFirstName("Alice");
        patron.setLastName("Smith");
        patron.setEmail("alice.smith@example.com");
        patron.setPassword("password123");
        patron.setPhoneNumbers(Collections.emptyList());
        PatronAddress address = new PatronAddress("456 Elm St", "Othertown", "OtherState", "USA", "67890");
        patron.setPatronAddress(address);
        patron.setMemberShipType(MemberShipType.REGULAR);

        Patron savedPatron = patronRepository.save(patron);

        // Act: update firstName.
        savedPatron.setFirstName("Alicia");
        Patron updatedPatron = patronRepository.save(savedPatron);
        Patron foundPatron = patronRepository.findPatronsByPatronIdentifier_PatronId(identifier.getPatronId());

        // Assert
        assertNotNull(updatedPatron);
        assertNotNull(foundPatron);
        assertEquals("Alicia", foundPatron.getFirstName());
    }

    @Test
    void whenPatronIsDeleted_thenReturnNull() {
        // Arrange
        Patron patron = new Patron();
        PatronIdentifier identifier = new PatronIdentifier("32345678-1234-1234-1234-123456789012");
        patron.setPatronIdentifier(identifier);
        patron.setFirstName("To");
        patron.setLastName("Delete");
        patron.setEmail("delete@example.com");
        patron.setPassword("password123");
        patron.setPhoneNumbers(Collections.emptyList());
        PatronAddress address = new PatronAddress("789 Oak St", "Thistown", "ThisState", "USA", "11111");
        patron.setPatronAddress(address);
        patron.setMemberShipType(MemberShipType.REGULAR);

        Patron savedPatron = patronRepository.save(patron);

        // Act
        patronRepository.delete(savedPatron);

        // Assert
        Patron foundPatron = patronRepository.findPatronsByPatronIdentifier_PatronId(identifier.getPatronId());
        assertNull(foundPatron);
    }

    @Test
    void whenMultiplePatronsAreSaved_thenFindAllReturnsCorrectCount() {
        // Arrange
        Patron patron1 = new Patron();
        PatronIdentifier id1 = new PatronIdentifier("42345678-1234-1234-1234-123456789012");
        patron1.setPatronIdentifier(id1);
        patron1.setFirstName("Patron");
        patron1.setLastName("One");
        patron1.setEmail("one@example.com");
        patron1.setPassword("password123");
        patron1.setPhoneNumbers(Collections.emptyList());
        PatronAddress address1 = new PatronAddress("101 First St", "CityOne", "StateOne", "USA", "10101");
        patron1.setPatronAddress(address1);
        patron1.setMemberShipType(MemberShipType.REGULAR);

        Patron patron2 = new Patron();
        PatronIdentifier id2 = new PatronIdentifier("52345678-1234-1234-1234-123456789012");
        patron2.setPatronIdentifier(id2);
        patron2.setFirstName("Patron");
        patron2.setLastName("Two");
        patron2.setEmail("two@example.com");
        patron2.setPassword("password123");
        patron2.setPhoneNumbers(Collections.emptyList());
        PatronAddress address2 = new PatronAddress("202 Second St", "CityTwo", "StateTwo", "USA", "20202");
        patron2.setPatronAddress(address2);
        patron2.setMemberShipType(MemberShipType.REGULAR);

        patronRepository.saveAll(List.of(patron1, patron2));

        // Act
        List<Patron> allPatrons = patronRepository.findAll();

        // Assert
        assertNotNull(allPatrons);
        assertEquals(2, allPatrons.size());
        assertThat(allPatrons, containsInAnyOrder(
                hasProperty("patronIdentifier", equalTo(id1)),
                hasProperty("patronIdentifier", equalTo(id2))
        ));
    }

    @Test
    void whenNoPatronsExist_thenFindAllReturnsEmptyList() {
        // Act
        List<Patron> patrons = patronRepository.findAll();
        // Assert
        assertNotNull(patrons);
        assertTrue(patrons.isEmpty(), "Expected no patrons in the repository, but found some");
    }

    @Test
    void whenSavingPatronWithNullEmail_thenThrowException() {
        // Arrange: create a patron with a null email.
        Patron patron = new Patron();
        PatronIdentifier identifier = new PatronIdentifier("62345678-1234-1234-1234-123456789012");
        patron.setPatronIdentifier(identifier);
        patron.setFirstName("Null");
        patron.setLastName("Email");
        patron.setEmail(null); // Intentionally null.
        patron.setPassword("password123");
        patron.setPhoneNumbers(Collections.emptyList());
        PatronAddress address = new PatronAddress("404 NotFound St", "Nowhere", "Nostate", "USA", "00000");
        patron.setPatronAddress(address);
        patron.setMemberShipType(MemberShipType.REGULAR);

        // Act & Assert: expect a constraint violation exception.
        Exception exception = assertThrows(Exception.class, () -> {
            patronRepository.save(patron);
            patronRepository.flush();
        });
        assertNotNull(exception);
    }

    @Test
    void testPatronConstructorParameters() {
        // Arrange: define expected values.
        String firstName = "Test";
        String lastName = "Patron";
        String email = "test.patron@example.com";
        String password = "password123";
        // Assuming a simple empty list for phone numbers.
        List<PatronPhoneNumber> phoneNumbers = Collections.emptyList();
        PatronAddress address = new PatronAddress("500 Test Ave", "Test City", "Test State", "USA", "55555");
        MemberShipType type = MemberShipType.REGULAR;

        // Act: instantiate a Patron using the parameterized constructor.
        Patron patron = new Patron(firstName, lastName, email, password, phoneNumbers, address, type);

        // Assert: verify that all fields are correctly assigned.
        // The id should be null until persistence.
        assertNull(patron.getId(), "Expected id to be null before persistence");
        // The constructor should initialize the embedded PatronIdentifier.
        assertNotNull(patron.getPatronIdentifier(), "PatronIdentifier should not be null");
        assertEquals(firstName, patron.getFirstName());
        assertEquals(lastName, patron.getLastName());
        assertEquals(email, patron.getEmail());
        assertEquals(password, patron.getPassword());
        assertEquals(phoneNumbers, patron.getPhoneNumbers());
        assertEquals(address, patron.getPatronAddress());
        assertEquals(type, patron.getMemberShipType());
    }
}
