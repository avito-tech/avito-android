package com.avito.android.signer.internal

import com.avito.android.signer.AbstractSignTask
import com.avito.android.signer.SignExtension
import com.avito.android.tls.TlsConfigurationPlugin
import com.avito.android.tls.TlsCredentialsService
import org.gradle.api.Project
import org.gradle.api.provider.Provider

internal class TaskConfigurator(
    private val extension: SignExtension,
    private val project: Project,
    private val urlResolver: UrlResolver = UrlResolver(),
    private val defaultTimeoutSec: Long = 40L
) {

    fun configure(task: AbstractSignTask, token: String) {
        task.group = "ci"

        task.serviceUrl.set(
            urlResolver.resolveServiceUrl(
                url = extension.serviceUrl,
                taskPath = task.path
            )
        )

        task.tokenProperty.set(token)

        task.readWriteTimeoutSec.set(
            extension.readWriteTimeoutSec.convention(defaultTimeoutSec)
        )

        task.useTls.set(
            extension.useTls
        )
        configureTls(task = task, project = project)
    }

    private fun configureTls(task: AbstractSignTask, project: Project) {
        val tlsCredentialsService: Provider<TlsCredentialsService> =
            TlsConfigurationPlugin.provideCredentialsService(project)

        task.tlsCredentialsService.set(tlsCredentialsService)
        task.usesService(tlsCredentialsService)
    }
}
