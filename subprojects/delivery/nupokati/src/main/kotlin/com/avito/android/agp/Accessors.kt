package com.avito.android.agp

import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.VariantOutputConfiguration
import com.avito.android.Problem
import com.avito.android.asRuntimeException

internal fun ApplicationVariant.getVersionCode(): Int {
    return outputs.firstOrNull {
        // todo probably not a correct artifact, need to be more specific
        it.outputType == VariantOutputConfiguration.OutputType.SINGLE && it.enabled.get()
    }?.versionCode?.orNull ?: throw Problem(
        shortDescription = "versionCode in not available",
        context = "Configuring NupokatiPlugin",
        because = "No obvious reason, versionCode should be set here"
    ).asRuntimeException()
}
