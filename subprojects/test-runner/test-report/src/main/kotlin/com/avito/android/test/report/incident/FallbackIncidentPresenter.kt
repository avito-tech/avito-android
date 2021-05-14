package com.avito.android.test.report.incident

import com.avito.android.Result
import com.avito.report.model.IncidentElement
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonPrimitive

internal class FallbackIncidentPresenter : IncidentPresenter {

    override fun canCustomize(exception: Throwable): Boolean = true

    override fun customize(exception: Throwable): Result<List<IncidentElement>> {
        val data: JsonPrimitive? = if (exception is IncidentChainFactory.CustomizeFailException) {
            exception.failReason.message?.toJson()
        } else null

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
