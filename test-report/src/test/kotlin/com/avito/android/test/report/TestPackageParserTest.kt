package com.avito.android.test.report

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TestPackageParserTest {

    private val unitTagResolver: TestPackageParser = TestPackageParser.Impl()

    @Test
    fun `unit parsed with subpackages`() {
        val result = unitTagResolver.parse("com.avito.android.test.unit.some_feature")

        assertThat(result).isInstanceOf(TestPackageParser.Result.Success::class.java)
        assertThat((result as TestPackageParser.Result.Success).features).containsExactly("some_feature")
    }

    @Test
    fun `unit parsed with underscore with multiple subpackages`() {
        val result =
            unitTagResolver.parse("com.avito.android.test.unit.some_feature.some_inner_feature")

        assertThat(result).isInstanceOf(TestPackageParser.Result.Success::class.java)
        assertThat((result as TestPackageParser.Result.Success).features).containsExactlyElementsIn(
            listOf(
                "some_feature",
                "some_inner_feature"
            )
        )
    }
}
