package org.smartregister.fhircore.anc.test

import android.os.Bundle
import android.os.Environment
import com.fasterxml.jackson.core.JsonPointer.SEPARATOR
import io.cucumber.junit.CucumberOptions
import io.cucumber.android.runner.CucumberAndroidJUnitRunner
import org.junit.BeforeClass
import java.io.File

@CucumberOptions(features = ["features"],
        glue = ["org.smartregister.fhircore.anc.steps"],
        tags = ["@all"])
@Suppress("unused")
class CucumberTestRunner: CucumberAndroidJUnitRunner() {

@BeforeClass
override fun onCreate(bundle: Bundle) {
    println("class invoked ")
    val optionsInBundle = setupCucumberOptionsInBundle(bundle)
    super.onCreate(optionsInBundle)
}

    private fun setupCucumberOptionsInBundle(bundle: Bundle): Bundle {
        val storageDirectory = getStorageDirectory()
        bundle.putString("plugin", createRerunReportOptions(storageDirectory))
        return bundle
    }

    private fun getStorageDirectory(): String {
        val directory = targetContext.getExternalFilesDir(null)
        println("************************************************************************************* ")
        println(Environment.getExternalStorageDirectory().getAbsolutePath())
        println("************************************************************************************* ")
        return File(directory, "reports").absolutePath
    }

    private fun createRerunReportOptions(storageDirectory: String): String {
        return "" +
                "junit:" + storageDirectory + "/cucumber-junit.xml" + SEPARATOR +
                "json:" + storageDirectory + "/cucumber-report-composite.json" + SEPARATOR +
                "html:" + storageDirectory + "/cucumber-report.html" + SEPARATOR +
                "rerun:" + storageDirectory + "/rerun.txt" + SEPARATOR +
                "pretty" + SEPARATOR
    }
}
