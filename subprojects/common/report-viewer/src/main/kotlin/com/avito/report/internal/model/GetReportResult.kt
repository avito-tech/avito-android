package com.avito.report.internal.model

import com.avito.report.model.Report

internal sealed class GetReportResult {

    data class Found(val report: Report) : GetReportResult()

    data class NotFound(val exception: Throwable) : GetReportResult()

    data class Error(val exception: Throwable) : GetReportResult()
}
