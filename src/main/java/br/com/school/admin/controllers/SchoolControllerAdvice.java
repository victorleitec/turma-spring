package br.com.school.admin.controllers;

import br.com.school.admin.exceptions.BusinessRuleException;
import br.com.school.admin.exceptions.ErrorDto;
import br.com.school.admin.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SchoolControllerAdvice {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDto handleException(ResourceNotFoundException e) {
        return new ErrorDto(e.getMessage(), "404");
    }

    @ExceptionHandler({BusinessRuleException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDto handleException(Exception e) {
        return new ErrorDto(e.getMessage(), "400");
    }
}
