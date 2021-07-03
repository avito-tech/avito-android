package com.avito.plugin

import org.gradle.api.provider.Property

public abstract class TmsExtension {

    public abstract val reportsHost: Property<String>
}
