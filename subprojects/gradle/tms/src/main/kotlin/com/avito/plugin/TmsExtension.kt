package com.avito.plugin

import org.gradle.api.provider.Property

abstract class TmsExtension {

    abstract val reportsHost: Property<String>
}
