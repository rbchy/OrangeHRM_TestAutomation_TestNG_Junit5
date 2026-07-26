package unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import payroll.FederalTaxBracketCalculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit Testing: real 2026 IRS Publication 15-T bracket-based federal
 * withholding (Single/Standard annual table), replacing the flat-rate
 * placeholder used elsewhere in this project. Expected values below are
 * hand-computed from the published bracket table (base + rate x excess
 * over the bracket floor).
 */
class FederalTaxBracketCalculatorTest {

    private FederalTaxBracketCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new FederalTaxBracketCalculator();
    }

    @Test
    @DisplayName("Wages entirely within the 0% bracket owe nothing")
    void calculateAnnualWithholding_belowFirstBracket_isZero() {
        assertEquals(0.00, calculator.calculateAnnualWithholding(5_000), 0.001);
    }

    @Test
    @DisplayName("Exactly at the first bracket boundary still owes nothing")
    void calculateAnnualWithholding_atFirstBoundary_isZero() {
        assertEquals(0.00, calculator.calculateAnnualWithholding(7_500), 0.001);
    }

    @Test
    @DisplayName("10% bracket: base $0 + 10% of the excess over $7,500")
    void calculateAnnualWithholding_tenPercentBracket() {
        assertEquals(750.00, calculator.calculateAnnualWithholding(15_000), 0.001);
    }

    @Test
    @DisplayName("12% bracket: $50,000 taxable wages")
    void calculateAnnualWithholding_fiftyThousand() {
        // base 1,240 + 12% x (50,000 - 19,900) = 1,240 + 3,612 = 4,852.00
        assertEquals(4_852.00, calculator.calculateAnnualWithholding(50_000), 0.001);
    }

    @Test
    @DisplayName("22% bracket: $100,000 taxable wages")
    void calculateAnnualWithholding_oneHundredThousand() {
        // base 5,800 + 22% x (100,000 - 57,900) = 5,800 + 9,262 = 15,062.00
        assertEquals(15_062.00, calculator.calculateAnnualWithholding(100_000), 0.001);
    }

    @Test
    @DisplayName("Top 37% bracket: $700,000 taxable wages")
    void calculateAnnualWithholding_topBracket() {
        // base 192,979 + 37% x (700,000 - 648,100) = 192,979 + 19,203 = 212,182.00
        assertEquals(212_182.00, calculator.calculateAnnualWithholding(700_000), 0.001);
    }

    @Test
    @DisplayName("Per-period withholding is the annualized amount divided across pay periods")
    void calculatePerPeriodWithholding_matchesAnnualDividedByPeriods() {
        double annualWages = 50_000;
        int periods = 12;
        double perPeriodWages = annualWages / periods;

        double perPeriod = calculator.calculatePerPeriodWithholding(perPeriodWages, periods);
        double expected = calculator.calculateAnnualWithholding(annualWages) / periods;

        assertEquals(expected, perPeriod, 0.05);
    }

    @Test
    @DisplayName("Negative taxable wages are rejected")
    void calculateAnnualWithholding_negativeWages_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateAnnualWithholding(-1));
    }
}
