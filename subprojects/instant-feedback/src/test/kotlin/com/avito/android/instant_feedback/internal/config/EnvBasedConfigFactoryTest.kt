package com.avito.android.instant_feedback.internal.config

import com.avito.android.isFailure
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class EnvBasedConfigFactoryTest {

    @Test
    fun `missing environment variable - returns failure - when creating`() {
        val result = EnvBasedConfigFactory.create()
        assertThat(result.isFailure()).isTrue()
    }
}
