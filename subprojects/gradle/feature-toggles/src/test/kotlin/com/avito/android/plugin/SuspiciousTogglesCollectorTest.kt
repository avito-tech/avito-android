package com.avito.android.plugin

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.gradle.api.logging.Logger
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class SuspiciousTogglesCollectorTest {

    private lateinit var collector: SuspiciousTogglesCollector

    @Mock
    lateinit var logger: Logger

    private val monthAgo = LocalDate.parse("2018-05-20")
    private val quarterAgo = LocalDate.parse("2018-03-20")

    @BeforeEach
    fun setUp() {
        collector = SuspiciousTogglesCollector(logger, developerToTeam = mapOf())
    }

    @Test
    fun `collect toggles - merge json and code toggles`() {

        val jsonTogglesList = listOf(
            JsonToggle("toggle1", true),
            JsonToggle("toggle2", null),
            JsonToggle("toggle3", false),
            JsonToggle("toggle4", "api"),
            JsonToggle("toggle5", true),
            JsonToggle("toggle6", false),
            JsonToggle("toggle7", false),
            JsonToggle("stetho", false)
        )
        val blameCodeLinesList = listOf(
            CodeElement(" toggle1", LocalDate.parse("2018-02-20"), "dkostyrev@avito.ru"),
            CodeElement(" defaultValue=true", LocalDate.parse("2018-02-20"), "dkostyrev@avito.ru"),
            CodeElement(" toggle2", LocalDate.parse("2018-02-20"), "dkostyrev@avito.ru"),
            CodeElement(" defaultValue=null", LocalDate.parse("2018-02-20"), "dkostyrev@avito.ru"),
            CodeElement(" toggle3", LocalDate.parse("2018-02-20"), "dkostyrev@avito.ru"),
            CodeElement(" defaultValue=false", LocalDate.parse("2018-02-20"), "dkostyrev@avito.ru"),
            CodeElement(" toggle4", LocalDate.parse("2018-02-20"), "dkostyrev@avito.ru"),
            CodeElement(" defaultValue=api", LocalDate.parse("2018-02-20"), "dkostyrev@avito.ru"),
            CodeElement(" toggle5", LocalDate.parse("2018-03-20"), "dkostyrev@avito.ru"),
            CodeElement(" defaultValue=true", LocalDate.parse("2018-05-21"), "dkostyrev@avito.ru"),
            CodeElement(" toggle6", LocalDate.parse("2018-01-20"), "dkostyrev@avito.ru"),
            CodeElement(" defaultValue=false", LocalDate.parse("2018-03-21"), "dkostyrev@avito.ru"),
            CodeElement(" toggle7", LocalDate.parse("2018-02-20"), "dvoronin@avito.ru"),
            CodeElement(" defaultValue=false", LocalDate.parse("2018-02-20"), "dvoronin@avito.ru"),
            CodeElement(" stetho", LocalDate.parse("2018-02-20"), "dvoronin@avito.ru"),
            CodeElement(" defaultValue=false", LocalDate.parse("2018-02-20"), "dvoronin@avito.ru")
        )

        val toggles = collector.collectSuspiciousToggles(
            jsonTogglesList, blameCodeLinesList,
            turnedOnDateAgo = monthAgo, turnedOffDateAgo = quarterAgo
        )

        verifyNoMoreInteractions(logger)
        assertThat(toggles[0].toggleName).isEqualTo("toggle1")
        assertThat(toggles[1].toggleName).isEqualTo("toggle3")
        assertThat(toggles[2].toggleName).isEqualTo("toggle7")
        assertThat(toggles[2].team).isEqualTo("undefined")
        assertThat(toggles.size).isEqualTo(3)
    }
}
