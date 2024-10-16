package com.avito.retrofit.adapter

/**
 * idk why we can't use [retrofit2.HttpException]
 */
public class HttpException(
    code: Int,
    body: String,
    responseMessage: String,
) : RuntimeException("HTTP Response[ code: $code, message: $responseMessage, body: $body]")
