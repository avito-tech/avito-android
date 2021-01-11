package com.avito.android.plugin

import com.avito.logger.LoggerFactory
import java.time.LocalDate

typealias Team = String
typealias DeveloperEmail = String

internal class SuspiciousTogglesCollector(
    loggerFactory: LoggerFactory,
    private val developerToTeam: Map<DeveloperEmail, Team>
) {

    private val logger = loggerFactory.create("SuspiciousTogglesCollector")

    private val excludedToggles = listOf(
        "stetho",
        "certificate_pinning",
        "leak_canary",
        "schema_check"
    )

    fun collectSuspiciousToggles(
        jsonTogglesList: List<JsonToggle>,
        blameCodeLinesList: List<CodeElement>,
        turnedOnDateAgo: LocalDate,
        turnedOffDateAgo: LocalDate
    ): List<Toggle> {
        return jsonTogglesList
            .filter { it.value is Boolean }
            .asSequence()
            .map { jsonToggle ->
                val codeElement = blameCodeLinesList.find { it.codeLine.contains(jsonToggle.key) }
                val toggleIndex = blameCodeLinesList.indexOf(codeElement)
                val lastChangeCodeItem = blameCodeLinesList
                    .subList(toggleIndex, blameCodeLinesList.size)
                    .find { it.codeLine.contains("defaultValue") }

                if (lastChangeCodeItem == null) {
                    logger.critical("Error: ${jsonToggle.key}", NoSuchElementException(jsonToggle.key))
                    null
                } else {
                    Toggle(
                        toggleName = jsonToggle.key,
                        isOn = jsonToggle.value.toString().toBoolean(),
                        changeDate = lastChangeCodeItem.changeTime,
                        team = resolveTeam(lastChangeCodeItem)
                    )
                }
            }
            .filterNotNull()
            .filter {
                it.isOn && it.changeDate < turnedOnDateAgo
                    || !it.isOn && it.changeDate < turnedOffDateAgo
            }
            .filterNot { excludedToggles.contains(it.toggleName) }
            .toList()
    }

    private fun resolveTeam(lastChangeCodeItem: CodeElement): Team =
        developerToTeam[lastChangeCodeItem.email] ?: "undefined"
}
