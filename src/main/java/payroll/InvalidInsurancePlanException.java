package payroll;

/** Thrown when an insurance enrollment request specifies an invalid plan tier or dependent count. */
public class InvalidInsurancePlanException extends RuntimeException {

    public InvalidInsurancePlanException(String message) {
        super(message);
    }
}
