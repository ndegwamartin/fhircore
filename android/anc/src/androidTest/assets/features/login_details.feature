@login_details
Feature: Enter login details

  @all
  Scenario: Successful login
    Given I start the application
    When I click email field
    And I enter valid email demo
    And I close the keyboard
    And I click password field
    And I enter valid password Amani123
    And I close the keyboard
    And I click sign in button
    Then I expect to see successful login message
