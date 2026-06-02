package com.avito.android.network_contracts.fixation.service

import com.avito.android.contracts.platform.internal.http.HttpClientService
import com.avito.android.contracts.platform.scheme.collect.ApiSchemesMetadata
import com.avito.android.contracts.platform.scheme.fixation.UpsertService
import com.avito.android.contracts.platform.shared.extractSchemesVersionFromBranch
import com.avito.android.network_contracts.fixation.service.data.UpdateApiSchemesService
import com.avito.android.network_contracts.fixation.service.data.UpdateApiSchemesServiceImpl
import com.avito.git.GitInfoBuildService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.ServiceReference

public abstract class NetworkContractsUpsertService :
    UpsertService<NetworkContractsUpsertService.Parameters> {

    private val updateApiSchemesService: UpdateApiSchemesService by lazy {
        UpdateApiSchemesServiceImpl(
            httpClient = parameters.httpClient.get().get().buildClient()
        )
    }

    override suspend fun sendContracts(schemes: List<ApiSchemesMetadata>) {
        val branchName = parameters.branchName.orNull
            ?: parameters.gitInfoService.get().getGitState().currentBranch.name
        updateApiSchemesService.sendContracts(
            author = parameters.author.get(),
            version = extractSchemesVersionFromBranch(branchName),
            schemes = schemes
        )
    }

    internal interface Parameters : UpsertService.Parameters {

        val author: Property<String>
        val branchName: Property<String>
        val httpClient: Property<Provider<HttpClientService>>

        @get:ServiceReference(GitInfoBuildService.NAME)
        val gitInfoService: Property<GitInfoBuildService>
    }
}
