package unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import payroll.InsurancePremiumCalculator;
import payroll.InvalidInsurancePlanException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Functional / UI Testing substitute for Insurance Benefit Enrollment.
 * There is no reachable enrollment UI to automate (it's a paid OrangeHRM
 * feature, not part of the public demo), so the enrollment business logic
 * is unit-tested directly instead of driven through Selenium.
 */
class InsurancePremiumCalculatorTest {

    private InsurancePremiumCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new InsurancePremiumCalculator();
    }

    @Test
    @DisplayName("Positive: individual plan with no dependents returns the base premium")
    void calculatePremium_individualPlan_returnsBasePremium() {
        double premium = calculator.calculatePremium("INDIVIDUAL", 0);
        assertEquals(125.50, premium, 0.001);
    }

    @Test
    @DisplayName("Positive: family plan premium scales with dependent count")
    void calculatePremium_familyPlanWithDependents_addsPerDependentSurcharge() {
        double premium = calculator.calculatePremium("FAMILY", 2);
        assertEquals(400.75, premium, 0.001); // 310.75 base + 2 x 45.00
    }

    @Test
    @DisplayName("Positive: plan tier lookup is case-insensitive")
    void calculatePremium_lowercasePlanTier_isAccepted() {
        double premium = calculator.calculatePremium("individual", 0);
        assertEquals(125.50, premium, 0.001);
    }

    @Test
    @DisplayName("Negative: individual plan cannot include dependents")
    void calculatePremium_individualPlanWithDependents_throwsException() {
        assertThrows(InvalidInsurancePlanException.class, () -> calculator.calculatePremium("INDIVIDUAL", 1));
    }

    @Test
    @DisplayName("Negative: unknown plan tier is rejected")
    void calculatePremium_unknownPlanTier_throwsException() {
        assertThrows(InvalidInsurancePlanException.class, () -> calculator.calculatePremium("PLATINUM", 0));
    }

    @Test
    @DisplayName("Negative: negative dependent count is rejected")
    void calculatePremium_negativeDependentCount_throwsException() {
        assertThrows(InvalidInsurancePlanException.class, () -> calculator.calculatePremium("FAMILY", -1));
    }
}
