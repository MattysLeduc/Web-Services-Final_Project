package com.leduc.books.utils.exceptions;

public class BookAlreadyRegisteredException extends RuntimeException {
    public BookAlreadyRegisteredException(String message) {
        super(message);
    }
}
