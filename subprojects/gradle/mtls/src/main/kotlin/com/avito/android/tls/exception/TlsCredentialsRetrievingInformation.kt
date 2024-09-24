package com.avito.android.tls.exception

internal data class TlsCredentialsRetrievingInformation(
    val action: String,
    val problem: String,
    val solution: String? = null,
)
