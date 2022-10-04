package com.avito.robolectric

import org.gradle.api.provider.Property

public interface RobolectricTestsExtension {
    public val reportRunIdPrefix: Property<String>
}
