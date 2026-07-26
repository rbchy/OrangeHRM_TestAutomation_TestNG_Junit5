Feature: AI-Enhanced HRM Login and Dashboard Validation
  As an HR Admin
  I want to login to the HRM portal using AI self-healing locators
  So that minor UI changes do not break my automation pipeline.

  @Smoke @AI-Driven
  Scenario: Successful login with valid credentials and verification of dashboard
    Given User opens the HRM login page
    When User enters valid username "Admin" and password "admin123"
    And User clicks on the login button
    Then User should be redirected to the "Dashboard" page