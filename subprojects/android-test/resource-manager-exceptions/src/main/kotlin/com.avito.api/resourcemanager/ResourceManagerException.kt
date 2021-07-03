package com.avito.api.resourcemanager

import com.avito.api.ResourceException
import com.avito.report.model.IncidentElement

public class ResourceManagerException(
    override val message: String,
    cause: Throwable?,
    requestUrl: String,
    requestBody: String?,
    public val responseBody: String?,
    public val incidentChain: List<IncidentElement>
) : ResourceException(message, cause, requestUrl, requestBody, responseBody)
