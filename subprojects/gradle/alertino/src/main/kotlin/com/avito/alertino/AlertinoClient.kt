package com.avito.alertino

import com.avito.alertino.internal.AlertinoApi
import com.avito.alertino.internal.model.Recipient
import com.avito.alertino.internal.model.SendNotificationBody
import com.avito.alertino.internal.model.SendNotificationResponse
import com.avito.alertino.internal.model.SendNotificationToThreadBody
import com.avito.alertino.model.AlertinoRecipient
import com.avito.alertino.model.CreatedMessage
import com.avito.android.Result
import com.avito.http.RetryInterceptor
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Base64

internal class AlertinoClient(
    baseUrl: String,
    loggerFactory: LoggerFactory,
) : AlertinoSender {

    private val logger = loggerFactory.create<AlertinoClient>()

    private val okHttpClient = OkHttpClient.Builder().addInterceptor(
        RetryInterceptor(
            retries = 3, allowedMethods = listOf("POST")
        )
    ).addInterceptor(
        HttpLoggingInterceptor { message ->
            logger.info(message)
        }.apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
    ).build()

    private val retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .validateEagerly(true).build()

    private val alertinoApi: AlertinoApi = retrofit.create(AlertinoApi::class.java)

    override fun sendNotification(
        template: String,
        recipient: AlertinoRecipient,
        values: Map<String, String>
    ): Result<CreatedMessage> {

        val body = SendNotificationBody(
            template = template,
            recipients = listOf(Recipient(addresses = listOf(recipient).map { it.name })),
            subscribeRecipients = false,
            labels = emptyList(),
            values = convertValuesToBase64EncodedString(values)
        )

        val response = alertinoApi.sendNotification(body).execute()
        return handleResponse(recipient, response)
    }

    override fun sendNotificationToThread(
        template: String,
        previousMessage: CreatedMessage,
        values: Map<String, String>
    ): Result<CreatedMessage> {

        val body = SendNotificationToThreadBody(
            template = template,
            recipientToThreadIdMap = mapOf(previousMessage.recipient.name to previousMessage.threadId),
            values = convertValuesToBase64EncodedString(values)
        )

        val response = alertinoApi.sendNotificationToThread(body).execute()
        return handleResponse(previousMessage.recipient, response)
    }

    private fun handleResponse(
        recipient: AlertinoRecipient,
        response: Response<SendNotificationResponse>
    ): Result<CreatedMessage> {

        if (!response.isSuccessful) {
            return Result.Failure(
                RuntimeException(
                    "Sending message to Alertino failed: ${response.code()} ${response.errorBody()?.string()}"
                )
            )
        }

        val sendNotificationResponse: SendNotificationResponse? = response.body()
        if (sendNotificationResponse == null || sendNotificationResponse.result.createdMessages.isEmpty()) {
            return Result.Failure(
                IllegalStateException("Sending message to Alertino failed. Response: $sendNotificationResponse")
            )
        }

        val threadId = sendNotificationResponse.result.createdMessages.entries.first().value
        return Result.Success(CreatedMessage(recipient, threadId))
    }

    private fun convertValuesToBase64EncodedString(values: Map<String, String>): String {
        val jsonObject = JsonObject().apply {
            values.forEach { (key, value) ->
                addProperty(key, value)
            }
        }
        return Base64.getEncoder().encodeToString(jsonObject.toString().toByteArray())
    }
}
