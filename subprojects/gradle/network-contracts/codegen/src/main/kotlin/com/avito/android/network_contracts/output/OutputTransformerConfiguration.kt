package com.avito.android.network_contracts.output

import org.gradle.api.Named
import org.gradle.api.provider.Property

public interface OutputTransformerConfiguration : Named {

    public val transformer: Property<OutputTransformer>
}
