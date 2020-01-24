package com.avito.android.monitoring

import com.google.common.truth.Truth.assertThat
import okhttp3.HttpUrl
import org.junit.Test

class StatsdHttpTrackerKtTest {

    @Test
    fun `resource manager url converter to metric`() {
        assertThat(convertUrlToMetricKey(HttpUrl.parse("https://host.ru/api/something/get")!!))
            .isEqualTo("host_api_something_get")
    }
}
