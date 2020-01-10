package com.avito.slack

import com.avito.slack.model.FoundMessage
import com.avito.time.TimeProvider
import java.util.Date

class TodayMessageCondition(private val timeProvider: TimeProvider) : SlackMessageUpdateCondition {

    override fun updateIf(existingMessage: FoundMessage): Boolean {
        return timeProvider.isSameDay(slackTimeToDate(existingMessage.timestamp), timeProvider.now())
    }

    private fun slackTimeToDate(slackTimeStamp: String): Date {
        val slackTime = slackTimeStamp.substringBefore('.').toLong()
        return timeProvider.toDate(slackTime)
    }
}
