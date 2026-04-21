package org.pt.project.controller;

import lombok.extern.slf4j.Slf4j;
import org.pt.project.exception.ErrorResponse;
import org.pt.project.exception.LoginDuplicateException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class HandlerForException {

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuthError(AuthenticationException e) {
        return new ErrorResponse("Неверный логин или пароль");
    }

    @ExceptionHandler(LoginDuplicateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateLogin(LoginDuplicateException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleError(final RuntimeException e) {
        log.error(e.getMessage(), e);
        return new ErrorResponse("Server error");
    }
}
