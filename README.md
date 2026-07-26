# OrangeHRM Payroll Automation

AI-enhanced QA automation framework for a Human Resource Management (HRM) and Payroll system, built around the public [OrangeHRM demo](https://opensource-demo.orangehrmlive.com/) plus a locally-stubbed payroll/tax/insurance backend. Covers UI, API, integration, end-to-end, data-driven, and unit-level testing across payroll, federal/state tax, 401(k), overtime, and insurance domains.

**Tester:** RB Chowdhury

[![GitHub](https://img.shields.io/badge/GitHub-your--github--username-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/your-github-username)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-your--linkedin-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://linkedin.com/in/your-linkedin)

[![CI](https://github.com/your-github-username/hrm-payroll-automation/actions/workflows/ci.yml/badge.svg)](https://github.com/your-github-username/hrm-payroll-automation/actions/workflows/ci.yml)

---

## Tools & Technologies

<p>
  <img src="https://img.shields.io/badge/Java-11-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 11" />
  <img src="https://img.shields.io/badge/Maven-3.x-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven" />
  <img src="https://img.shields.io/badge/Selenium-4.22-43B02A?style=for-the-badge&logo=selenium&logoColor=white" alt="Selenium" />
  <img src="https://img.shields.io/badge/TestNG-7.10-EF2D5E?style=for-the-badge&logo=testinglibrary&logoColor=white" alt="TestNG" />
  <img src="https://img.shields.io/badge/Cucumber-7.18-23D96C?style=for-the-badge&logo=cucumber&logoColor=white" alt="Cucumber" />
  <img src="https://img.shields.io/badge/REST_Assured-5.4-3A5FCD?style=for-the-badge&logo=swagger&logoColor=white" alt="REST Assured" />
</p>
<p>
  <img src="https://img.shields.io/badge/JUnit_5-5.14-25A162?style=for-the-badge&logo=junit5&logoColor=white" alt="JUnit 5" />
  <img src="https://img.shields.io/badge/Mockito-5.23-78C257?style=for-the-badge&logo=java&logoColor=white" alt="Mockito" />
  <img src="https://img.shields.io/badge/WireMock-3.13-3D3D3D?style=for-the-badge&logo=wire&logoColor=white" alt="WireMock" />
  <img src="https://img.shields.io/badge/Apache_POI-5.2-D22128?style=for-the-badge&logo=apache&logoColor=white" alt="Apache POI" />
  <img src="https://img.shields.io/badge/Appium-9.2-662D91?style=for-the-badge&logo=appium&logoColor=white" alt="Appium" />
  <img src="https://img.shields.io/badge/Apache_JMeter-5.6-D22128?style=for-the-badge&logo=apachejmeter&logoColor=white" alt="Apache JMeter" />
</p>
<p>
  <img src="https://img.shields.io/badge/Allure_Report-2.35-FF5A5F?style=for-the-badge&logo=qameta&logoColor=white" alt="Allure Report" />
  <img src="https://img.shields.io/badge/GitHub_Actions-CI%2FCD-2088FF?style=for-the-badge&logo=githubactions&logoColor=white" alt="GitHub Actions" />
  <img src="https://img.shields.io/badge/JSON_Schema-Validator-000000?style=for-the-badge&logo=json&logoColor=white" alt="JSON Schema Validator" />
</p>

---

## Test Architecture

This project intentionally maps to a full QA test taxonomy rather than a single style of testing. Each category below is a deliberate design choice, not filler.

| Category | How it's covered | Where |
|---|---|---|
| Smoke | Cucumber `@Smoke` login flow against the real OrangeHRM demo | `stepDefinitions/LoginSteps.java`, `features/Login.feature` |
| Sanity | Fast, narrow post-bugfix check on a single endpoint | `tests/PayStubSanityTest.java` |
| Regression | Multi-state tax matrix + API contract checks, run as one suite | `tests/MultiStateTaxTest.java`, `tests/TaxInsuranceApiTest.java`, `tests/InsuranceApiTest.java` |
| Positive / Negative | Valid vs. invalid work-hours and payroll inputs | `unit/WorkHoursValidatorTest.java`, `unit/PayrollCalculatorTest.java` |
| Unit | Payroll math, tax brackets, 401(k) limits, overtime - isolated with JUnit 5 + Mockito | `src/main/java/payroll/**`, `src/test/java/unit/**` |
| Integration | Time-clock export -> payroll import data hand-off | `tests/TimeClockPayrollIntegrationTest.java` |
| API | Response code + JSON schema validation on payroll/insurance endpoints | `tests/InsuranceApiTest.java`, `src/test/resources/schemas/` |
| Functional / UI | Page Object Model - login/dashboard, PIM > Add Employee, Leave > Apply | `pages/LoginPage.java`, `pages/PimAddEmployeePage.java`, `pages/LeaveApplyPage.java` |
| Positive / Negative (UI) | Valid employee add vs. missing required fields / duplicate Employee Id; valid leave request vs. overlapping request | `tests/PimEmployeeValidationTest.java`, `tests/LeaveApplicationTest.java` |
| Mobile Automation | Appium scaffold for geofenced clock-in/out (template - no real app available) | `src/test/java/mobile/ClockInGeofenceMobileTest.java` |
| End-to-End | Punch -> approval -> tax deduction -> direct deposit, one chained flow | `tests/PayrollE2ETest.java` |
| Data-Driven | 11-state tax matrix read from an actual `.xlsx` via Apache POI | `src/test/resources/testdata/StateTaxMatrix.xlsx` |
| Load & Performance | JMeter plan simulating 50,000 concurrent clock-outs (template - no real staging target yet) | `performance/PayrollClockOutLoadTest.jmx` |

All backend endpoints (payroll deductions, pay stubs, insurance, time clock, direct deposit) are stubbed locally with **WireMock** - there's no real payroll backend to point at, so every API/Integration/E2E test is deterministic and runs with zero external dependencies.

### Domain-depth additions

Rather than shallow coverage everywhere, a few payroll rules are modeled with real regulatory figures instead of placeholders:

- **Federal tax** - actual 2026 IRS Publication 15-T bracket table (`payroll/FederalTaxBracketCalculator.java`)
- **401(k)** - real 2026 IRS annual contribution limits, standard/catch-up/super catch-up tiers, plus employer match (`payroll/RetirementContributionCalculator.java`)
- **Overtime** - FLSA weekly (40hr) rule and the blended/weighted-average rate method for multi-rate weeks (`payroll/OvertimeCalculator.java`)
- **Union dues** - flat-fee or percentage-of-gross deduction (`payroll/UnionDuesCalculator.java`)

### PIM & Leave coverage

`PimEmployeeValidationTest` and `LeaveApplicationTest` run against the real OrangeHRM demo (these modules genuinely exist there, unlike the payroll/tax backend). Their Page Object locators (`PimAddEmployeePage`, `LeaveApplyPage`) are based on OrangeHRM's documented DOM structure rather than a fresh live inspection - verify against the live app if a locator drifts. Both write real records to the shared public demo with no UI-based cleanup, and run in their own sequential, isolated TestNG `<test>` block for that reason. **Time** (timesheet edit-lock) and **My Info** (self-service field RBAC) were deliberately left out of this phase - both require a second, non-admin demo account and a supervisor workflow state that isn't available on the public single-login demo. See `TEST_STRATEGY.md` for the full reasoning.

---

## Project Structure

```
hrm-payroll-automation/
├── .github/workflows/ci.yml        # GitHub Actions: build, test, Allure report
├── performance/                    # JMeter load test plan (standalone, not part of mvn build)
├── src/main/java/
│   ├── pages/                      # Page Object Model (Selenium)
│   └── payroll/                    # Payroll/tax/insurance business logic
├── src/test/java/
│   ├── stepDefinitions/            # Cucumber step definitions
│   ├── runner/                     # Cucumber TestNG runner
│   ├── tests/                      # TestNG: UI, API, Sanity, Integration, E2E
│   ├── unit/                       # JUnit 5 + Mockito unit tests
│   └── mobile/                     # Appium scaffold (excluded from the build)
├── src/test/resources/
│   ├── features/                   # Cucumber .feature files
│   ├── schemas/                    # JSON schemas for API contract tests
│   └── testdata/                   # Data-driven test fixtures (.xlsx)
├── testng.xml                      # TestNG suite definition
└── pom.xml
```

---

## Running the Suite

```bash
# JUnit 5 unit tests only (fast, isolated business logic)
mvn clean test

# Everything: unit tests + full TestNG suite (UI, API, Integration, E2E, Cucumber)
mvn clean verify
```

> `mvn test` and `mvn verify` are intentionally split. JUnit 5 and TestNG both live on the test classpath, so unit tests run under Surefire (`mvn test`) while the TestNG suite runs under Failsafe (`mvn verify`), avoiding a test-provider conflict between the two frameworks.

### Viewing the Allure report

```bash
mvn clean verify        # populates target/allure-results
mvn allure:serve        # opens the report in your browser
# or, for a static copy:
mvn allure:report        # target/site/allure-maven-plugin/index.html
```

---

## CI/CD

Every push and pull request to `main` runs the full suite via GitHub Actions (`.github/workflows/ci.yml`): unit tests, the TestNG suite (headless Chrome in CI), and an Allure report published as a build artifact.

---

## Known Limitations

- **Mobile Automation** and **Load & Performance** are templates, not live tests - there's no real mobile app or staging server to run them against yet. Both are clearly marked and excluded from `mvn verify` so they don't affect build status.
- `tests/TimeClockNegativeTest.java` (Selenium against a time-clock UI) is tagged `Flaky` and excluded for the same reason - the equivalent business rule is covered for real in `unit/WorkHoursValidatorTest.java` instead.
