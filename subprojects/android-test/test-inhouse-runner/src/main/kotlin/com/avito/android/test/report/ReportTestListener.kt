package com.avito.android.test.report

import com.avito.android.runner.InHouseInstrumentationTestRunner
import com.avito.logger.LoggerFactory

class ReportTestListener : AbstractReportTestListener() {

    override val loggerFactory: LoggerFactory by lazy { InHouseInstrumentationTestRunner.instance.loggerFactory }
}
