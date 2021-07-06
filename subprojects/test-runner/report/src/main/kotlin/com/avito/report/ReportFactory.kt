package com.avito.report

import java.io.Serializable

public interface ReportFactory : Serializable {

    public fun createReport(): Report
}
