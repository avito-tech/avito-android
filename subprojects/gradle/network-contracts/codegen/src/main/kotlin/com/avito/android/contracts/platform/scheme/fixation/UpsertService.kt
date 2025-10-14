package com.avito.android.contracts.platform.scheme.fixation

import com.avito.android.contracts.platform.scheme.collect.ApiSchemesMetadata
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

public interface UpsertService<T : UpsertService.Parameters> : BuildService<T> {

    public suspend fun sendContracts(schemes: List<ApiSchemesMetadata>)

    public interface Parameters : BuildServiceParameters

    public companion object {

        public fun <T : UpsertService<P>, P : Parameters> provideUpsertService(
            project: Project,
            klass: Class<T>,
            configuration: Action<P>,
        ): Provider<T> {
            return project.gradle.sharedServices.registerIfAbsent(
                klass.name,
                klass,
            ) {
                it.parameters(configuration)
            }
        }
    }
}
