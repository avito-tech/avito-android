package com.avito.android.test.report.incident

import com.avito.android.Result
import com.avito.report.model.IncidentElement

/**
 * Customize incident view in Report Viewer based on exception type and data
 * @see [IncidentElement] for format details
 */
internal interface IncidentPresenter {

    fun canCustomize(exception: Throwable): Boolean

    fun customize(exception: Throwable): Result<List<IncidentElement>>
}
