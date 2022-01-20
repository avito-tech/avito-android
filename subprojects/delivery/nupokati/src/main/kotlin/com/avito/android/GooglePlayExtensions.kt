package com.avito.android

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

public abstract class GooglePlayExtensions {

    public abstract val keyFile: RegularFileProperty

    /**
     * used in tests
     */
    public abstract val mockUrl: Property<String>
}
