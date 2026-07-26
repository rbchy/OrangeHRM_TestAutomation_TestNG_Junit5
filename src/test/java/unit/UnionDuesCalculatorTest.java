package unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import payroll.UnionDuesCalculator;
import payroll.UnionDuesCalculator.DuesType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Monthly union dues deduction: flat fee or percentage of gross pay. */
class UnionDuesCalculatorTest {

    private UnionDuesCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new UnionDuesCalculator();
    }

    @Test
    @DisplayName("Positive: a flat monthly dues amount is deducted as-is")
    void calculateDues_flatAmount() {
        assertEquals(45.00, calculator.calculateDues(4_000.00, DuesType.FLAT, 45.00), 0.001);
    }

    @Test
    @DisplayName("Positive: percentage-of-gross dues are computed from gross pay")
    void calculateDues_percentageOfGross() {
        assertEquals(80.00, calculator.calculateDues(4_000.00, DuesType.PERCENTAGE_OF_GROSS, 2.0), 0.001);
    }

    @Test
    @DisplayName("Edge case: flat dues never exceed the employee's gross pay for the period")
    void calculateDues_flatAmountExceedsGrossPay_isCappedAtGrossPay() {
        double dues = calculator.calculateDues(200.00, DuesType.FLAT, 500.00);
        assertEquals(200.00, dues, 0.001);
    }

    @Test
    @DisplayName("Negative: dues type must not be null")
    void calculateDues_nullType_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateDues(4_000.00, null, 45.00));
    }

    @Test
    @DisplayName("Negative: a negative gross pay is rejected")
    void calculateDues_negativeGrossPay_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateDues(-100.00, DuesType.FLAT, 45.00));
    }
}
