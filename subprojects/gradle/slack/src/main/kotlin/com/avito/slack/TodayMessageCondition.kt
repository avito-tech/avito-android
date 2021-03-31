package com.avito.slack

import com.avito.slack.model.FoundMessage
import com.avito.time.TimeProvider
import com.avito.time.isSameDay
import java.util.Date

class TodayMessageCondition(private val timeProvider: TimeProvider) : SlackMessagePredicate {

    override fun matches(existingMessage: FoundMessage): Boolean {
        return slackTimeToDate(existingMessage.timestamp).isSameDay(timeProvider.now())
    }

    private fun slackTimeToDate(slackTimeStamp: String): Date {
        val slackTime = slackTimeStamp.substringBefore('.').toLong()
        return timeProvider.toDate(slackTime)
    }
}
