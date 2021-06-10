package com.avito.runner.scheduler.logcat

import com.avito.android.Problem

internal object StubLogcatAccessor : LogcatAccessor {

    override fun getLogs(): LogcatResult = LogcatResult.Unavailable(
        Problem(
            shortDescription = "stub description",
            context = "StubLogcatAccessor"
        )
    )
}
