package com.avito.android.network_contracts.extension.urls

import org.gradle.api.provider.Property

public interface ServiceUrlConfiguration : UrlConfiguration {

    public val serviceUrl: Property<String>
}
