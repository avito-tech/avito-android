package com.avito.sharedtests

import org.gradle.api.provider.Property

public interface SharedTestsExtension {
    public val reportRunIdPrefix: Property<String>
}
