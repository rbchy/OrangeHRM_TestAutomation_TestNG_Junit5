package tests;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-Driven Testing: multi-state tax withholding matrix read from an
 * actual .xlsx file (src/test/resources/testdata/StateTaxMatrix.xlsx) via
 * Apache POI, rather than a hardcoded array. Add a row to that spreadsheet
 * to add another state/bracket to the regression run - no code change
 * needed.
 */
public class MultiStateTaxTest {

    private static final String TAX_MATRIX_RESOURCE = "testdata/StateTaxMatrix.xlsx";

    @DataProvider(name = "USStateTaxMatrix")
    public Object[][] getTaxData() throws IOException {
        List<Object[]> rows = new ArrayList<>();

        try (InputStream in = MultiStateTaxTest.class.getClassLoader().getResourceAsStream(TAX_MATRIX_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Could not find test data file on classpath: " + TAX_MATRIX_RESOURCE);
            }

            try (XSSFWorkbook workbook = new XSSFWorkbook(in)) {
                int lastRowNum = workbook.getSheetAt(0).getLastRowNum();

                for (int rowIndex = 1; rowIndex <= lastRowNum; rowIndex++) {
                    Row row = workbook.getSheetAt(0).getRow(rowIndex);
                    if (row == null) {
                        continue;
                    }

                    String state = getStringCell(row, 0);
                    double grossPay = getNumericCell(row, 1);
                    double stateTaxRate = getNumericCell(row, 2);
                    double expectedTaxWithholding = getNumericCell(row, 3);

                    rows.add(new Object[] { state, grossPay, stateTaxRate, expectedTaxWithholding });
                }
            }
        }

        return rows.toArray(new Object[0][]);
    }

    @Test(dataProvider = "USStateTaxMatrix", groups = { "Regression" })
    public void verifyStateTaxCalculations(String state, double grossPay, double stateTaxRate,
            double expectedTaxWithholding) {
        System.out.println("Executing payroll regression matrix run for State: " + state);

        double computedTax = (grossPay * stateTaxRate) / 100;

        Assert.assertEquals(computedTax, expectedTaxWithholding, 0.01,
                "Statutory state tax calculation mismatch for: " + state);
    }

    private String getStringCell(Row row, int index) {
        Cell cell = row.getCell(index);
        return cell == null ? null : cell.getStringCellValue();
    }

    private double getNumericCell(Row row, int index) {
        Cell cell = row.getCell(index);
        return cell == null ? 0.0 : cell.getNumericCellValue();
    }
}
