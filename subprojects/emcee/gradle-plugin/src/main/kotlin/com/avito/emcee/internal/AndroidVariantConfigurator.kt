package com.avito.emcee.internal

import com.android.build.api.variant.Variant

internal abstract class AndroidVariantConfigurator<T : Variant>(val variant: T) : EmceeTestTaskConfigurator
