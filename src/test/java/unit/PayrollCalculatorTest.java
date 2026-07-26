package unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import payroll.PayrollCalculator;
import payroll.TaxRateProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit Testing (federal/state tax math) + Positive Testing (regular pay for
 * a valid full workday). TaxRateProvider is mocked with Mockito so the
 * calculator's arithmetic is verified in isolation from wherever tax rates
 * actually come from.
 */
@ExtendWith(MockitoExtension.class)
class PayrollCalculatorTest {

    @Mock
    private TaxRateProvider taxRateProvider;

    private PayrollCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PayrollCalculator(taxRateProvider);
    }

    @Test
    @DisplayName("Positive: a standard 8-hour day pays exactly hourlyRate x 8, no overtime")
    void calculateRegularPay_eightHours_returnsExactPay() {
        double pay = calculator.calculateRegularPay(25.0, 8.0);
        assertEquals(200.00, pay, 0.001);
    }

    @Test
    @DisplayName("Hours beyond the 8-hour threshold are paid at 1.5x")
    void calculateOvertimePay_tenHours_appliesOvertimeForHoursBeyondEight() {
        double overtimePay = calculator.calculateOvertimePay(25.0, 10.0);
        assertEquals(75.00, overtimePay, 0.001); // 2 overtime hours x 25 x 1.5
    }

    @Test
    @DisplayName("Gross daily pay combines regular and overtime portions")
    void calculateGrossDailyPay_combinesRegularAndOvertime() {
        double grossPay = calculator.calculateGrossDailyPay(20.0, 9.0);
        assertEquals(190.00, grossPay, 0.001); // 8h regular (160) + 1h overtime (30)
    }

    @Test
    @DisplayName("State tax uses the rate returned by TaxRateProvider (California, 9.3%)")
    void calculateStateTax_california_usesMockedRate() {
        when(taxRateProvider.getStateTaxRate("California")).thenReturn(9.3);

        double tax = calculator.calculateStateTax(7500.00, "California");

        assertEquals(697.50, tax, 0.001);
        verify(taxRateProvider).getStateTaxRate("California");
    }

    @Test
    @DisplayName("State tax is zero for no-income-tax states (Texas)")
    void calculateStateTax_texas_zeroRate() {
        when(taxRateProvider.getStateTaxRate("Texas")).thenReturn(0.0);

        double tax = calculator.calculateStateTax(7500.00, "Texas");

        assertEquals(0.00, tax, 0.001);
    }

    @Test
    @DisplayName("Federal tax uses the flat rate returned by TaxRateProvider")
    void calculateFederalTax_usesMockedRate() {
        when(taxRateProvider.getFederalTaxRate()).thenReturn(12.0);

        double tax = calculator.calculateFederalTax(8500.00);

        assertEquals(1020.00, tax, 0.001);
    }

    @Test
    @DisplayName("401k contribution is grossPay x deferral percentage")
    void calculate401kContribution_sixPercent() {
        double contribution = calculator.calculate401kContribution(8500.00, 6.0);
        assertEquals(510.00, contribution, 0.001);
    }

    @Test
    @DisplayName("Negative: negative hours are rejected before any pay is calculated")
    void calculateRegularPay_negativeHours_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateRegularPay(25.0, -1.0));
    }
}
