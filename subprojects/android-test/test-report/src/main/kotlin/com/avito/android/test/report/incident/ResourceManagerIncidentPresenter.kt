package com.avito.android.test.report.incident

import com.avito.api.resourcemanager.ResourceManagerException
import com.avito.report.model.IncidentElement
import com.github.salomonbrys.kotson.toJson

internal class ResourceManagerIncidentPresenter : IncidentPresenter {

    override fun canCustomize(exception: Throwable): Boolean = exception is ResourceManagerException

    override fun customize(exception: Throwable): IncidentPresenter.Result {
        exception as ResourceManagerException

        val mainElement = IncidentElement(
            message = "Ошибка при обращении к ${exception.requestUrl}",
            origin = "ResourcesClient",
            data = exception.requestBody?.toJson()
        )

        return IncidentPresenter.Result.OK(listOf(mainElement) + exception.incidentChain)
    }
}
