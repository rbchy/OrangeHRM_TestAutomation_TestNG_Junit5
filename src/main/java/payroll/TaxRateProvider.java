package payroll;

/**
 * Source of tax rates used by {@link PayrollCalculator}. Kept as a separate
 * collaborator (rather than hard-coded inside the calculator) so unit tests
 * can mock it with Mockito and test the calculator's math in isolation from
 * wherever rates actually come from (a rates table, an external tax-agency
 * API, a config file, etc.).
 */
public interface TaxRateProvider {

    /**
     * @param state full US state name, e.g. "California"
     * @return the state income tax rate as a percentage (e.g. 9.3 for 9.3%)
     */
    double getStateTaxRate(String state);

    /**
     * @return the flat federal withholding rate as a percentage
     */
    double getFederalTaxRate();
}
