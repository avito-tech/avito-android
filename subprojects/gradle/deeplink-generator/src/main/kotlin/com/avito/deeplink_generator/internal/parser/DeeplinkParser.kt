package com.avito.deeplink_generator.internal.parser

import com.avito.deeplink_generator.model.Deeplink

internal object DeeplinkParser {

    private const val SCHEMA_DELIMITER = "://"
    private const val HOST_DELIMITER = "/"

    fun parse(link: String, defaultSchema: String): Deeplink {
        val schema = if (defaultSchema.isEmpty() || !link.startsWith(defaultSchema)) {
            link.substringBefore(SCHEMA_DELIMITER, missingDelimiterValue = defaultSchema)
        } else {
            defaultSchema
        }
        require(schema.isNotEmpty()) {
            "Schema must be supplied to $link! " +
                "Please, either define a defaultScheme in deeplinkGenerator extension, or add it manually to a link"
        }

        val deeplinkWithoutSchema = link.substringAfter(SCHEMA_DELIMITER)
        val host = deeplinkWithoutSchema.substringBefore(HOST_DELIMITER)
        val path = deeplinkWithoutSchema.substringAfter(host)
        return Deeplink(schema, host, path)
    }
}
