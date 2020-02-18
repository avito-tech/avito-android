package com.avito.android.monitoring

import android.annotation.SuppressLint
import android.util.Log
import com.avito.android.runner.TestRunEnvironment
import io.sentry.SentryClient
import io.sentry.event.Event
import io.sentry.event.EventBuilder
import io.sentry.event.interfaces.ExceptionInterface
import okhttp3.Response

class CompositeTestIssuesMonitor(
    private val sentry: SentryClient,
    private val testRunEnvironment: TestRunEnvironment.RunEnvironment,
    private val logTag: String,
    private val responseParser: (EventBuilder, RequestResponseData) -> EventBuilder = { event, _ -> event }
) : TestIssuesMonitor {

    override fun onFailure(throwable: Throwable) {
        sentry.sendEvent(createExceptionEvent(throwable).withLevel(Event.Level.ERROR))
    }

    override fun onWarning(throwable: Throwable) {
        sentry.sendEvent(createExceptionEvent(throwable).withLevel(Event.Level.WARNING))
    }

    @SuppressLint("LogNotTimber")
    override fun onWarning(response: Response) {
        val requestResponseData = ResponseDataExtractor.extract(response)

        val event = createHttpEvent(requestResponseData).withLevel(Event.Level.WARNING)

        sentry.sendEvent(
            try {
                responseParser.invoke(event, requestResponseData)
            } catch (e: Throwable) {
                Log.e(logTag, "Can't send sentry event", e)
                event
            }
        )
    }

    private fun createHttpEvent(requestResponseData: RequestResponseData): EventBuilder {

        val event = createEvent()
            .withMessage("${requestResponseData.host} ${requestResponseData.responseCode}")
            .withExtra("requestUrl", requestResponseData.requestUrl)
            .withExtra("responseCode", requestResponseData.responseCode)

        if (requestResponseData.requestBody != null) {
            event.withExtra("requestBody", requestResponseData.requestBody)
        }

        if (requestResponseData.responseBody != null) {
            event.withExtra("responseBody", requestResponseData.responseBody)
        }

        return event
    }

    private fun createExceptionEvent(throwable: Throwable): EventBuilder {
        return createEvent()
            .withMessage(throwable.message)
            .withSentryInterface(ExceptionInterface(throwable))
    }

    private fun createEvent(): EventBuilder {
        return EventBuilder()
            .withTag(
                "testName",
                "${testRunEnvironment.testMetadata.className}.${testRunEnvironment.testMetadata.methodName}"
            )
            .withTag("buildId", testRunEnvironment.teamcityBuildId.toString())
            .withTag("buildBranch", testRunEnvironment.buildBranch)
            .withTag("deviceId", testRunEnvironment.deviceId)
            .withTag("deviceName", testRunEnvironment.deviceName)
            .withTag("runId", testRunEnvironment.testRunCoordinates.runId)
    }
}
