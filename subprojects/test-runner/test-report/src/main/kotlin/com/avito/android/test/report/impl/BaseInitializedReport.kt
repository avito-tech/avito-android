package com.avito.android.test.report.impl

import com.avito.android.Result
import com.avito.android.test.report.ReportState
import com.avito.android.test.report.model.StepAttachments
import com.avito.android.test.report.screenshot.ScreenshotCapturer
import com.avito.android.test.report.transport.Transport
import com.avito.filestorage.FutureValue
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.model.Entry
import com.avito.time.TimeProvider
import java.io.File

internal abstract class BaseInitializedReport(
    loggerFactory: LoggerFactory,
    protected val transport: Transport,
    private val screenshotCapturer: ScreenshotCapturer,
    protected val timeProvider: TimeProvider
) : BaseInternalReport() {

    private val logger = loggerFactory.create<BaseInitializedReport>()

    abstract val state: ReportState.NotFinished.Initialized
    abstract val currentAttachments: StepAttachments

    override fun addHtml(label: String, content: String, wrapHtml: Boolean) {
        val wrappedContentIfNeeded = if (wrapHtml) wrapInHtml(content) else content

        val html = transport.sendContent(
            test = state.testMetadata,
            content = wrappedContentIfNeeded,
            type = Entry.File.Type.html,
            comment = label
        )

        currentAttachments.uploads.add(html)
    }

    private fun wrapInHtml(content: String): String {
        return """<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8" />
</head>
<body>
<pre>
$content
</pre>
</body>
</html>""".trimIndent()
    }

    override fun addText(label: String, text: String) {
        val txt = transport.sendContent(
            test = state.testMetadata,
            content = text,
            type = Entry.File.Type.plain_text,
            comment = label
        )
        currentAttachments.uploads.add(txt)
    }

    override fun addComment(comment: String) {
        currentAttachments.entries.add(
            Entry.Comment(comment, timeProvider.nowInSeconds())
        )
    }

    override fun addScreenshot(label: String) {
        makeScreenshot(label).fold(
            { futureValue ->
                if (futureValue != null) {
                    currentAttachments.uploads.add(futureValue)
                }
            },
            { throwable ->
                logger.warn("Failed to addScreenshot", throwable)
            }
        )
    }

    protected fun makeScreenshot(comment: String): Result<FutureValue<Entry.File>?> {
        return screenshotCapturer.captureAsFile().map { screenshot: File ->

            transport.sendContent(
                test = state.testMetadata,
                file = screenshot,
                type = Entry.File.Type.img_png,
                comment = comment
            )
        }
    }

    override fun addAssertion(label: String) {
        currentAttachments.entries.add(
            Entry.Check(label, timeProvider.nowInSeconds())
        )
    }
}
