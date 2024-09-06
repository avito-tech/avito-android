package com.avito.android.network

import com.avito.alertino.AlertinoSender
import com.avito.alertino.model.AlertinoRecipient
import com.avito.alertino.model.CreatedMessage
import com.avito.android.Result

class FakeAlertinoSender : AlertinoSender {

    override fun sendNotification(
        template: String,
        recipient: AlertinoRecipient,
        values: Map<String, String>
    ): Result<CreatedMessage> {
        return Result.Success(
            CreatedMessage(
                recipient = AlertinoRecipient(name = "name 1"),
                threadId = "thread 1",
            )
        )
    }

    override fun sendNotificationToThread(
        template: String,
        previousMessage: CreatedMessage,
        values: Map<String, String>
    ): Result<CreatedMessage> {
        return Result.Success(
            CreatedMessage(
                recipient = AlertinoRecipient(name = "name 2"),
                threadId = "thread 2",
            )
        )
    }
}
