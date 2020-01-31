package com.avito.android.test.report.incident

import com.avito.android.test.report.StepException
import com.avito.report.model.IncidentElement
import com.github.salomonbrys.kotson.toJson

internal class TestCaseIncidentPresenter : IncidentPresenter {

    override fun canCustomize(exception: Throwable): Boolean = exception is StepException

    override fun customize(exception: Throwable): IncidentPresenter.Result {
        exception as StepException

        val title = StepException.title(exception.isPrecondition)

        val data = StepException.data(exception.isPrecondition, exception.action, exception.assertion)

        return IncidentPresenter.Result.ok(
            IncidentElement(
                message = title,
                origin = "testCase",
                data = data.toJson()
            )
        )
    }
}
