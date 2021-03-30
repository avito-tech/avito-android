package com.avito.android.runner

import com.avito.android.stats.StatsDConfig
import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class TestRunEnvironmentKtTest {

    @Test
    fun `statsdconfig enabled - if required params passed`() {
        val params = StubArgsProvider()

        params.add("statsDHost", "http://stub.com")
        params.add("statsDPort", "8124")
        params.add("statsDNamespace", "apps.namespace")

        val result = parseStatsDConfig(params)

        assertThat(result).isInstanceOf<StatsDConfig.Enabled>()
    }
}
