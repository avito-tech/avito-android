package com.avito.plugin

import com.avito.plugin.internal.validateUrl
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class UrlResolverTest {

    @Test
    fun `path with segments`() {
        val arg = "https://some.ru/service-signer/1"
        val result = validateUrl(arg) { error("") }

        assertThat(result).isEqualTo("https://some.ru/service-signer/1/sign")
    }
}
