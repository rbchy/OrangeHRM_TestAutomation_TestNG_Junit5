package payroll;

/**
 * Core payroll math: regular/overtime pay, state/federal tax withholding,
 * and 401k contribution amounts. Tax rates are supplied by a
 * {@link TaxRateProvider} collaborator rather than hard-coded, so this class
 * can be unit-tested in isolation with a mocked provider.
 */
public class PayrollCalculator {

    private static final double DAILY_OVERTIME_THRESHOLD_HOURS = 8.0;
    private static final double OVERTIME_MULTIPLIER = 1.5;

    private final TaxRateProvider taxRateProvider;

    public PayrollCalculator(TaxRateProvider taxRateProvider) {
        if (taxRateProvider == null) {
            throw new IllegalArgumentException("taxRateProvider must not be null");
        }
        this.taxRateProvider = taxRateProvider;
    }

    /** Straight hourly pay for hours at or below the daily overtime threshold. */
    public double calculateRegularPay(double hourlyRate, double hoursWorked) {
        validateNonNegative(hourlyRate, "hourlyRate");
        validateNonNegative(hoursWorked, "hoursWorked");
        double regularHours = Math.min(hoursWorked, DAILY_OVERTIME_THRESHOLD_HOURS);
        return round2(regularHours * hourlyRate);
    }

    /** 1.5x pay for hours worked beyond the daily overtime threshold. */
    public double calculateOvertimePay(double hourlyRate, double hoursWorked) {
        validateNonNegative(hourlyRate, "hourlyRate");
        validateNonNegative(hoursWorked, "hoursWorked");
        double overtimeHours = Math.max(0, hoursWorked - DAILY_OVERTIME_THRESHOLD_HOURS);
        return round2(overtimeHours * hourlyRate * OVERTIME_MULTIPLIER);
    }

    /** Regular pay + overtime pay for a single day. */
    public double calculateGrossDailyPay(double hourlyRate, double hoursWorked) {
        return round2(calculateRegularPay(hourlyRate, hoursWorked) + calculateOvertimePay(hourlyRate, hoursWorked));
    }

    public double calculateStateTax(double grossPay, String state) {
        validateNonNegative(grossPay, "grossPay");
        double rate = taxRateProvider.getStateTaxRate(state);
        return round2(grossPay * rate / 100.0);
    }

    public double calculateFederalTax(double grossPay) {
        validateNonNegative(grossPay, "grossPay");
        double rate = taxRateProvider.getFederalTaxRate();
        return round2(grossPay * rate / 100.0);
    }

    public double calculate401kContribution(double grossPay, double deferralPercentage) {
        validateNonNegative(grossPay, "grossPay");
        validateNonNegative(deferralPercentage, "deferralPercentage");
        return round2(grossPay * deferralPercentage / 100.0);
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
