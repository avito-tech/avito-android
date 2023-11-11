package com.avito.runner.config

import com.avito.reportviewer.model.ReportCoordinates
import java.io.Serializable

public sealed interface RunnerReportConfig : Serializable {

    public object None : RunnerReportConfig {
        override fun equals(other: Any?): Boolean {
            return other as? None != null
        }
    }

    public data class ReportViewer(
        public val reportApiUrl: String,
        public val reportViewerUrl: String,
        public val fileStorageUrl: String,
        public val coordinates: ReportCoordinates,
    ) : RunnerReportConfig {

        init {
            validate(this)
        }

        private companion object {
            fun validate(report: ReportViewer) {
                with(report) {
                    require(reportApiUrl.isNotEmpty())
                    require(reportViewerUrl.isNotEmpty())
                    require(fileStorageUrl.isNotEmpty())
                }
            }
        }
    }
}
