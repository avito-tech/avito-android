package com.avito.android

import org.gradle.api.provider.Property

/**
 * A task associated with a variant name.
 */
public interface VariantAwareTask {
    public val variantName: Property<String>
}
