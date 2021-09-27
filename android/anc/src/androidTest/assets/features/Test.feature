@CucmberTest
Feature: Testing Cucumber

  @cucumberTest
  Scenario: Successful login
    Given I start the application
    When I enter valid email HelloWorld
    And I click sign in button
