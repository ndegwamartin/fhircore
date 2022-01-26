package org.smartregister.fhircore.anc.activity.test

class RunCukesTest {
    var passedCount = 0
    var failedCount = 0
    //var skippedCount = 0
    fun getPassCount(): Int? {
        return passedCount
    }

    fun getFailCount(): Int? {
        return failedCount
    }
/*
    fun getSkippedCount(): Int? {
        return skippedCount
    }

 */

    fun setPassCount(passCount: Int) {
        passedCount = passCount
    }

    fun setFailCount(failCount: Int) {
        failedCount = failCount
    }

    fun setSkippedCount(skipCount: Int) {
       // skippedCount = skipCount
    }

    var automationSteps: ArrayList<String>? = null
    var expectedResults: ArrayList<String>? = null
/*
    @BeforeClass
    @Throws(SQLException::class)
    fun beforeClass() {
        if (ConfigProperties.isReportingEnable.toLowerCase().equals("true")) Reports.startReport()
        automationSteps = ArrayList()
        expectedResults = ArrayList()
    }

    @AfterClass
    @Throws(IOException::class, MessagingException::class, APIException::class)
    fun AfterClass() {
        //Reporter.loadXMLConfig(new File(Reports.getReportConfigPath()));
        if (ConfigProperties.isReportingEnable.toLowerCase().equals("true")) {
            Reports.getExtentReport().flush()
            Reports.getExtentReport().close()
        }
        if (ConfigProperties.logTestRail.toLowerCase().equals("true")) {
            TestRail.createSuite()
            TestRail.updateTestRail()
        }
        if (sendEmail.toLowerCase().equals("true")) {
            SendEmailAfterExecution.sendReportAfterExecution(passedCount, failedCount, skippedCount)
        }
    }

 */
}