package com.avito.android.network_contracts

import com.avito.android.contracts.platform.ContractsRootPlugin
import com.avito.android.contracts.platform.extension.ContractsRootExtension
import com.avito.android.contracts.platform.extension.defaultNetwork
import com.avito.android.contracts.platform.internal.http.HttpClientService
import com.avito.android.contracts.platform.scheme.fixation.UpsertService
import com.avito.android.network_contracts.fixation.service.NetworkContractsUpsertService
import com.avito.git.gitStateProvider
import com.avito.kotlin.dsl.getMandatoryStringProperty
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

public class NetworkContractsRootPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        if (!target.plugins.hasPlugin(ContractsRootPlugin::class.java)) {
            target.plugins.apply(ContractsRootPlugin::class.java)
        }

        val schemesContractExtension = target.extensions.getByType<ContractsRootExtension>()

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
                    HttpClientService.provideHttpClientService(target, NetworkContractsVariantConstants.NAME)
                }
            )
        }

        schemesContractExtension.defaultNetwork {
            serviceName.convention(NetworkContractsVariantConstants.SERVICE_NAME)
        }

        schemesContractExtension.networks.register(NetworkContractsVariantConstants.NAME)

        schemesContractExtension.fixations.register(NetworkContractsVariantConstants.NAME) {
            it.upsertService.set(networkContractsUpsertService)
        }
    }
}
