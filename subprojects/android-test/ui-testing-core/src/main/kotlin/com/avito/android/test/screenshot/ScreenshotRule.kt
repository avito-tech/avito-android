package com.avito.android.test.screenshot

import org.junit.rules.TestWatcher
import org.junit.runner.Description

class ScreenshotRule(
    private val provider: ScreenshotProvider,
    private val consumer: ScreenshotConsumer
) : TestWatcher() {

    override fun finished(description: Description) {
        super.finished(description)
        if (description.hasAnnotation(SuppressScreenshot::class.java)) {
            return
        }

        takeScreenshot(description = "After Test")
    }

    fun takeScreenshot(description: String) {
        val screenShot = provider.getScreenshot()
        consumer.onScreenshotIsReady(screenShot, description)
    }
}

private fun <T : Annotation> Description.hasAnnotation(clazz: Class<T>): Boolean {
    return this.getAnnotation(clazz) != null
}
