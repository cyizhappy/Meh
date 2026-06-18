package rw.gov.erp.payroll.exception;

/**
 * Thrown when a requested resource is not found (HTTP 404)
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
