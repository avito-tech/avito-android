package com.avito.report.model

import com.google.gson.JsonElement

/**
 * Atomic error for Incident.chain
 *
 * @param message human-readable message for report
 * @param code http code for example
 * @param type determines how to display data (no info atm)
 *
 * todo extract to public api module (used in avito build scripts)
 */
public data class IncidentElement(
    val message: String,
    val code: Int? = null,
    val type: String? = null,
    val origin: String? = null,
    val className: String? = null,
    val data: JsonElement? = null
)
