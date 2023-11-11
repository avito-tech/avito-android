package com.avito.runner.listener

import com.avito.report.model.TestStaticData
import com.avito.runner.service.listener.TestListener

internal fun interface ReportArtifactsTestListenerProvider {
    fun provide(tests: List<TestStaticData>): TestListener
}
