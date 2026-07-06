package com.avito.android.clickstream.config

import com.avito.kotlin.dsl.ProjectProperty
import com.avito.kotlin.dsl.PropertyScope
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.kotlin.dsl.getOptionalLongProperty
import org.gradle.api.Project
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider

public val Project.clickStreamConfig: Provider<ClickStreamConfig> by
ProjectProperty.lazy(scope = PropertyScope.ROOT_PROJECT) { project ->
    Providers.of(
        ClickStreamConfig(
            serviceUrl = project.getMandatoryStringProperty("avito.clickstream.serviceUrl"),
            readTimeOutInSeconds = project.getOptionalLongProperty("avito.clickstream.readTimeOutInSeconds")
                ?: DEFAULT_READ_TIMEOUT,
            connectTimeOutInSeconds = project.getOptionalLongProperty("avito.clickstream.connectTimeOutInSeconds")
                ?: DEFAULT_CONNECT_TIMEOUT,
            useLegacyEndpoint = project.getBooleanProperty("avito.clickstream.useLegacyEndpoint", true)
        )
    )
}

private const val DEFAULT_READ_TIMEOUT = 30L
private const val DEFAULT_CONNECT_TIMEOUT = 30L
