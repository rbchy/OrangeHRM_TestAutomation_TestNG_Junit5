package unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import payroll.InvalidWorkHoursException;
import payroll.WorkHoursValidator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Positive Testing (a normal 8-hour entry is accepted) and Negative Testing
 * (26 hours or a negative figure is rejected with a validation error).
 * This replaces the original Selenium-based negative test
 * (tests.TimeClockNegativeTest), which pointed at a time-clock UI that
 * doesn't exist anywhere reachable - the validation rule itself is plain
 * business logic and is exercised directly here instead.
 */
class WorkHoursValidatorTest {

    private WorkHoursValidator validator;

    @BeforeEach
    void setUp() {
        validator = new WorkHoursValidator();
    }

    @Test
    @DisplayName("Positive: a standard 8-hour entry passes validation")
    void validateDailyHours_eightHours_isAccepted() {
        assertDoesNotThrow(() -> validator.validateDailyHours(8.0));
    }

    @Test
    @DisplayName("Positive: the 24-hour boundary itself is still valid")
    void validateDailyHours_boundaryTwentyFourHours_isAccepted() {
        assertDoesNotThrow(() -> validator.validateDailyHours(24.0));
    }

    @Test
    @DisplayName("Negative: 26 hours in a day is rejected")
    void validateDailyHours_twentySixHours_throwsInvalidWorkHoursException() {
        InvalidWorkHoursException ex = assertThrows(InvalidWorkHoursException.class,
                () -> validator.validateDailyHours(26.0));

        assertEquals("Daily logged working hours cannot exceed 24 hours.", ex.getMessage());
    }

    @Test
    @DisplayName("Negative: a negative hours figure is rejected")
    void validateDailyHours_negativeHours_throwsInvalidWorkHoursException() {
        InvalidWorkHoursException ex = assertThrows(InvalidWorkHoursException.class,
                () -> validator.validateDailyHours(-2.0));

        assertEquals("Daily logged working hours cannot be negative.", ex.getMessage());
    }
}
