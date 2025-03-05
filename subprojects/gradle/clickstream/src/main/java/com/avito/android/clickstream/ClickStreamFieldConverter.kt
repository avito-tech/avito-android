package com.avito.android.clickstream

import java.text.NumberFormat
import java.util.Locale

public class ClickStreamFieldConverter {

    private val formatter: NumberFormat = NumberFormat.getNumberInstance(Locale.US)

    init {
        formatter.isGroupingUsed = false
        formatter.minimumFractionDigits = 0
        formatter.maximumFractionDigits = 6
    }

    public fun convertFields(fields: Map<String, Any>): MutableMap<String, String> {
        return mutableMapOf<String, String>().apply {
            fields.forEach { entry ->
                val (key, value) = entry
                val convertedValue = value.convertToString()
                if (convertedValue.isNotEmpty()) {
                    put(key, convertedValue)
                }
            }
        }
    }

    private fun Any.convertToString(): String {
        return when (this) {
            is Boolean -> (if (this) 1 else 0).toString()
            is List<*> -> this.joinToString(separator = ",") { elem ->
                when (elem) {
                    is Float -> formatter.format(elem.toDouble())
                    is Double -> formatter.format(elem)
                    else -> elem.toString()
                }
            }
            is Array<*> -> this.joinToString(separator = ",") { elem ->
                when (elem) {
                    is Float -> formatter.format(elem.toDouble())
                    is Double -> formatter.format(elem)
                    else -> elem.toString()
                }
            }
            else -> this.toString()
        }
    }
}
