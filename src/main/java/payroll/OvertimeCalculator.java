package payroll;

/**
 * FLSA weekly overtime (29 U.S.C. Sec. 207): hours worked beyond 40 in a
 * single workweek are paid at 1.5x the regular rate. This is distinct from
 * the daily-overtime rule already modeled in PayrollCalculator (some states,
 * e.g. California, also apply a daily threshold on top of the federal
 * weekly one - the two aren't interchangeable).
 *
 * Also covers the FLSA "blended rate" / weighted-average method required
 * when an employee works multiple hourly rates within the same workweek
 * (e.g. covering two different roles or shifts).
 */
public class OvertimeCalculator {

    private static final double WEEKLY_OVERTIME_THRESHOLD_HOURS = 40.0;
    private static final double OVERTIME_MULTIPLIER = 1.5;

    public double calculateWeeklyRegularPay(double hourlyRate, double totalHoursWorkedInWeek) {
        validateNonNegative(hourlyRate, "hourlyRate");
        validateNonNegative(totalHoursWorkedInWeek, "totalHoursWorkedInWeek");
        double regularHours = Math.min(totalHoursWorkedInWeek, WEEKLY_OVERTIME_THRESHOLD_HOURS);
        return round2(regularHours * hourlyRate);
    }

    public double calculateWeeklyOvertimePay(double hourlyRate, double totalHoursWorkedInWeek) {
        validateNonNegative(hourlyRate, "hourlyRate");
        validateNonNegative(totalHoursWorkedInWeek, "totalHoursWorkedInWeek");
        double overtimeHours = Math.max(0, totalHoursWorkedInWeek - WEEKLY_OVERTIME_THRESHOLD_HOURS);
        return round2(overtimeHours * hourlyRate * OVERTIME_MULTIPLIER);
    }

    /**
     * FLSA weighted-average ("blended rate") overtime: the regular rate is
     * total straight-time earnings divided by total hours across all rates
     * worked that week; overtime hours (over 40) earn an extra half of
     * that blended rate on top of the straight-time pay already included.
     *
     * @param hourlyRates     rate paid for each block of hours
     * @param hoursAtEachRate hours worked at the corresponding rate, same
     *                        index/length as hourlyRates
     * @return total pay for the week (straight time at each rate + the
     *         half-time overtime premium)
     */
    public double calculateBlendedWeeklyPay(double[] hourlyRates, double[] hoursAtEachRate) {
        if (hourlyRates == null || hoursAtEachRate == null) {
            throw new IllegalArgumentException("hourlyRates and hoursAtEachRate must not be null");
        }
        if (hourlyRates.length != hoursAtEachRate.length || hourlyRates.length == 0) {
            throw new IllegalArgumentException("hourlyRates and hoursAtEachRate must be the same non-zero length");
        }

        double totalStraightTimeEarnings = 0;
        double totalHours = 0;
        for (int i = 0; i < hourlyRates.length; i++) {
            validateNonNegative(hourlyRates[i], "hourlyRates[" + i + "]");
            validateNonNegative(hoursAtEachRate[i], "hoursAtEachRate[" + i + "]");
            totalStraightTimeEarnings += hourlyRates[i] * hoursAtEachRate[i];
            totalHours += hoursAtEachRate[i];
        }

        double overtimeHours = Math.max(0, totalHours - WEEKLY_OVERTIME_THRESHOLD_HOURS);
        if (overtimeHours == 0) {
            return round2(totalStraightTimeEarnings);
        }

        double blendedRate = totalStraightTimeEarnings / totalHours;
        double overtimePremium = overtimeHours * blendedRate * 0.5;

        return round2(totalStraightTimeEarnings + overtimePremium);
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
