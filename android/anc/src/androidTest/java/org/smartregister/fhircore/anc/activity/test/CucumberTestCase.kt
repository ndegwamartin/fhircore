package org.smartregister.fhircore.anc.activity.test

import cucumber.api.CucumberOptions
@CucumberOptions(features = ["features"],
    glue = ["com.sniper.bdd.cucumber.steps"],
    tags = ["@e2e", "@smoke"])
@SuppressWarnings("unused")

class CucumberTestCase {
}