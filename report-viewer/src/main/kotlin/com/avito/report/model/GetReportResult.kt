package com.avito.report.model

sealed class GetReportResult {
    data class Found(val report: Report) : GetReportResult()
    object NotFound : GetReportResult()
    data class Error(val exception: Throwable) : GetReportResult()
}
