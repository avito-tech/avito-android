package com.avito.android.module_graph.extractor

import kotlinx.serialization.json.Json
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

public abstract class ModuleGraphInfoExtractorService : BuildService<BuildServiceParameters.None> {

    public val defaultJson: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }
    }

    public val linesOfCodeCounter: ModuleLinesOfCodeCounter by lazy {
        ModuleLinesOfCodeCounterImpl(defaultJson)
    }

    public companion object {
        public fun provideService(project: Project): Provider<ModuleGraphInfoExtractorService> {
            return registerService(project)
        }

        private fun registerService(project: Project): Provider<ModuleGraphInfoExtractorService> {
            return project.gradle.sharedServices.registerIfAbsent(
                ModuleGraphInfoExtractorService::class.java.name,
                ModuleGraphInfoExtractorService::class.java,
            )
        }
    }
}
