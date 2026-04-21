package org.pt.project.exception;

public class LoginDuplicateException extends RuntimeException {
    public LoginDuplicateException(String message) {
        super(message);
    }
}
