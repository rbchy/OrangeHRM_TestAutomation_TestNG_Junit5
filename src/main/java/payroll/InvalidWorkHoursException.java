package payroll;

/** Thrown when a submitted daily work-hours entry is outside the valid range. */
public class InvalidWorkHoursException extends RuntimeException {

    public InvalidWorkHoursException(String message) {
        super(message);
    }
}
