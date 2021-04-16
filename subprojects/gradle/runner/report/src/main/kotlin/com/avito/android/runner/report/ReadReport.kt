package com.avito.android.runner.report

import com.avito.android.Result
import com.avito.report.model.SimpleRunTest

public interface ReadReport {

    public fun getTests(): Result<List<SimpleRunTest>>
}
