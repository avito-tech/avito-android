package com.avito.android.monitoring

import io.sentry.DefaultSentryClientFactory
import io.sentry.marshaller.json.JsonMarshaller

internal class CustomizableSentryClientFactory(
    private val jsonValueMaxLengthString: Int
) : DefaultSentryClientFactory() {

    override fun createJsonMarshaller(maxMessageLength: Int): JsonMarshaller {
        return CustomizableJsonMarshaller(
            maxMessageLength,
            jsonValueMaxLengthString
        )
    }
}
