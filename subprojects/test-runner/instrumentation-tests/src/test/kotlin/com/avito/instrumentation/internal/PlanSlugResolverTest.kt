package com.avito.instrumentation.internal

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class PlanSlugResolverTest {

    @Test
    fun `planSlug - for one level path`() {
        val planSlug = PlanSlugResolver.generateDefaultPlanSlug(":app")

        assertThat(planSlug).isEqualTo("AppAndroid")
    }

    @Test
    fun `planSlug - for two level path`() {
        val planSlug = PlanSlugResolver.generateDefaultPlanSlug(":demo:app")

        assertThat(planSlug).isEqualTo("DemoAppAndroid")
    }

    @Test
    fun `planSlug - for path with symbols`() {
        val planSlug = PlanSlugResolver.generateDefaultPlanSlug(":demo-app")

        assertThat(planSlug).isEqualTo("DemoAppAndroid")
    }
}
