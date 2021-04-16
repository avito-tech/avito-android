package com.avito.report.internal.model

import com.avito.report.model.Report

internal sealed class GetReportResult {
    data class Found(val report: Report) : GetReportResult()
    object NotFound : GetReportResult()
    data class Error(val exception: Throwable) : GetReportResult()
}
