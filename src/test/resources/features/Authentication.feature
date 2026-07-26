Feature: Core Portal Authentication Smoke Suite

  Scenario: Verify HR Admin can successfully login and access the Dashboard
    Given the HR Admin navigates to the core portal login page
    When the admin enters valid credentials
    Then the admin should be redirected to the Dashboard page within 3 seconds
    And all primary dashboard metrics widgets should be visible