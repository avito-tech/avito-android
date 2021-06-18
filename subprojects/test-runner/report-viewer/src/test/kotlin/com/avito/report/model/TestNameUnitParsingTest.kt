package com.avito.report.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class TestNameUnitParsingTest {

    @Test
    fun `testName unit - parsed for avito data`() {
        assertThat(TestName("com.avito.android.test.geo.MyTest", "test").team).isEqualTo(Team("geo"))
    }

    @Test
    fun `testName unit - parsed for domofond data`() {
        assertThat(TestName("ru.domofond.test.MyTest", "test").team).isEqualTo(Team("domofond"))
    }

    @Test
    fun `unit parsed with subpackages`() {
        assertThat(TestName("com.avito.android.test.auto.some_feature.MyTest", "test").team)
            .isEqualTo(Team("auto"))
    }

    @Test
    fun `unit parsed with underscore`() {
        assertThat(TestName("com.avito.android.test.seller_x.some_feature.MyTest", "test").team)
            .isEqualTo(Team("seller-x"))
    }

    @Test
    fun `unit parsed with underscore with multiple subpackages`() {
        assertThat(TestName("com.avito.android.test.seller_x.some_feature.some_inner_feature.MyTest", "test").team)
            .isEqualTo(Team("seller-x"))
    }

    @Test
    fun `unit undefined for illegal testName`() {
        assertThat(TestName("illegalTestName", "test").team).isEqualTo(Team.UNDEFINED)
    }
}
