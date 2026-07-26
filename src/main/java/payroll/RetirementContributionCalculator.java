package payroll;

/**
 * 401(k) employee deferral and employer match calculation, enforcing the
 * real 2026 IRS annual contribution limits (Notice 2025-67 / SECURE 2.0):
 *   - Standard elective deferral limit: $24,500
 *   - Catch-up (age 50-59, or 64+): additional $8,000 -> $32,500 total
 *   - "Super" catch-up (SECURE 2.0, ages 60-63): additional $11,250 -> $35,750 total
 *
 * Replaces the flat, uncapped percentage-of-gross 401k calculation in
 * PayrollCalculator.calculate401kContribution, which has no notion of
 * annual limits or employer matching.
 */
public class RetirementContributionCalculator {

    public static final double STANDARD_ANNUAL_LIMIT_2026 = 24_500.00;
    public static final double CATCH_UP_AMOUNT_2026 = 8_000.00; // ages 50-59 and 64+
    public static final double SUPER_CATCH_UP_AMOUNT_2026 = 11_250.00; // ages 60-63

    /** @return the IRS annual elective-deferral limit that applies at this employee's age. */
    public double applicableAnnualLimit(int employeeAge) {
        if (employeeAge < 0) {
            throw new IllegalArgumentException("employeeAge must not be negative: " + employeeAge);
        }
        if (employeeAge >= 60 && employeeAge <= 63) {
            return STANDARD_ANNUAL_LIMIT_2026 + SUPER_CATCH_UP_AMOUNT_2026;
        }
        if (employeeAge >= 50) {
            return STANDARD_ANNUAL_LIMIT_2026 + CATCH_UP_AMOUNT_2026;
        }
        return STANDARD_ANNUAL_LIMIT_2026;
    }

    /**
     * The employee's contribution for this pay period: the requested
     * deferral percentage of gross pay, capped so year-to-date
     * contributions never exceed the age-appropriate IRS annual limit.
     */
    public double calculateEmployeeContribution(double grossPay, double deferralPercentage,
            double yearToDateContributions, int employeeAge) {
        validateNonNegative(grossPay, "grossPay");
        validateNonNegative(deferralPercentage, "deferralPercentage");
        validateNonNegative(yearToDateContributions, "yearToDateContributions");

        double desiredContribution = round2(grossPay * deferralPercentage / 100.0);
        double limit = applicableAnnualLimit(employeeAge);
        double remainingRoom = Math.max(0, round2(limit - yearToDateContributions));

        return round2(Math.min(desiredContribution, remainingRoom));
    }

    /**
     * Employer match: matchPercentageOfContribution of what the employee
     * put in, capped at matchCapPercentageOfGross of gross pay (e.g. "100%
     * match up to 4% of gross").
     */
    public double calculateEmployerMatch(double employeeContribution, double grossPay,
            double matchPercentageOfContribution, double matchCapPercentageOfGross) {
        validateNonNegative(employeeContribution, "employeeContribution");
        validateNonNegative(grossPay, "grossPay");
        validateNonNegative(matchPercentageOfContribution, "matchPercentageOfContribution");
        validateNonNegative(matchCapPercentageOfGross, "matchCapPercentageOfGross");

        double proposedMatch = round2(employeeContribution * matchPercentageOfContribution / 100.0);
        double matchCap = round2(grossPay * matchCapPercentageOfGross / 100.0);

        return round2(Math.min(proposedMatch, matchCap));
    }

    private void validateNonNegative(double value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative: " + value);
        }
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
