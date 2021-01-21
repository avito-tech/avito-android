package com.avito.android.elastic

import java.util.Locale

internal class ElasticDateFormatChecker : DateFormatChecker {

    private val datePattern = Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}")

    private val dateCheckCache = mutableSetOf<String>()

    override fun check(formattedDate: String) {
        if (!dateCheckCache.contains(formattedDate)) {
            require(datePattern.matches(formattedDate)) {
                "Incorrect elastic date format: should be yyyy-MM-dd, got: $formattedDate; " +
                    "Possible problem with locale, default is: ${Locale.getDefault()}"
            }

            dateCheckCache.add(formattedDate)
        }
    }
}
