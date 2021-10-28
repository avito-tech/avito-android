package com.avito.instrumentation.internal

import com.android.build.api.variant.Variant

internal abstract class AndroidVariantConfigurator<T : Variant>(val variant: T) : InstrumentationTaskConfigurator
