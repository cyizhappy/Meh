package rw.gov.erp.payroll.exception;

/**
 * Thrown for bad requests or business rule violations (HTTP 400)
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
