package payroll;

/** Union dues deduction: either a flat per-period fee or a percentage of gross pay. */
public class UnionDuesCalculator {

    public enum DuesType {
        FLAT,
        PERCENTAGE_OF_GROSS
    }

    /**
     * @return the dues amount, capped at grossPay - dues can never exceed
     *         what the employee actually earned this period.
     */
    public double calculateDues(double grossPay, DuesType type, double amount) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        validateNonNegative(grossPay, "grossPay");
        validateNonNegative(amount, "amount");

        double dues;
        switch (type) {
            case FLAT:
                dues = amount;
                break;
            case PERCENTAGE_OF_GROSS:
                dues = grossPay * amount / 100.0;
                break;
            default:
                throw new IllegalArgumentException("Unsupported dues type: " + type);
        }

        return round2(Math.min(dues, grossPay));
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
