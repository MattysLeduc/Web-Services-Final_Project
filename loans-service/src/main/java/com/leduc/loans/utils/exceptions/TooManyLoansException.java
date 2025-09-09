package com.leduc.loans.utils.exceptions;

public class TooManyLoansException extends RuntimeException {

  public TooManyLoansException(String message) {
        super(message);
    }

  public TooManyLoansException(Throwable cause) { super(cause); }

  public TooManyLoansException(String message, Throwable cause) { super(message, cause); }
}
