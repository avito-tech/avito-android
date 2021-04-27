package com.avito.report.internal.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

/**
 * Atomic error for Incident.chain
 *
 * @param message human-readable message for report
 * @param code http code for example
 * @param type determines how to display data (no info atm)
 */
public data class IncidentElement(
    @SerializedName("message") val message: String,
    @SerializedName("code") val code: Int? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("origin") val origin: String? = null,
    @SerializedName("class") val className: String? = null,
    @SerializedName("data") val data: JsonElement? = null
)
