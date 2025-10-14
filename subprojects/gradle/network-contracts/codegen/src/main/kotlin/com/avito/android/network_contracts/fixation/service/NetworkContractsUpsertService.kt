package com.avito.android.network_contracts.fixation.service

import com.avito.android.contracts.platform.internal.http.HttpClientService
import com.avito.android.contracts.platform.scheme.collect.ApiSchemesMetadata
import com.avito.android.contracts.platform.scheme.fixation.UpsertService
import com.avito.android.contracts.platform.shared.extractSchemesVersionFromBranch
import com.avito.android.network_contracts.fixation.service.data.UpdateApiSchemesService
import com.avito.android.network_contracts.fixation.service.data.UpdateApiSchemesServiceImpl
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

public abstract class NetworkContractsUpsertService :
    UpsertService<NetworkContractsUpsertService.Parameters> {

    private val updateApiSchemesService: UpdateApiSchemesService by lazy {
        UpdateApiSchemesServiceImpl(
            httpClient = parameters.httpClient.get().get().buildClient()
        )
    }

    override suspend fun sendContracts(schemes: List<ApiSchemesMetadata>) {
        updateApiSchemesService.sendContracts(
            author = parameters.author.get(),
            version = extractSchemesVersionFromBranch(parameters.branchName.get()),
            schemes = schemes
        )
    }

    internal interface Parameters : UpsertService.Parameters {

        val author: Property<String>
        val branchName: Property<String>
        val httpClient: Property<Provider<HttpClientService>>
    }
}
