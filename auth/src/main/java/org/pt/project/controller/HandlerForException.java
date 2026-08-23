package org.pt.project.controller;

import lombok.extern.slf4j.Slf4j;
import org.pt.project.exception.ErrorResponse;
import org.pt.project.exception.InvalidCredentialsException;
import org.pt.project.exception.LoginDuplicateException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@RestControllerAdvice
public class HandlerForException {

    @ExceptionHandler({
            AuthenticationException.class,
            InvalidCredentialsException.class
    })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuthError(RuntimeException e) {
        log.warn("Authentication failed: {}", e.getMessage());
        return new ErrorResponse("Неверный логин или пароль");
    }

    @ExceptionHandler(HttpClientErrorException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorResponse handleUpstreamClientError(HttpClientErrorException e) {
        log.warn("Upstream HTTP {}: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
        return new ErrorResponse("Ошибка при обращении к сервису аутентификации");
    }

    @ExceptionHandler(LoginDuplicateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateLogin(LoginDuplicateException e) {
        log.warn(e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleError(final RuntimeException e) {
        log.error("Unhandled error: {}", e.getMessage(), e);
        return new ErrorResponse("Server error");
    }
}
