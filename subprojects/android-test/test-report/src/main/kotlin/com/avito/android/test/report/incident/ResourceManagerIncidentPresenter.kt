package com.avito.android.test.report.incident

import com.avito.android.Result
import com.avito.api.resourcemanager.ResourceManagerException
import com.avito.report.model.IncidentElement

internal class ResourceManagerIncidentPresenter : IncidentPresenter {

    override fun canCustomize(exception: Throwable): Boolean = exception is ResourceManagerException

    override fun customize(exception: Throwable): Result<List<IncidentElement>> {
        exception as ResourceManagerException

        val mainElement = IncidentElement(
            message = "Ошибка при обращении к ${exception.requestUrl}",
            origin = "ResourcesClient",
            data = exception.requestBody
        )

        return Result.Success(listOf(mainElement) + exception.incidentChain)
    }
}
