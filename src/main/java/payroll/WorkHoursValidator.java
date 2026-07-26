package payroll;

/**
 * Validates a single day's logged work hours. Originally this rule was
 * covered only by a Selenium test against a time-clock UI that doesn't
 * exist in any environment available to this project (see
 * tests.TimeClockNegativeTest). The validation rule itself is plain
 * business logic, so it's implemented here and unit-tested directly -
 * see unit.WorkHoursValidatorTest for the Positive/Negative coverage.
 */
public class WorkHoursValidator {

    public static final double MAX_DAILY_HOURS = 24.0;
    public static final double MIN_DAILY_HOURS = 0.0;

    public void validateDailyHours(double hours) {
        if (hours > MAX_DAILY_HOURS) {
            throw new InvalidWorkHoursException("Daily logged working hours cannot exceed 24 hours.");
        }
        if (hours < MIN_DAILY_HOURS) {
            throw new InvalidWorkHoursException("Daily logged working hours cannot be negative.");
        }
    }
}
