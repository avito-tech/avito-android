package com.avito.instrumentation.runner.input_params

import com.avito.instrumentation.configuration.report.ReportConfig.NoOp
import com.avito.instrumentation.kotlinStubConfig
import com.avito.runner.config.RunnerReportConfig
import java.io.File

class NoOpReportCase(override val projectDir: File) : Case() {

    private inner class Then : Case.Then(
        "${projectDir.canonicalPath}/outputs/$configurationName"
    ) {

        override val expectedPluginInstrumentationParams = mapOf(
            "configuration" to configurationName,
            "override" to "overrideInConfiguration",
            "expectedCustomParam" to "value",
            "avito.report.transport" to "noop",
        )

        override val expectedReportConfig: RunnerReportConfig = RunnerReportConfig.None
    }

    override val reportConfig: NoOp = NoOp
    override val buildScript: String
        get() = kotlinStubConfig(reportConfig)

    override fun createThen(commit: String): Case.Then {
        return Then()
    }
}
