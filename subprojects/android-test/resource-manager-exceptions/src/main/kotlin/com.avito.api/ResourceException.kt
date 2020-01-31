package com.avito.api

open class ResourceException(
    override val message: String,
    override val cause: Throwable? = null,
    val requestUrl: String,
    val requestBody: String?,
    val errorBody: String?
) : Exception(message, cause)
