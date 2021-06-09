package com.avito.report.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class TestNameFeaturesParsingTest {

    @Test
    fun `testName features - returns empty list - for test on root unit package`() {
        assertThat(TestName(name = "com.avito.android.test.geo.MyTest.test").features).isEmpty()
    }

    @Test
    fun `testName features - returns single feature`() {
        assertThat(TestName(name = "com.avito.android.test.geo.my_feature.MyTest.test").features)
            .containsExactly("my_feature")
    }

    @Test
    fun `testName features - returns multiple feature`() {
        assertThat(
            TestName(
                name = "com.avito.android.test.geo.my_feature.my_inner_feature.my_inner_inner_feature.MyTest.test"
            ).features
        ).containsExactly("my_feature", "my_inner_feature", "my_inner_inner_feature")
    }

    @Test
    fun `testName features - returns empty list - for illegal testName`() {
        assertThat(TestName(name = "illegalTestName").features).isEmpty()
    }

    @Test
    fun `testName features - returns single feature - for domofond`() {
        assertThat(TestName(name = "ru.domofond.test.feature.MyTest.test").features).containsExactly("feature")
    }
}
