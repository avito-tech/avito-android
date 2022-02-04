package com.avito.android.build_checks.internal.kotlin_daemon

import org.gradle.internal.logging.events.LogEvent
import org.gradle.internal.logging.events.OutputEvent
import org.gradle.internal.logging.events.OutputEventListener
import java.util.concurrent.atomic.AtomicInteger

internal class FailureEventListener(
    private val fallbacksCounter: AtomicInteger,
) : OutputEventListener {

    override fun onOutput(event: OutputEvent) {
        if (isFallbackMessage(event)) {
            // Can't fail a build from OutputEventListener. So, only mark it
            fallbacksCounter.incrementAndGet()
        }
    }

    private fun isFallbackMessage(event: OutputEvent): Boolean {
        return event is LogEvent
            && event.message.contains("Could not connect to kotlin daemon. Using fallback strategy.")
    }
}
