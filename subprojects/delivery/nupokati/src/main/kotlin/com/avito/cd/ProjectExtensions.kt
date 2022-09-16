package com.avito.cd

import com.avito.kotlin.dsl.ProjectProperty
import org.gradle.api.Project
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider

@Deprecated("Only for compile compatibility don't use")
public val Project.cdBuildConfig: Provider<CdBuildConfig> by ProjectProperty.lazy(
    factory = CdBuildConfigFactory()
)

private class CdBuildConfigFactory : (Project) -> Provider<CdBuildConfig> {

    override fun invoke(project: Project): Provider<CdBuildConfig> {
        return Providers.notDefined()
    }
}

@Suppress("DEPRECATION")
@Deprecated("Only for compile compatibility don't use", ReplaceWith(""))
public val Project.isCdBuildConfigPresent: Boolean
    get() = cdBuildConfig.isPresent
