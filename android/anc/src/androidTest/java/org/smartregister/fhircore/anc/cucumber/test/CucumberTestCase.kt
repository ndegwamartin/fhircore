package org.smartregister.fhircore.anc.cucumber.test

import cucumber.api.CucumberOptions
@CucumberOptions(features = ["features"],
    glue = ["org.smartregister.fhircore.anc.cucumber.steps"],
    tags = ["@e2e", "@smoke"])
@SuppressWarnings("unused")

class CucumberTestCase {
}