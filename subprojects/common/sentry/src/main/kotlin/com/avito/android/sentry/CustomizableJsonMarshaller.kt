package com.avito.android.sentry

import com.fasterxml.jackson.core.JsonGenerator
import io.sentry.marshaller.json.JsonMarshaller
import io.sentry.marshaller.json.SentryJsonGenerator
import java.io.OutputStream

internal class CustomizableJsonMarshaller(
    maxMessageLength: Int,
    private val jsonValueMaxLengthString: Int
) : JsonMarshaller(maxMessageLength) {

    override fun createJsonGenerator(destination: OutputStream?): JsonGenerator {
        return (super.createJsonGenerator(destination) as SentryJsonGenerator).apply {
            setMaxLengthString(jsonValueMaxLengthString)
        }
    }
}
