package com.avito.android.runner.annotation.resolver

import android.os.Bundle

interface TestMetadataInjector {

    fun inject(instrumentationArguments: Bundle)
}

class AnnotationResolversBasedMetadataInjector(
    private val annotationResolvers: Set<TestMetadataResolver>
) : TestMetadataInjector {

    override fun inject(instrumentationArguments: Bundle) {
        val fullyQualifiedTestName = instrumentationArguments.getString("class")

        if (fullyQualifiedTestName.isNullOrBlank()) {
            throw RuntimeException("Test name not found in instrumentation arguments: $instrumentationArguments")
        }

        for (annotationResolver in annotationResolvers) {

            when (val switchResolution = annotationResolver.resolve(fullyQualifiedTestName)) {
                is TestMetadataResolver.Resolution.ReplaceString ->
                    instrumentationArguments.putString(
                        annotationResolver.key, // TODO: protect against accidental collision in keys
                        switchResolution.replacement
                    )
                is TestMetadataResolver.Resolution.ReplaceSerializable ->
                    instrumentationArguments.putSerializable(
                        annotationResolver.key,
                        switchResolution.replacement
                    )
                is TestMetadataResolver.Resolution.NothingToChange -> {
                }
            }
        }
    }
}
