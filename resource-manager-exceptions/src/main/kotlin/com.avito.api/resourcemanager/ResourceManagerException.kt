package com.avito.api.resourcemanager

import com.avito.api.ResourceException
import com.avito.report.model.IncidentElement

class ResourceManagerException(
    override val message: String,
    cause: Throwable?,
    requestUrl: String,
    requestBody: String?,
    val responseBody: String?,
    val incidentChain: List<IncidentElement>
) : ResourceException(message, cause, requestUrl, requestBody, responseBody)
