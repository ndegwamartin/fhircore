Feature: Enter login details
  @smoke
    @e2e
  Scenario : Successful login
    Given I start the application
    When Application is started
    Then I expect to see Login button