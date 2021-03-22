package com.avito.android.test.report.incident

import com.avito.android.Result
import com.avito.api.ResourceException
import com.avito.report.model.IncidentElement
import com.github.salomonbrys.kotson.toJson

internal class ResourceIncidentPresenter : IncidentPresenter {

    override fun canCustomize(exception: Throwable): Boolean = exception.javaClass == ResourceException::class.java

    override fun customize(exception: Throwable): Result<List<IncidentElement>> {
        exception as ResourceException

        val mainElement = IncidentElement(
            message = "Ошибка при обращении к ${exception.requestUrl}",
            origin = "ResourcesClient",
            data = exception.requestBody?.toJson()
        )

        val responseElement = IncidentElement(
            message = "Ответ от ${exception.requestUrl}",
            origin = "ResourcesClient",
            data = "${exception.errorBody}".toJson()
        )

        return Result.Success(listOf(mainElement, responseElement))
    }
}
