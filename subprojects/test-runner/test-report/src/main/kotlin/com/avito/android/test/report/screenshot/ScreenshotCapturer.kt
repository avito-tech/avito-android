package com.avito.android.test.report.screenshot

import com.avito.android.Result
import java.io.File
import java.util.UUID

public interface ScreenshotCapturer {

    public fun captureAsFile(
        filename: String = "${UUID.randomUUID()}.png"
    ): Result<File>
}
