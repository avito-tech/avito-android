package com.avito.cd

import com.avito.kotlin.dsl.ProjectProperty
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.gradle.api.Project
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider
import java.io.File

public val Project.cdBuildConfig: Provider<CdBuildConfig> by ProjectProperty.lazy(
    factory = CdBuildConfigFactory()
)

private class CdBuildConfigFactory : (Project) -> Provider<CdBuildConfig> {

    override fun invoke(project: Project): Provider<CdBuildConfig> {
        return if (project.buildEnvironment is BuildEnvironment.CI) {
            val configFilePath = project.getOptionalStringProperty("cd.build.config.file")
            if (configFilePath != null) {
                Providers.of(configFilePath).map { path ->
                    val configFile = project.rootProject.file(path)
                    val config = deserializeToCdBuildConfig(configFile)
                    CdBuildConfigValidator(config).validate()
                    config
                }
            } else {
                Providers.notDefined()
            }
        } else {
            Providers.notDefined()
        }
    }

    private fun deserializeToCdBuildConfig(configFile: File): CdBuildConfig {
        require(configFile.exists()) { "Can't find cd config file in $configFile" }
        return uploadCdGson.fromJson(configFile.reader(), CdBuildConfig::class.java)
    }
}

public val Project.isCdBuildConfigPresent: Boolean
    get() = cdBuildConfig.isPresent

public val Project.buildOutput: Provider<BuildOutput> by ProjectProperty.lazy<Provider<BuildOutput>> {
    Providers.of(BuildOutput())
}

// todo open for tests; create serializer
public val uploadCdGson: Gson by lazy {
    GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .disableHtmlEscaping()
        .registerTypeAdapter(CdBuildConfig.Deployment::class.java, DeploymentDeserializer)
        .create()
}
