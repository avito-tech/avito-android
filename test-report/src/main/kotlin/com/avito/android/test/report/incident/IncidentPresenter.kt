package com.avito.android.test.report.incident

import com.avito.report.model.IncidentElement

/**
 * Customize incident view in Report Viewer based on exception type and data
 * @see [IncidentElement] for format details
 */
internal interface IncidentPresenter {

    sealed class Result {
        data class OK(val chain: List<IncidentElement>) : Result()
        data class Fail(val exception: Exception) : Result()

        companion object {
            internal fun ok(incidentElement: IncidentElement): OK = OK(listOf(incidentElement))
        }
    }

    fun canCustomize(exception: Throwable): Boolean

    fun customize(exception: Throwable): Result
}
