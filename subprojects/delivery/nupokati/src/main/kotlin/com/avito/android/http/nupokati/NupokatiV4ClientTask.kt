package com.avito.android.http.nupokati

import com.avito.android.stats.StatsDConfig
import com.avito.android.stats.StatsDSender
import com.avito.android.tls.TlsCredentialsService
import com.avito.logger.GradleLoggerPlugin
import com.avito.time.DefaultTimeProvider
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

internal abstract class NupokatiV4ClientTask : DefaultTask() {

    @get:Input
    abstract val baseUrl: Property<String>

    @get:Input
    abstract val connectionTimeoutSeconds: Property<Long>

    @get:Input
    abstract val readTimeoutSeconds: Property<Long>

    @get:Input
    abstract val writeTimeoutSeconds: Property<Long>

    @get:Input
    abstract val chunkedUploadThresholdBytes: Property<Long>

    @get:Input
    abstract val useTls: Property<Boolean>

    @get:Internal
    abstract val tlsCredentialsService: Property<TlsCredentialsService>

    @get:Internal
    abstract val statsDConfig: Property<StatsDConfig>

    protected fun buildNupokatiClient(): NupokatiV4Client {
        val loggerFactory = GradleLoggerPlugin.provideLoggerFactory(this).get()
        val tlsCredentials = if (useTls.get()) {
            tlsCredentialsService.get().createCredentials()
        } else {
            null
        }
        val statsDSender = StatsDSender.create(statsDConfig.get(), loggerFactory)

        return NupokatiV4ClientFactory.create(
            baseUrl = baseUrl.get(),
            connectionTimeoutSeconds = connectionTimeoutSeconds.get(),
            readTimeoutSeconds = readTimeoutSeconds.get(),
            writeTimeoutSeconds = writeTimeoutSeconds.get(),
            chunkedUploadThresholdBytes = chunkedUploadThresholdBytes.get(),
            tlsCredentials = tlsCredentials,
            statsDSender = statsDSender,
            timeProvider = DefaultTimeProvider(),
            loggerFactory = loggerFactory,
        )
    }
}
