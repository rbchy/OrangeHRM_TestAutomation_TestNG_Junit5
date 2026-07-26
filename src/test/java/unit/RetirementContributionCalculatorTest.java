package unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import payroll.RetirementContributionCalculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 401(k) employee deferral capping against the real 2026 IRS annual limits,
 * plus employer match calculation.
 */
class RetirementContributionCalculatorTest {

    private RetirementContributionCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new RetirementContributionCalculator();
    }

    @Test
    @DisplayName("Under-30 employee: standard $24,500 annual limit applies")
    void applicableAnnualLimit_underFifty_isStandardLimit() {
        assertEquals(24_500.00, calculator.applicableAnnualLimit(30), 0.001);
    }

    @Test
    @DisplayName("Age 55: standard catch-up limit ($24,500 + $8,000 = $32,500) applies")
    void applicableAnnualLimit_age55_isCatchUpLimit() {
        assertEquals(32_500.00, calculator.applicableAnnualLimit(55), 0.001);
    }

    @Test
    @DisplayName("Age 61: SECURE 2.0 super catch-up limit ($24,500 + $11,250 = $35,750) applies")
    void applicableAnnualLimit_age61_isSuperCatchUpLimit() {
        assertEquals(35_750.00, calculator.applicableAnnualLimit(61), 0.001);
    }

    @Test
    @DisplayName("Age 64: reverts to the standard catch-up limit (super catch-up is 60-63 only)")
    void applicableAnnualLimit_age64_isCatchUpLimit() {
        assertEquals(32_500.00, calculator.applicableAnnualLimit(64), 0.001);
    }

    @Test
    @DisplayName("Positive: a normal contribution well under the annual limit is unaffected")
    void calculateEmployeeContribution_underLimit_returnsRequestedAmount() {
        double contribution = calculator.calculateEmployeeContribution(5_000.00, 6.0, 0.0, 30);
        assertEquals(300.00, contribution, 0.001);
    }

    @Test
    @DisplayName("A contribution that would cross the annual limit is capped at the remaining room")
    void calculateEmployeeContribution_nearLimit_isCapped() {
        // Requested: 50,000 * 50% = 25,000, but only 500 of headroom remains under the 24,500 limit.
        double contribution = calculator.calculateEmployeeContribution(50_000.00, 50.0, 24_000.00, 30);
        assertEquals(500.00, contribution, 0.001);
    }

    @Test
    @DisplayName("Once the annual limit is already reached, further contributions are zero")
    void calculateEmployeeContribution_limitAlreadyReached_isZero() {
        double contribution = calculator.calculateEmployeeContribution(5_000.00, 10.0, 24_500.00, 30);
        assertEquals(0.00, contribution, 0.001);
    }

    @Test
    @DisplayName("Employer match is capped at the configured percentage of gross pay")
    void calculateEmployerMatch_cappedByGrossPercentage() {
        // Employee contributed 300 (6% of 5,000); employer matches 100% up to 4% of gross (200.00).
        double match = calculator.calculateEmployerMatch(300.00, 5_000.00, 100.0, 4.0);
        assertEquals(200.00, match, 0.001);
    }

    @Test
    @DisplayName("Employer match is the employee's contribution when it's below the gross-pay cap")
    void calculateEmployerMatch_belowCap_matchesContributionInFull() {
        // Employee contributed 100 (2% of 5,000); cap is 4% of gross (200.00), so full match applies.
        double match = calculator.calculateEmployerMatch(100.00, 5_000.00, 100.0, 4.0);
        assertEquals(100.00, match, 0.001);
    }

    @Test
    @DisplayName("Negative: a negative age is rejected")
    void applicableAnnualLimit_negativeAge_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> calculator.applicableAnnualLimit(-1));
    }
}
