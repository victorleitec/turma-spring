package br.com.school.admin.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message, null, false, false);
    }
}
