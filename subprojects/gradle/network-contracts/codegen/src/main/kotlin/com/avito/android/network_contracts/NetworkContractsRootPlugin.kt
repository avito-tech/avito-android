package com.avito.android.network_contracts

import com.avito.android.contracts.platform.ContractsRootPlugin
import com.avito.android.contracts.platform.extension.ContractsRootExtension
import com.avito.android.contracts.platform.extension.network
import com.avito.android.contracts.platform.internal.http.HttpClientService
import com.avito.android.contracts.platform.scheme.fixation.UpsertService
import com.avito.android.network_contracts.fixation.service.NetworkContractsUpsertService
import com.avito.git.gitStateProvider
import com.avito.kotlin.dsl.getMandatoryStringProperty
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType

public class NetworkContractsRootPlugin : Plugin<Project> {

    @Suppress("DEPRECATION")
    override fun apply(target: Project) {
        if (!target.plugins.hasPlugin(ContractsRootPlugin::class.java)) {
            target.plugins.apply(ContractsRootPlugin::class.java)
        }

        val schemesContractExtension = target.extensions.getByType<ContractsRootExtension>()
        val networkContractsExtension = target.extensions
            .create<NetworkContractsRootExtension>(NetworkContractsRootExtension.NAME)

        val networkContractsUpsertService = UpsertService.provideUpsertService(
            project = target,
            klass = NetworkContractsUpsertService::class.java
        ) {
            it.author.set(
                target.provider {
                    target.getMandatoryStringProperty("avito.networkContracts.fixation.author")
                }
            )
            it.branchName.set(target.gitStateProvider().map { it.currentBranch.name })
            it.httpClient.set(
                target.provider {
                    HttpClientService.provideHttpClientService(target)
                }
            )
        }

        schemesContractExtension.network {
            useTls.set(networkContractsExtension.network.useTls
                .convention(networkContractsExtension.useTls)
            )

            retries.set(
                networkContractsExtension.network.retries
                    .convention(networkContractsExtension.networkRetries)
            )
            timeouts.set(
                networkContractsExtension.network.timeouts
                    .convention(networkContractsExtension.networkTimeouts)
            )
            serviceUrl.set(
                networkContractsExtension.network.serviceUrl
                    .convention(networkContractsExtension.serviceUrl)
            )
            crtEnvName.set(
                networkContractsExtension.network.crtEnvName
                    .convention(networkContractsExtension.crtEnvName)
            )
            keyEnvName.set(
                networkContractsExtension.network.keyEnvName
                    .convention(networkContractsExtension.keyEnvName)
            )
        }

        schemesContractExtension.fixations.register(NetworkContractsVariantConstants.NAME) {
            it.upsertService.set(networkContractsUpsertService)
        }
    }
}
