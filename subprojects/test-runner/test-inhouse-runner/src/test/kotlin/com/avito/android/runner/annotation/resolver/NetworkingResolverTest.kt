package com.avito.android.runner.annotation.resolver

import com.avito.android.mock.MockWebServerApiRule
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.jupiter.api.Test

class NetworkingResolverTest {

    @Test
    fun `test with MockWebServerApiRule - resolve - returns MOCK_WEB_SERVER`() {
        val result = NetworkingResolver().resolver(TestWithMockWebServerRule::class.java)

        assertThat(result).isEqualTo(
            TestMetadataResolver.Resolution.ReplaceSerializable(NetworkingType.MOCK_WEB_SERVER)
        )
    }

    @Test
    fun `test with AbstractMockApiRule - resolve - returns MOCKED_NETWORK_LAYER`() {
        val result = NetworkingResolver().resolver(TestWithAbstractMockApiRule::class.java)

        assertThat(result).isEqualTo(
            TestMetadataResolver.Resolution.ReplaceSerializable(NetworkingType.MOCKED_NETWORK_LAYER)
        )
    }

    @Test
    fun `test with both mock rules - resolve - returns ILLEGAL`() {
        val result = NetworkingResolver().resolver(TestWithBothMockRules::class.java)

        assertThat(result).isEqualTo(
            TestMetadataResolver.Resolution.ReplaceSerializable(NetworkingType.ILLEGAL)
        )
    }

    @Test
    fun `test without mock rules - resolve - returns REAL`() {
        val result = NetworkingResolver().resolver(TestWithoutMocking::class.java)

        assertThat(result).isEqualTo(
            TestMetadataResolver.Resolution.ReplaceSerializable(NetworkingType.REAL)
        )
    }
}

private class TestWithMockWebServerRule {
    @get:Rule
    val mockApi = MockWebServerApiRule()
}

private class TestWithAbstractMockApiRule {
    @get:Rule
    val mockApi = TestMockApiRule()
}

private class TestWithBothMockRules {
    @get:Rule
    val mockWebServer = MockWebServerApiRule()

    @get:Rule
    val mockApi = TestMockApiRule()
}

private class TestWithoutMocking
