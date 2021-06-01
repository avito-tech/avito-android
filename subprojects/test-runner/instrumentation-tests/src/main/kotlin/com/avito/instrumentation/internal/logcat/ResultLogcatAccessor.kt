package com.avito.instrumentation.internal.logcat

import com.avito.android.Problem
import com.avito.android.Result

internal class ResultLogcatAccessor(val result: Result<String>) : LogcatAccessor {

    override fun getLogs(): LogcatResult {
        return result.fold(
            onSuccess = { LogcatResult.Success(it) },
            onFailure = { throwable ->
                LogcatResult.Unavailable(
                    Problem(
                        shortDescription = "Error fetching logcat from device",
                        context = "ResultLogcatAccessor: getting logs for test report",
                        because = "adb logcat failed after all retries, " +
                            "take a look on provided throwable for details",
                        throwable = throwable
                    )
                )
            }
        )
    }
}
