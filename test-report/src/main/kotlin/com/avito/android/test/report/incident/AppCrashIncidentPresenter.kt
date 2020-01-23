package com.avito.android.test.report.incident

import com.avito.report.model.IncidentElement

internal class AppCrashIncidentPresenter : IncidentPresenter {

    override fun canCustomize(exception: Throwable): Boolean = exception is AppCrashException

    override fun customize(exception: Throwable): IncidentPresenter.Result =
        IncidentPresenter.Result.ok(IncidentElement(message = "Crash приложения"))
}

class AppCrashException(cause: Throwable) : Exception(cause)
