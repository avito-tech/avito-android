package com.avito.android.clickstream.config

import com.avito.kotlin.dsl.ProjectProperty
import com.avito.kotlin.dsl.PropertyScope
import com.avito.kotlin.dsl.getMandatoryStringProperty
import org.gradle.api.Project
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider

public val Project.clickStreamConfig: Provider<ClickStreamConfig> by
ProjectProperty.lazy(scope = PropertyScope.ROOT_PROJECT) { project ->
    Providers.of(
        ClickStreamConfig(
            serviceUrl = project.getMandatoryStringProperty("avito.clickstream.serviceUrl"),
        )
    )
}
