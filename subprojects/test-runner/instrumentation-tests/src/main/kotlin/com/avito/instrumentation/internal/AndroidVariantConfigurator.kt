package com.avito.instrumentation.internal

import com.android.build.api.variant.Variant

internal interface AndroidVariantConfigurator<T : Variant> : InstrumentationTaskConfigurator {

    val variant: T
}
