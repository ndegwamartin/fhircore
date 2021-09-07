package org.smartregister.fhircore.anc.test


import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import cucumber.api.CucumberOptions
import cucumber.api.android.CucumberInstrumentationCore


@CucumberOptions(features = ["features"],
        glue = ["org.smartregister.fhircore.anc.steps"],
        tags = ["@e2e", "@smoke","@all"])
@Suppress("unused")

class CucumberTestRunner : AndroidJUnitRunner() {

private val instrumentationCore: CucumberInstrumentationCore? = CucumberInstrumentationCore(this)

    override fun onCreate(bundle: Bundle?) {
        instrumentationCore!!.create(bundle)
        super.onCreate(bundle)
    }

    override fun onStart() {
        waitForIdleSync()
        instrumentationCore!!.start()
    }
}