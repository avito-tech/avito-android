package com.avito.android.runner.annotation.resolver

import com.avito.android.runner.annotation.resolver.TestMetadataResolver.Resolution
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AnnotationResolversBasedMetadataInjectorTest {

    @Test
    fun `resolvers can not override each other`() {
        val error = assertThrows<IllegalArgumentException> {
            AnnotationResolversBasedMetadataInjector(
                setOf(
                    stubResolver("key", Resolution.ReplaceString("value 1")),
                    stubResolver("key", Resolution.ReplaceString("value 2"))
                )
            )
        }
        assertThat(error).hasMessageThat().contains("Multiple TestMetadataResolvers have the same key: key")
    }

    private fun stubResolver(key: String, resolution: Resolution): TestMetadataResolver =
        object : TestMetadataResolver {
            override val key: String
                get() = key

            override fun resolve(test: TestMethodOrClass): Resolution {
                return resolution
            }
        }
}
