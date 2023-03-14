package br.com.school.admin.exceptions;

public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message, null, false, false);
    }
}
