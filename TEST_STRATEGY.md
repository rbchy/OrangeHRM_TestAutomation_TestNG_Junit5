# Test Strategy — HRM Payroll Automation

**Author:** RB Chowdhury
**System under test:** [OrangeHRM demo](https://opensource-demo.orangehrmlive.com/) (UI, real) + a payroll/tax/insurance backend (no real deployment exists, so it's stubbed locally with WireMock — see [Test Data & Environment Strategy](#test-data--environment-strategy))

## Why this document exists

A test suite without a stated strategy just looks like a list of things that happened to get automated. This document is the reasoning behind *why* these tests, in this shape, and — just as importantly — why several obvious things were deliberately left out. The goal isn't maximum test count; it's covering the paths where a defect would actually cost money, break compliance, or leak sensitive data, and being explicit about what's traded off to get there.

---

## 1. Risk-based prioritization

Every module and feature was assessed against one question: **if this breaks, what's the blast radius?**

| Risk tier | Meaning | Modules |
|---|---|---|
| **Critical** | Wrong output = wrong money paid, a compliance violation, or exposed PII | Payroll deductions, federal/state tax, 401(k), overtime, insurance premiums, direct deposit |
| **High** | Feeds into critical calculations or gates who can see/change them | Time (timesheets), Leave (balance affects pay), PIM (salary records), Admin (role-based access) |
| **Medium** | Real HR workflow, but doesn't touch pay or compliance | Recruitment, Performance |
| **Low** | No business logic worth the automation cost | Buzz (social feed), Directory (read-only listing), Maintenance |

**Consequence:** Critical and High tier items get the deepest coverage — including real 2026 IRS figures rather than placeholder math (see [Section 4](#4-domain-depth-over-breadth)). Medium tier gets functional smoke coverage at most. Low tier is explicitly out of scope. This is also why `TimeClockNegativeTest` (Selenium against a fictional time-clock host) was retired in favor of `unit/WorkHoursValidatorTest` — the *business rule* (hours can't exceed 24 or go negative) is Critical/High risk, but the specific UI it happened to be bolted onto never existed anywhere reachable. Testing the rule directly is both more honest and more reliable than testing a UI that doesn't exist.

---

## 2. Test pyramid

| Layer | Count | Tooling | Feedback speed |
|---|---|---|---|
| Unit | 48 | JUnit 5 + Mockito | ~1s for the whole layer |
| System (UI / API / Integration / E2E / BDD) | 18 | TestNG, Selenium, Cucumber, REST Assured | ~15-20s |

Roughly 2.7 unit tests for every system-level test. This is intentional, not incidental: payroll math (tax brackets, 401(k) limits, overtime, dues) is exactly the kind of logic that's cheap and fast to verify exhaustively at the unit level — every boundary, every edge case — without paying for a browser or a mock server per case. System-level tests then only need to prove the *pieces are wired together correctly* (a real UI flow, a real API contract, a real multi-step chain), not re-verify every calculation branch. Pushing coverage down the pyramid keeps the suite fast enough to run on every commit (see [Section 6](#6-cicd-strategy)) instead of becoming the kind of slow UI-heavy suite teams eventually stop running.

---

## 3. Test type coverage

| Category | Approach | Where |
|---|---|---|
| Smoke | Cucumber `@Smoke` login flow, real OrangeHRM demo | `stepDefinitions/LoginSteps.java` |
| Sanity | Narrow, fast, single-endpoint check (models a post-hotfix verification) | `tests/PayStubSanityTest.java` |
| Regression | Multi-state tax matrix + API contracts, run as one suite | `tests/MultiStateTaxTest.java`, `tests/TaxInsuranceApiTest.java`, `tests/InsuranceApiTest.java` |
| Positive / Negative | Valid vs. invalid payroll/hours inputs | `unit/WorkHoursValidatorTest.java`, `unit/PayrollCalculatorTest.java` |
| Unit | Tax brackets, 401(k) limits, overtime, dues — isolated, no I/O | `src/main/java/payroll/**`, `src/test/java/unit/**` |
| Integration | Time-clock export → payroll import data hand-off | `tests/TimeClockPayrollIntegrationTest.java` |
| API | 200 OK + JSON schema conformance | `tests/InsuranceApiTest.java` |
| Functional / UI | Page Object Model - login/dashboard, PIM > Add Employee, Leave > Apply, Leave > Entitlements (test-setup helper) | `pages/LoginPage.java`, `pages/PimAddEmployeePage.java`, `pages/LeaveApplyPage.java`, `pages/LeaveEntitlementPage.java` |
| Positive / Negative (UI) | Valid employee add vs. missing required fields / duplicate Employee Id; valid leave request vs. overlapping request | `tests/PimEmployeeValidationTest.java`, `tests/LeaveApplicationTest.java` |
| End-to-End | Punch → approval → deduction → direct deposit, one chained flow | `tests/PayrollE2ETest.java` |
| Data-Driven | 11-state tax matrix read from a real `.xlsx` via Apache POI | `testdata/StateTaxMatrix.xlsx` |
| Mobile Automation | Appium scaffold — **not runnable**, no real app exists | `mobile/ClockInGeofenceMobileTest.java` |
| Load & Performance | JMeter plan — **not runnable**, no real staging target exists | `performance/PayrollClockOutLoadTest.jmx` |

The last two are marked as templates rather than working tests deliberately (see [Section 5](#5-explicitly-out-of-scope)) — a scaffold that's honest about what it isn't is more credible than a test that appears to pass but isn't actually exercising anything real.

---

## 4. Domain depth over breadth

Given a choice between covering every payroll feature shallowly or a few features to regulatory accuracy, this project chose depth on three:

- **Federal tax** (`FederalTaxBracketCalculator`) — the real 2026 IRS Publication 15-T Standard Withholding bracket table, not a flat-rate placeholder.
- **401(k)** (`RetirementContributionCalculator`) — real 2026 IRS annual limits, including the SECURE 2.0 age-60-63 "super catch-up" tier most payroll demos never model.
- **Overtime** (`OvertimeCalculator`) — both the daily threshold and the FLSA weekly-40-hour rule, plus the weighted-average "blended rate" method required for employees working multiple pay rates in one week.

The reasoning: an interviewer evaluating payroll-QA candidates can tell the difference between "I wrote a test that asserts `tax == gross * 0.1`" and "I modeled the actual bracket schedule the IRS publishes." The former proves you can write assertions; the latter proves domain research and inclination to get compliance-sensitive numbers right, not just green checkmarks.

---

## 5. Explicitly out of scope

Being upfront about gaps is part of the strategy, not a weakness in it.

| Item | Why it's out | What would bring it in |
|---|---|---|
| Buzz, Directory, Maintenance modules | Low business risk, no payroll/compliance logic | N/A — not planned |
| Mobile Automation | No real employee app (APK/IPA) exists | A real app build + Appium server |
| Load & Performance | No real staging server exists | A real deployment target |
| Wage garnishment (court-ordered deductions) | Not yet modeled | Planned — legally must take priority over 401(k)/insurance deductions |
| Employee lifecycle edge cases (new-hire proration, termination final pay incl. PTO payout, mid-period rate changes) | Not yet modeled | Planned |
| Multi-state tax reciprocity | Only flat per-state rates modeled so far, no cross-state agreements | Possible future addition |
| Self-healing locators, visual regression, WCAG accessibility scanning | Discussed as valuable additions, not yet implemented | See conversation history / future backlog |
| Time (timesheet edit-lock behavior) | Requires a second, non-admin/supervisor demo account and workflow state not available on the public single-login demo | A second demo account or a self-hosted OrangeHRM instance |
| My Info (self-service field RBAC - which fields an employee vs. admin can edit) | Same constraint as Time - needs a real non-admin login to test the restricted view | Same as above |

**A note on the PIM/Leave locators added in this phase:** `PimAddEmployeePage`, `LeaveApplyPage`, and `LeaveEntitlementPage` were built from OrangeHRM's well-documented public demo DOM structure rather than a fresh live inspection - the browser automation tooling used earlier in this session to inspect the live app became unresponsive to clicks/typing partway through. If a locator has drifted from the live app, Selenium will fail loudly and specifically at that line, which should make it quick to patch. Treat `PimEmployeeValidationTest` and `LeaveApplicationTest` as needing one live confirmation run before relying on them in CI.

**Root cause finally found for the Leave Type dropdown timeouts:** what looked like a slow/flaky dropdown across several fix attempts (wider waits, stale-element handling, click-intercept retries) turned out to be a data problem, confirmed by an Allure failure screenshot - the Apply Leave page was rendering "No Leave Types with Leave Balance" because the Admin demo account had zero leave entitlement, so no dropdown ever existed to become clickable. Fixed two ways: `LeaveApplicationTest.ensureLeaveEntitlementExists()` (`@BeforeClass`) now grants the Admin account an entitlement via a new `LeaveEntitlementPage` before either Leave test runs, and `LeaveApplyPage.selectFirstAvailableLeaveType()` now checks for that message and fails fast with a clear diagnostic instead of burning the full 30s wait if it ever recurs (e.g. the entitlement gets consumed by other test runs on the shared demo over time).

---

## 6. CI/CD strategy

Every push and pull request to `main` runs the full suite via GitHub Actions (`.github/workflows/ci.yml`):
`mvn clean verify` (unit tests via Surefire, the TestNG suite via Failsafe — kept as separate plugin executions specifically because JUnit 5 and TestNG on the same classpath produce an ambiguous provider auto-detection otherwise), followed by Allure report generation published as a build artifact. Chrome runs headless in CI (`CI` env var toggles this in `LoginSteps.java`) since GitHub-hosted runners have no display server.

The suite runs in well under 20 seconds locally, which is what makes "run on every push" realistic instead of aspirational.

---

## 7. Test data & environment strategy

There is no real payroll/tax/insurance backend to test against — the original placeholders (`api.orangehrm-payroll-engine.local`, etc.) never resolved to anything. Rather than skip API/Integration/E2E coverage entirely, every backend call in this project is stubbed locally with **WireMock**, started on a dynamic port per test class. This means:

- Tests are deterministic — no flakiness from a shared demo environment's state changing between runs.
- Tests run with zero external network dependency (aside from the one real UI smoke test against the public OrangeHRM demo).
- The contract (request/response shape, JSON schema) is still genuinely verified, even though the implementation behind it is simulated.

The one real external dependency — the OrangeHRM demo login flow — is intentionally kept as the single Smoke test, both because it's the only feature in this taxonomy with a real, stable, public target, and to demonstrate the project isn't *only* testing against mocks.

---

## 8. Reporting

**Allure** (`allure-testng`, `allure-junit5`, `allure-cucumber7-jvm`) aggregates results from all three test frameworks into one report — `mvn allure:serve` for local viewing, published as a CI artifact on every run. Pinned to the Allure 2 (Java-based) report runtime rather than the plugin's newer Node-based default, which had an unresolved path-handling bug at the time this was set up.
