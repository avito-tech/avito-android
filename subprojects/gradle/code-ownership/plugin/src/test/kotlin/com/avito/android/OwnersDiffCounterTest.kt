package com.avito.android

import com.avito.android.diff.comparator.EqualsOwnersComparator
import com.avito.android.diff.counter.OwnersDiffCounterImpl
import com.avito.android.model.Owner
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class OwnersDiffCounterTest {

    private val comparator = EqualsOwnersComparator()
    private val diffCounter = OwnersDiffCounterImpl(comparator)

    @Test
    fun `no changes in owners - diff is empty`() {
        val owners = setOf(Speed, Performance, MobileArchitecture)
        val diff = diffCounter.countOwnersDiff(expectedOwners = owners, actualOwners = owners)

        assertThat(diff).isEmpty()
    }

    @Test
    fun `expected owners are empty - diff contains only additions`() {
        val actualOwners = setOf(Speed, Performance, MobileArchitecture)
        val diff = diffCounter.countOwnersDiff(expectedOwners = emptySet(), actualOwners = actualOwners)

        assertThat(diff).hasSize(actualOwners.size)
        assertThat(diff.added).isNotEmpty()
        assertThat(diff.removed).isEmpty()
    }

    @Test
    fun `actual owners are empty - diff contains only removals`() {
        val expectedOwners = setOf(Speed, Performance, MobileArchitecture)
        val diff = diffCounter.countOwnersDiff(expectedOwners = expectedOwners, actualOwners = emptySet())

        assertThat(diff).hasSize(expectedOwners.size)
        assertThat(diff.added).isEmpty()
        assertThat(diff.removed).isNotEmpty()
    }

    @Test
    fun `owners changed without intersection - diff contains all elements`() {
        val expectedOwners = setOf(Speed, Performance)
        val actualOwners = setOf(MobileArchitecture)
        val diff = diffCounter.countOwnersDiff(expectedOwners, actualOwners)

        assertThat(diff).hasSize(expectedOwners.size + actualOwners.size)
        assertThat(diff.added).containsExactly(MobileArchitecture)
        assertThat(diff.removed).containsExactly(Speed, Performance)
    }

    @Test
    fun `owners changed with intersection - diff contains unique elements`() {
        val expectedOwners = setOf(Speed, Performance)
        val actualOwners = setOf(Speed, MobileArchitecture)
        val diff = diffCounter.countOwnersDiff(expectedOwners, actualOwners)

        assertThat(diff.added).containsExactly(MobileArchitecture)
        assertThat(diff.removed).containsExactly(Performance)
        assertThat(diff).doesNotContain(Speed)
    }

    private object Speed : Owner
    private object Performance : Owner
    private object MobileArchitecture : Owner
}
