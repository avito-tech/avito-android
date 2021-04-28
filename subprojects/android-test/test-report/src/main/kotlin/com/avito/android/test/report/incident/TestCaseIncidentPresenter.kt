package com.avito.android.test.report.incident

import com.avito.android.Result
import com.avito.android.test.report.StepException
import com.avito.report.model.IncidentElement

internal class TestCaseIncidentPresenter : IncidentPresenter {

    override fun canCustomize(exception: Throwable): Boolean = exception is StepException

    override fun customize(exception: Throwable): Result<List<IncidentElement>> {
        exception as StepException

        val title = StepException.title(exception.isPrecondition)

        val data = StepException.data(exception.isPrecondition, exception.action, exception.assertion)

        return Result.Success(
            listOf(
                IncidentElement(
                    message = title,
                    origin = "testCase",
                    data = data
                )
            )
        )
    }
}
