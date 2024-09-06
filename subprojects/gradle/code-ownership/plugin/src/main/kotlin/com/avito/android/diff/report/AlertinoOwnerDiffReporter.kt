package com.avito.android.diff.report

import com.avito.alertino.AlertinoSender
import com.avito.alertino.model.AlertinoRecipient
import com.avito.android.diff.formatter.OwnersDiffMessageFormatter
import com.avito.android.diff.model.OwnersDiff

internal class AlertinoOwnerDiffReporter(
    private val alertinoSender: AlertinoSender,
    private val messageFormatter: OwnersDiffMessageFormatter
) : OwnersDiffReporter {

    override fun reportDiffFound(diffs: OwnersDiff) {
        val message = messageFormatter.formatDiffMessage(diffs)
        alertinoSender.sendNotification(
            template = NOTIFICATION_NAME,
            recipient = AlertinoRecipient(CHANNEL_ID),
            values = mapOf("text" to message)
        )
    }

    private companion object {
        const val NOTIFICATION_NAME = "reportCodeOwnershipDiffAndroid"
        const val CHANNEL_ID = "#mob-arch-alerts"
    }
}
