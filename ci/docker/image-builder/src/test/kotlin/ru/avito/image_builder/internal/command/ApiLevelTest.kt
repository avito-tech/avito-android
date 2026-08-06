package ru.avito.image_builder.internal.command

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ApiLevelTest {

    @Test
    fun `api level without minor part - parse - has no minor`() {
        assertThat(ApiLevel.parse("30")).isEqualTo(ApiLevel.Major(30))
    }

    @Test
    fun `api level with minor part - parse - keeps both parts`() {
        assertThat(ApiLevel.parse("37.0")).isEqualTo(ApiLevel.MajorMinor(37, 0))
    }

    @Test
    fun `api level with minor part - toString - restores the whole value`() {
        assertThat(ApiLevel.parse("37.0").toString()).isEqualTo("37.0")
    }

    @Test
    fun `api level without minor part - toString - has no trailing dot`() {
        assertThat(ApiLevel.parse("30").toString()).isEqualTo("30")
    }

    @Test
    fun `not a number - parse - fails`() {
        val error = assertThrows<IllegalArgumentException> { ApiLevel.parse("android-37") }

        assertThat(error).hasMessageThat().contains("android-37")
    }

    @Test
    fun `trailing dot - parse - fails`() {
        assertThrows<IllegalArgumentException> { ApiLevel.parse("37.") }
    }

    @Test
    fun `three parts - parse - fails`() {
        assertThrows<IllegalArgumentException> { ApiLevel.parse("37.0.1") }
    }
}
