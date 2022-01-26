Feature: Enter login details
  @smoke
    @e2e
  Scenario Outline: Successful login
    Given I start the application
    When I click username field
    And I enter valid username <username>
    And I close the keyboard
    And I click password field
    And I enter valid password <password>
    And I close the keyboard
    And I click sign in button
    Then I expect to see successful login message
    Examples:
      | username        | password |
      | demo | Amani123   |