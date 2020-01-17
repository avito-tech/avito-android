package com.avito.performance

import com.avito.report.model.HistoryTest
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class TestBeforeTargetCommitFilterTest {

    private val targetCommit = "targetCommit"
    private val detector: CommitAncestorDetector = mock()
    private val filter = TestBeforeTargetCommitFilter.Impl(detector, targetCommit)

    @BeforeEach
    fun setup() {
        given(detector.isParent(targetCommit, targetCommit)).willReturn(true)
    }

    @Test
    fun `filter - returns null - when get no tags`() {
        val list = listOf(
            givenTest(),
            givenTest(),
            givenTest()
        )
        val filteredTest = filter.filter(list)

        assertThat(filteredTest, IsNull())
    }

    @Test
    fun `filter - returns null - if test with proper target branch absent`() {
        val list = listOf(
            givenTest(buildCommit = "commit_1"),
            givenTest(buildCommit = "commit_2"),
            givenTest()
        )
        val filteredTest = filter.filter(list)

        assertThat(filteredTest, IsNull())
    }

    @Test
    fun `filter - returns test with proper target branch`() {
        val list = listOf(
            givenTest(buildCommit = "commit_1"),
            givenTest(buildCommit = targetCommit),
            givenTest()
        )
        val filteredTest = filter.filter(list)

        assertThat(filteredTest!!.getBuildCommit(), IsEqual(targetCommit))
    }

    private fun givenTest(buildCommit: String? = null): HistoryTest {
        return HistoryTest(
            id = "id",
            tags = if (buildCommit == null) emptyList() else listOf("buildCommit:$buildCommit")
        )
    }
}
