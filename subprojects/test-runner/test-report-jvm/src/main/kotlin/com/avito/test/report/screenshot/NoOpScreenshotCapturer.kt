package com.avito.test.report.screenshot

import com.avito.android.Result
import com.avito.android.test.report.screenshot.ScreenshotCapturer
import java.io.File

public object NoOpScreenshotCapturer : ScreenshotCapturer {
    override fun captureAsFile(filename: String): Result<File> =
        Result.Failure(IllegalStateException("Screenshots are not available for JVM tests"))
}
