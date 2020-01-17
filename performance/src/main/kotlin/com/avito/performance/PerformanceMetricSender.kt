package com.avito.performance

import com.avito.android.stats.CountMetric
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.TimeMetric
import com.avito.bitbucket.Bitbucket
import com.avito.performance.stats.comparison.ComparedTest
import com.github.seratch.jslack.Slack
import com.github.seratch.jslack.api.model.Attachment
import com.github.seratch.jslack.api.webhook.Payload

internal interface PerformanceMetricSender {

    fun trackComparison(key: String, value: Long)

    fun trackSample(key: String, value: Long)

    fun trackSuccess(key: String, test: ComparedTest.Comparison)

    fun reportToPr()

    class Impl(
        private val sender: StatsDSender,
        private val graphiteKey: String = "undefined",
        private val buildUrl: String? = null,
        private val bitbucket: Bitbucket,
        private val slackConfig: SlackConfig
    ) : PerformanceMetricSender {

        private val prefix = "ci.performance.fps-metrics"

        override fun trackComparison(key: String, value: Long) {
            sender.send(
                "$prefix.comparison",
                TimeMetric(key, value)
            )
        }

        override fun trackSuccess(key: String, test: ComparedTest.Comparison) {
            with(test.failed()) {
                if (this@with.isEmpty()) {
                    sender.send(
                        "$prefix.result.success",
                        CountMetric(key)
                    )
                } else {
                    sender.send(
                        "$prefix.result.failure",
                        CountMetric(key)
                    )
                    sendToSlack(
                        mutableListOf("*$key* has failed. \n").apply { addAll(buildMessage(this@with)) },
                        ATTACHMENT_COLOR_RED
                    )
                }
            }

            test.performedMuchBetterThanUsual()
                .takeIf { it.isNotEmpty() }
                ?.let {
                    sendToSlack(
                        mutableListOf("*$key* has performed outstanding results. \n").apply { addAll(buildMessage(it)) },
                        ATTACHMENT_COLOR_GREEN
                    )
                }
        }

        override fun trackSample(key: String, value: Long) {
            sender.send(
                "$prefix.$graphiteKey",
                TimeMetric(key, value)
            )
        }

        override fun reportToPr() {
            bitbucket.createCommentWithTask(
                comment = "Возможно твои изменения просадили перформанс. См. вкладку Avito Performance Report в TC",
                task = "Разобраться, если есть сомнения, написать @mayudin"
            )
        }

        private fun buildMessage(series: Map<String, ComparedTest.Series>): List<String> {
            return series.map {
                "Series: *" + it.key + "* " + ":\n" +
                    "*significance*:\t${it.value.significance}\n" +
                    "*p-value*:\t${it.value.pValue}\n" +
                    "*currentSampleIs*:\t${it.value.currentSampleIs}\n" +
                    "*statistic*:\t${it.value.statistic}\n" +
                    "*threshold*:\t${it.value.threshold}\n" +
                    "*meanDiff(current-previous)*:\t${it.value.meanDiff}\n"
            }
        }

        private fun sendToSlack(messages: List<String>, color: String) {
            @Suppress("DEPRECATION")
            Slack.getInstance().send(
                slackConfig.hookUrl,
                Payload.builder()
                    .channel(slackConfig.channel)
                    .text("Build: $buildUrl")
                    .apply {
                        attachments(
                            messages.map {
                                Attachment.builder()
                                    .color(color)
                                    .text(it)
                                    .build()
                            }.toList()

                        )
                    }
                    .username(slackConfig.username)
                    .iconEmoji(slackConfig.avatar)
                    .build()
            )
            sender.send(
                "$prefix.pr-notification-sent",
                CountMetric("failure")
            )
        }
    }
}

private const val ATTACHMENT_COLOR_RED = "A30200"
private const val ATTACHMENT_COLOR_GREEN = "00a230"
