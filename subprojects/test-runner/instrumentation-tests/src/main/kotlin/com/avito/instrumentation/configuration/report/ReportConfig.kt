package com.avito.instrumentation.configuration.report

public sealed interface ReportConfig : java.io.Serializable {
    public object NoOp : ReportConfig {

        override fun equals(other: Any?): Boolean {
            return other as? NoOp != null
        }
    }

    public sealed interface ReportViewer : ReportConfig {
        public val reportApiUrl: String
        public val reportViewerUrl: String
        public val fileStorageUrl: String
        public val planSlug: String
        public val jobSlug: String

        public data class SendFromDevice(
            override val reportApiUrl: String,
            override val reportViewerUrl: String,
            override val fileStorageUrl: String,
            override val planSlug: String,
            override val jobSlug: String,
        ) : ReportViewer {
            init {
                validate(this)
            }
        }

        public data class SendFromRunner(
            override val reportApiUrl: String,
            override val reportViewerUrl: String,
            override val fileStorageUrl: String,
            override val planSlug: String,
            override val jobSlug: String,
        ) : ReportViewer {
            init {
                validate(this)
            }
        }

        private companion object {
            fun validate(report: ReportViewer) {
                with(report) {
                    require(reportApiUrl.isNotBlank())
                    require(reportViewerUrl.isNotBlank())
                    require(fileStorageUrl.isNotBlank())
                    require(planSlug.isNotBlank())
                    require(jobSlug.isNotBlank())
                }
            }
        }
    }
}
