package com.avito.android.runner.report

import com.avito.report.model.AndroidTest

public interface Report : ReadReport, LegacyReport {

    public fun addTest(test: AndroidTest)

    public companion object
}
