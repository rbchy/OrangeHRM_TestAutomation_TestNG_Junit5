package unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import payroll.OvertimeCalculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** FLSA weekly (40hr) overtime and the blended/weighted-average rate method. */
class OvertimeCalculatorTest {

    private OvertimeCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new OvertimeCalculator();
    }

    @Test
    @DisplayName("Positive: exactly 40 hours in a week has no overtime")
    void calculateWeeklyOvertimePay_fortyHours_isZero() {
        assertEquals(0.00, calculator.calculateWeeklyOvertimePay(20.0, 40.0), 0.001);
    }

    @Test
    @DisplayName("Hours beyond 40/week are paid at 1.5x, regardless of daily distribution")
    void calculateWeeklyOvertimePay_fortyFiveHours() {
        double overtimePay = calculator.calculateWeeklyOvertimePay(20.0, 45.0);
        assertEquals(150.00, overtimePay, 0.001); // 5 OT hours x 20 x 1.5
    }

    @Test
    @DisplayName("Weekly regular pay caps at the 40-hour threshold")
    void calculateWeeklyRegularPay_capsAtForty() {
        assertEquals(800.00, calculator.calculateWeeklyRegularPay(20.0, 45.0), 0.001); // 40 x 20
    }

    @Test
    @DisplayName("Blended rate: two roles at different rates in the same week, no overtime")
    void calculateBlendedWeeklyPay_underFortyHours_noPremium() {
        double pay = calculator.calculateBlendedWeeklyPay(
                new double[] { 20.0, 16.0 }, new double[] { 25.0, 10.0 });
        assertEquals(660.00, pay, 0.001); // 25x20 + 10x16 = 500 + 160
    }

    @Test
    @DisplayName("Blended rate: overtime hours earn a half-time premium on the weighted-average rate")
    void calculateBlendedWeeklyPay_withOvertime_appliesHalfTimePremium() {
        // 30h @ $20 + 15h @ $16 = 45 total hours, 5 OT hours.
        // Straight time: 600 + 240 = 840. Blended rate: 840 / 45 = 18.6667.
        // Overtime premium: 5 x 18.6667 x 0.5 = 46.67. Total: 886.67.
        double pay = calculator.calculateBlendedWeeklyPay(
                new double[] { 20.0, 16.0 }, new double[] { 30.0, 15.0 });
        assertEquals(886.67, pay, 0.01);
    }

    @Test
    @DisplayName("Negative: mismatched rate/hour array lengths are rejected")
    void calculateBlendedWeeklyPay_mismatchedArrays_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateBlendedWeeklyPay(new double[] { 20.0 }, new double[] { 10.0, 5.0 }));
    }

    @Test
    @DisplayName("Negative: negative hours are rejected")
    void calculateWeeklyOvertimePay_negativeHours_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateWeeklyOvertimePay(20.0, -5.0));
    }
}
