package com.avito.cd

import com.avito.cd.Providers.gson
import com.avito.kotlin.dsl.ProjectProperty
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Project
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider
import java.io.File

val Project.cdBuildConfig: Provider<CdBuildConfig> by ProjectProperty.lazy(
    factory = CdBuildConfigFactory()
)

private class CdBuildConfigFactory : (Project) -> Provider<CdBuildConfig> {

    override fun invoke(project: Project): Provider<CdBuildConfig> {
        val provider = if (project.buildEnvironment is BuildEnvironment.CI) {
            val configFilePath = project.getOptionalStringProperty("cd.build.config.file")
            if (configFilePath != null) {
                Providers.of(configFilePath).map { path ->
                    val configFile = project.rootProject.file(path)
                    val config = deserializeToCdBuildConfig(configFile)
                    validate(config)
                    config
                }
            } else {
                Providers.notDefined()
            }
        } else {
            Providers.notDefined<CdBuildConfig>()
        }
        return provider
    }

    private fun deserializeToCdBuildConfig(configFile: File): CdBuildConfig {
        require(configFile.exists()) { "Can't find cd config file in $configFile" }
        return gson.fromJson<CdBuildConfig>(configFile.reader(), CdBuildConfig::class.java)
    }

    private fun validate(config: CdBuildConfig) {
        val googlePlayDeployments = config.deployments.filterIsInstance<CdBuildConfig.Deployment.GooglePlay>()
        val deploysByVariant = googlePlayDeployments.groupBy(CdBuildConfig.Deployment.GooglePlay::buildVariant)
        deploysByVariant.forEach { (_, deploys) ->
            if (deploys.size > 1) {
                throw IllegalArgumentException("Must be one deploy per variant, but was: $googlePlayDeployments")
            }
        }
    }
}


val Project.isCdBuildConfigPresent
    get() = cdBuildConfig.isPresent

val Project.buildOutput by ProjectProperty.lazy<Provider<BuildOutput>> {
    Providers.of(BuildOutput())
}
