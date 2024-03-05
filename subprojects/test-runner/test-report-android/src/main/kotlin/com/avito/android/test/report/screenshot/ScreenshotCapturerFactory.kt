package com.avito.android.test.report.screenshot

import com.avito.android.instrumentation.ActivityProvider
import com.avito.report.TestArtifactsProvider

public object ScreenshotCapturerFactory {

    public fun create(
        testArtifactsProvider: TestArtifactsProvider,
        activityProvider: ActivityProvider
    ): ScreenshotCapturer {
        return ScreenshotCapturerImpl(
            testArtifactsProvider = testArtifactsProvider,
            activityProvider = activityProvider
        )
    }
}
