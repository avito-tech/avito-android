package com.avito.api

public open class ResourceException(
    override val message: String,
    override val cause: Throwable? = null,
    public val requestUrl: String,
    public val requestBody: String?,
    public val errorBody: String?
) : Exception(message, cause)
