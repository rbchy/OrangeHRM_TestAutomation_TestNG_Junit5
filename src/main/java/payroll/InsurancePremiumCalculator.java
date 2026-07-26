package payroll;

/**
 * Premium calculation for benefits enrollment. Insurance enrollment is a
 * paid/enterprise OrangeHRM feature with no publicly reachable demo, so this
 * is covered as a unit-tested business-logic class (see
 * unit.InsurancePremiumCalculatorTest) rather than a Selenium UI test
 * against a real enrollment form.
 */
public class InsurancePremiumCalculator {

    public static final String PLAN_INDIVIDUAL = "INDIVIDUAL";
    public static final String PLAN_FAMILY = "FAMILY";

    private static final double INDIVIDUAL_BASE_PREMIUM = 125.50;
    private static final double FAMILY_BASE_PREMIUM = 310.75;
    private static final double PER_DEPENDENT_SURCHARGE = 45.00;

    public double calculatePremium(String planTier, int dependentCount) {
        if (planTier == null) {
            throw new InvalidInsurancePlanException("Plan tier must not be null.");
        }
        if (dependentCount < 0) {
            throw new InvalidInsurancePlanException("Dependent count must not be negative: " + dependentCount);
        }

        String normalizedTier = planTier.trim().toUpperCase();
        switch (normalizedTier) {
            case PLAN_INDIVIDUAL:
                if (dependentCount > 0) {
                    throw new InvalidInsurancePlanException(
                            "Individual plans cannot include dependents; use " + PLAN_FAMILY + " instead.");
                }
                return round2(INDIVIDUAL_BASE_PREMIUM);
            case PLAN_FAMILY:
                return round2(FAMILY_BASE_PREMIUM + (dependentCount * PER_DEPENDENT_SURCHARGE));
            default:
                throw new InvalidInsurancePlanException("Unknown plan tier: " + planTier);
        }
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
