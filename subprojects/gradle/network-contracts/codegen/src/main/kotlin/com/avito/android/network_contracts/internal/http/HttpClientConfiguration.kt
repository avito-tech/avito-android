package com.avito.android.network_contracts.internal.http

import com.avito.android.tls.manager.TlsManager
import com.avito.logger.LoggerFactory
import org.gradle.api.provider.Property

internal interface HttpClientConfiguration {
    val serviceUrl: Property<String>
    val tlsManager: Property<TlsManager>
    val loggerFactory: Property<LoggerFactory>
}
