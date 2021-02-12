package com.avito.instrumentation.report

import com.avito.report.model.ReportCoordinates
import java.io.Serializable

public interface ReportFactory : Serializable {

    public sealed class Config : Serializable {

        public data class ReportViewerCoordinates(
            val reportCoordinates: ReportCoordinates,
            val buildId: String
        ) : Config()

        public data class ReportViewerId(
            val reportId: String
        ) : Config()

        public data class InMemory(
            val id: String
        ) : Config()
    }

    public fun createReport(config: Config): Report

    public fun createReadReport(config: Config): ReadReport

}
