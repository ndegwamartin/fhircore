package org.smartregister.fhircore.anc.test


import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import com.fasterxml.jackson.core.JsonPointer.SEPARATOR
import cucumber.api.CucumberOptions
import cucumber.api.android.CucumberInstrumentationCore
import java.io.File


@CucumberOptions(features = ["features"],
        glue = ["org.smartregister.fhircore.anc.steps"],
        tags = ["@e2e", "@smoke","@all"])
@Suppress("unused")

class CucumberTestRunner : AndroidJUnitRunner() {

private val instrumentationCore: CucumberInstrumentationCore? = CucumberInstrumentationCore(this)

//    override fun onCreate(bundle: Bundle) {
//        println("class invoked ")
//        val optionsInBundle = setupCucumberOptionsInBundle(bundle)
//        super.onCreate(optionsInBundle)
//    }
//
//    private fun setupCucumberOptionsInBundle(bundle: Bundle): Bundle {
//        val storageDirectory = getStorageDirectory()
//
//        bundle.putString("plugin", createRerunReportOptions(storageDirectory))
//        return bundle
//    }
//
//    private fun getStorageDirectory(): String {
//        val directory = targetContext.getExternalFilesDir(null)
//        return File( directory,"reports").absolutePath
//    }
//
//    private fun createRerunReportOptions(storageDirectory: String): String {
//        return "" +
//                "junit:" + storageDirectory + "/cucumber-junit.xml" + SEPARATOR +
//                "json:" + storageDirectory + "/cucumber-report-composite.json" + SEPARATOR +
//                "html:" + storageDirectory + "/cucumber-report.html" + SEPARATOR +
//                "rerun:" + storageDirectory + "/rerun.txt" + SEPARATOR +
//                "pretty" + SEPARATOR
//    }

    override fun onCreate(bundle: Bundle?) {
       instrumentationCore!!.create(bundle)
        super.onCreate(bundle)
    }

    override fun onStart() {
        waitForIdleSync()
       instrumentationCore!!.start()
    }
}