package com.avito.android.test.report.incident

import com.avito.android.Result
import com.avito.report.model.IncidentElement

internal class FallbackIncidentPresenter : IncidentPresenter {

    override fun canCustomize(exception: Throwable): Boolean = true

    override fun customize(exception: Throwable): Result<List<IncidentElement>> {
        val data = if (exception is IncidentChainFactory.CustomizeFailException) {
            exception.failReason.message
        } else {
            null
        }

        return Result.Success(
            listOf(
                IncidentElement(
                    message = exception.message ?: "Exception has no message",
                    className = exception::class.java.simpleName,
                    data = data
                )
            )
        )
    }
}
