package com.avito.android.runner.annotation.resolver

import android.os.Bundle

interface TestMetadataInjector {

    fun inject(test: TestMethodOrClass, instrumentationArguments: Bundle)
}

class AnnotationResolversBasedMetadataInjector(
    private val annotationResolvers: Set<TestMetadataResolver>
) : TestMetadataInjector {

    init {
        requireUniqueKeys()
    }

    override fun inject(test: TestMethodOrClass, instrumentationArguments: Bundle) {
        val testMetadata = resolveMetadata(test)
        requireNoDuplicates(instrumentationArguments, testMetadata)
        instrumentationArguments.putAll(testMetadata)
    }

    private fun resolveMetadata(test: TestMethodOrClass): Bundle {
        val metadata = Bundle()

        for (annotationResolver in annotationResolvers) {

            when (val switchResolution = annotationResolver.resolve(test)) {
                is TestMetadataResolver.Resolution.ReplaceString ->
                    metadata.putString(
                        annotationResolver.key,
                        switchResolution.replacement
                    )
                is TestMetadataResolver.Resolution.ReplaceSerializable ->
                    metadata.putSerializable(
                        annotationResolver.key,
                        switchResolution.replacement
                    )
                is TestMetadataResolver.Resolution.NothingToChange -> {
                }
            }
        }
        return metadata
    }

    // TODO: wrap to a bundle to avoid accidental overriding.
    private fun requireNoDuplicates(left: Bundle, right: Bundle) {
        val duplicatedKeys = right.keySet().intersect(left.keySet())
        require(duplicatedKeys.isEmpty()) {
            "Duplicates in test arguments: $duplicatedKeys.\n" +
                "- $left\n" +
                "- $right"
        }
    }

    private fun requireUniqueKeys() {
        val uniqueKeys = mutableSetOf<String>()

        annotationResolvers.forEach { resolver ->
            val key = resolver.key

            if (uniqueKeys.contains(key)) {
                val duplicates = annotationResolvers.filter { it.key == key }
                throw IllegalArgumentException(
                    "Multiple TestMetadataResolvers have the same key: ${key}\n" +
                        "Resolvers: $duplicates"
                )
            }
            uniqueKeys.add(key)
        }
    }
}
