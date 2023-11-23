package com.avito.android.tls.internal.provider

import com.avito.android.tls.credentials.TlsCredentials

internal sealed interface TlsCredentialsProvider {

    fun provideCredentials(): TlsCredentials
}
