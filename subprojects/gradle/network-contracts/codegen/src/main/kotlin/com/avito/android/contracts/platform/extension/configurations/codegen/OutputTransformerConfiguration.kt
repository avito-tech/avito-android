package com.avito.android.contracts.platform.extension.configurations.codegen

import com.avito.android.contracts.platform.output.OutputTransformer
import org.gradle.api.Named
import org.gradle.api.provider.Property

public interface OutputTransformerConfiguration : Named {

    public val transformer: Property<OutputTransformer>
}
