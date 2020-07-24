package com.avito.android.monitoring

import com.google.common.truth.Truth.assertThat
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.junit.jupiter.api.Test

class StatsdHttpTrackerKtTest {

    @Test
    fun `resource manager url converter to metric`() {
        assertThat(convertUrlToMetricKey("https://host.ru/api/something/get".toHttpUrlOrNull()!!))
            .isEqualTo("host_api_something_get")
    }
}
