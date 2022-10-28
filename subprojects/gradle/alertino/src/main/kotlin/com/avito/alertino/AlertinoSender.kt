package com.avito.alertino

import com.avito.alertino.model.AlertinoRecipient
import com.avito.alertino.model.CreatedMessage
import com.avito.android.Result

public interface AlertinoSender {

    public fun sendNotification(
        template: String,
        recipient: AlertinoRecipient,
        values: Map<String, String>
    ): Result<CreatedMessage>

    public fun sendNotificationToThread(
        template: String,
        previousMessage: CreatedMessage,
        values: Map<String, String>
    ): Result<CreatedMessage>
}
