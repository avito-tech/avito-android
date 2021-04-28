package com.avito.report.model

/**
 * Atomic error for Incident.chain
 *
 * @param message human-readable message for report
 * @param code http code for example
 * @param type determines how to display data (no info atm)
 */
public data class IncidentElement(
    val message: String,
    val code: Int? = null,
    val type: String? = null,
    val origin: String? = null,
    val className: String? = null,
    val data: String? = null
)
