package com.avito.runner.scheduler.suite.filter

import com.avito.android.AnnotationData
import com.avito.runner.scheduler.suite.config.InstrumentationFilterData
import com.avito.runner.scheduler.suite.config.createStub
import com.avito.runner.scheduler.suite.filter.TestsFilter.Verdict.Included
import com.avito.runner.scheduler.suite.filter.TestsFilter.Verdict.NotForTarget
import com.avito.runner.scheduler.suite.filter.TestsFilter.Verdict.Skipped
import com.avito.test.model.DeviceName
import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ChangedTestsByTargetFilterTest {

    private val changed = "com.test.Changed.test"
    private val notChanged = "com.test.NotChanged.test"
    private val changedTarget = DeviceName("api30PRChanged")
    private val mainTarget = DeviceName("api30PR")
    private val otherTarget = DeviceName("api36")

    private val filter = StubFilterFactoryFactory.create(
        filter = InstrumentationFilterData.createStub(),
        changedTestsFilterInput = ChangedTestsFilterInput.createStubInstance(
            targetFilterMode = TargetFilterMode.ByTarget(
                mapOf(
                    changedTarget.name to ChangedTestsFilterMode.CHANGED,
                    mainTarget.name to ChangedTestsFilterMode.ALL_EXCEPT_CHANGED,
                )
            ),
            changedTests = listOf("com.test.Changed"),
        )
    ).createFilter()

    @Test
    fun `changed test - runs only on changed target`() {
        assertThat(filter.filter(test(changed, changedTarget))).isInstanceOf<Included>()
        assertThat(filter.filter(test(changed, mainTarget))).isInstanceOf<NotForTarget>()
    }

    @Test
    fun `not changed test - runs only on main target`() {
        assertThat(filter.filter(test(notChanged, mainTarget))).isInstanceOf<Included>()
        assertThat(filter.filter(test(notChanged, changedTarget))).isInstanceOf<NotForTarget>()
    }

    @Test
    fun `target without mode - configuration error`() {
        val error = assertThrows<IllegalArgumentException> { filter.filter(test(changed, otherTarget)) }
        assertThat(error).hasMessageThat().contains(otherTarget.name)
    }

    @Test
    fun `not changed ignored test on changed target - not for target, not skipped by annotation`() {
        val ignored = listOf(AnnotationData(FilterFactory.JUNIT_IGNORE_ANNOTATION, emptyMap()))

        assertThat(filter.filter(test(notChanged, changedTarget, ignored))).isInstanceOf<NotForTarget>()
        assertThat(filter.filter(test(notChanged, mainTarget, ignored))).isInstanceOf<Skipped.HasExcludeAnnotations>()
    }

    private fun test(name: String, target: DeviceName, annotations: List<AnnotationData> = emptyList()) =
        TestsFilter.Test.createStub(name = name, deviceName = target, annotations = annotations)
}
