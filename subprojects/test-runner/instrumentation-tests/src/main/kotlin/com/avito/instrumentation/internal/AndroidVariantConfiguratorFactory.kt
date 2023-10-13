package com.avito.instrumentation.internal

import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.LibraryVariant
import com.android.build.api.variant.TestVariant
import com.android.build.api.variant.Variant
import com.avito.instrumentation.configuration.InstrumentationTestsPluginExtension

internal class AndroidVariantConfiguratorFactory(
    private val extension: InstrumentationTestsPluginExtension
) {

    fun createConfigurator(
        variant: Variant
    ): AndroidVariantConfigurator<*>? {
        return when (variant) {

            is ApplicationVariant -> if (variant.androidTest != null) {
                ApplicationVariantConfigurator(variant)
            } else {
                null
            }

            is LibraryVariant -> if (variant.androidTest != null) {
                LibraryVariantConfigurator(variant)
            } else {
                null
            }

            is TestVariant -> TestVariantConfigurator(variant, extension)

            else -> throw IllegalStateException(
                "InstrumentationTestsPlugin doesn't support " +
                    "${variant::class.java.simpleName} variants yet"
            )
        }
    }
}
