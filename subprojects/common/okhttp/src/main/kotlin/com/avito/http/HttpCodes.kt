package com.avito.http

public object HttpCodes {
    public const val OK: Int = 200
    public const val BAD_REQUEST: Int = 400
    public const val NOT_FOUND: Int = 404
    public const val CLIENT_TIMEOUT: Int = 408
    public const val INTERNAL_ERROR: Int = 500
    public const val BAD_GATEWAY: Int = 502
    public const val UNAVAILABLE: Int = 503
    public const val GATEWAY_TIMEOUT: Int = 504
}
