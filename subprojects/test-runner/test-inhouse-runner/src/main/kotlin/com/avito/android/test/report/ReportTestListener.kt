package com.avito.android.test.report

import com.avito.android.runner.InHouseInstrumentationTestRunner
import com.avito.logger.Logger
import com.avito.logger.create

class ReportTestListener : AbstractReportTestListener() {

    override val report: ReportTestLifecycle<*> by lazy {
        InHouseInstrumentationTestRunner.instance.report
    }

    override val logger: Logger by lazy {
        InHouseInstrumentationTestRunner.instance.loggerFactory.create<ReportTestListener>()
    }
}
