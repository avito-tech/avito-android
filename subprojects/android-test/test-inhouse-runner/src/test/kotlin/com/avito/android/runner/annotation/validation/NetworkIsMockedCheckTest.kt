package com.avito.android.runner.annotation.validation

import com.avito.android.mock.MockWebServerApiRule
import com.avito.android.runner.annotation.resolver.TestMethodOrClass
import com.avito.android.runner.annotation.resolver.TestMockApiRule
import com.avito.android.test.annotations.E2ETest
import com.avito.android.test.annotations.UIComponentTest
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class NetworkIsMockedCheckTest {

    @Test
    fun `success - valid network usage`() {
        validate(ComponentMockWebServerApiTest::class.java)
        validate(ComponentMockApiTest::class.java)
        validate(E2ERealNetworkTest::class.java)
    }

    @Test
    fun `fail - component test with real network`() {
        val error = assertThrows<IllegalStateException> {
            validate(ComponentRealNetworkTest::class.java)
        }

        assertThat(error).hasMessageThat().contains("ComponentRealNetworkTest")
    }

    private fun validate(testClass: Class<*>) =
        NetworkIsMockedCheck().validate(
            TestMethodOrClass(testClass)
        )

    @UIComponentTest
    class ComponentMockWebServerApiTest {

        @get:Rule
        val rule = MockWebServerApiRule()

        @org.junit.Test
        fun test() {
        }
    }

    @UIComponentTest
    class ComponentMockApiTest {

        @get:Rule
        val rule = TestMockApiRule()

        @org.junit.Test
        fun test() {
        }
    }

    @UIComponentTest
    class ComponentRealNetworkTest {

        @org.junit.Test
        fun test() {
        }
    }

    @E2ETest
    class E2ERealNetworkTest {

        @org.junit.Test
        fun test() {
        }
    }
}
