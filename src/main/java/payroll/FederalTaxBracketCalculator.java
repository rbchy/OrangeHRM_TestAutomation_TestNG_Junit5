package payroll;

/**
 * Real bracket-based federal income tax withholding, replacing the flat-rate
 * placeholder used elsewhere in this project (PayrollCalculator.calculateFederalTax).
 *
 * Uses the actual 2026 IRS Publication 15-T "Percentage Method Tables for
 * Automated Payroll Systems" - STANDARD Withholding Rate Schedule, Single
 * (or Married Filing Separately), annual pay period. This is the table for
 * a 2020-or-later Form W-4 with the Step 2 checkbox NOT checked (the most
 * common case), or any 2019-or-earlier W-4.
 *
 * Source: IRS Pub 15-T (2026), Standard Withholding Rate Schedules
 * (irs.gov/pub/irs-pdf/p15t.pdf). Figures are set for tax year 2026 and
 * will need updating when the IRS publishes the 2027 tables.
 */
public class FederalTaxBracketCalculator {

    /** One row of the IRS percentage-method rate schedule. */
    private static final class Bracket {
        final double over;
        final double base;
        final double rate; // decimal, e.g. 0.12 for 12%

        Bracket(double over, double base, double rate) {
            this.over = over;
            this.base = base;
            this.rate = rate;
        }
    }

    private static final Bracket[] SINGLE_STANDARD_ANNUAL_2026 = {
            new Bracket(0, 0, 0.00),
            new Bracket(7_500, 0, 0.10),
            new Bracket(19_900, 1_240, 0.12),
            new Bracket(57_900, 5_800, 0.22),
            new Bracket(113_200, 17_966, 0.24),
            new Bracket(209_275, 41_024, 0.32),
            new Bracket(263_725, 58_448, 0.35),
            new Bracket(648_100, 192_979, 0.37),
    };

    /**
     * @param annualTaxableWages annual wages after pre-tax deductions (401k,
     *                           Section 125 insurance premiums, etc.)
     * @return annual federal withholding, per the 2026 Single/Standard table
     */
    public double calculateAnnualWithholding(double annualTaxableWages) {
        if (annualTaxableWages < 0) {
            throw new IllegalArgumentException(
                    "annualTaxableWages must not be negative: " + annualTaxableWages);
        }

        Bracket applicable = SINGLE_STANDARD_ANNUAL_2026[0];
        for (Bracket bracket : SINGLE_STANDARD_ANNUAL_2026) {
            if (annualTaxableWages >= bracket.over) {
                applicable = bracket;
            } else {
                break;
            }
        }

        double withholding = applicable.base + (annualTaxableWages - applicable.over) * applicable.rate;
        return round2(withholding);
    }

    /** Convenience: withholding for a single pay period, given the period's taxable wages and pay frequency. */
    public double calculatePerPeriodWithholding(double periodTaxableWages, int payPeriodsPerYear) {
        if (payPeriodsPerYear <= 0) {
            throw new IllegalArgumentException("payPeriodsPerYear must be positive: " + payPeriodsPerYear);
        }
        double annualized = periodTaxableWages * payPeriodsPerYear;
        double annualWithholding = calculateAnnualWithholding(annualized);
        return round2(annualWithholding / payPeriodsPerYear);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
