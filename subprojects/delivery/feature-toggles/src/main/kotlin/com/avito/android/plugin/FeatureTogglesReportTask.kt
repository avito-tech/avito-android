package com.avito.android.plugin

import com.avito.utils.ProcessRunner
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.gradle.api.DefaultTask
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

public abstract class FeatureTogglesReportTask : DefaultTask() {

    private val monthAgo = LocalDate.now().minusMonths(1L)
    private val quarterAgo = LocalDate.now().minusMonths(3L)
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    @InputFile
    public val jsonReportFile: File = File(project.buildDir, "reports/feature_toggles.json")

    @InputFile
    public val featureTogglesFile: File =
        File(project.rootDir, "common/features/src/main/java/com/avito/android/Features.kt")

    @get:Input
    public abstract val slackHook: Property<String>

    @get:Input
    public abstract val developersToTeam: MapProperty<DeveloperEmail, Team>

    @get:Input
    public abstract val projectDir: Property<String>

    @TaskAction
    public fun doWork() {
        val jsonToggles = readJsonReport()

        val processRunner = ProcessRunner.create(
            workingDirectory = File(projectDir.get())
        )
        val blameCodeLines = readBlameCodeLines(processRunner)
        val suspiciousToggles: List<Toggle> = SuspiciousTogglesCollector(
            developerToTeam = developersToTeam.get()
        ).collectSuspiciousToggles(
            jsonTogglesList = jsonToggles,
            blameCodeLinesList = blameCodeLines,
            turnedOnDateAgo = monthAgo,
            turnedOffDateAgo = quarterAgo
        )

        val sortedToggles = sortToggles(suspiciousToggles)
        val reportText = buildReportText(sortedToggles)

        sendReport(
            slackHook = slackHook.get(),
            reportText = reportText,
        )
    }

    private fun sendReport(slackHook: String, reportText: String) {
        val connection = URL(slackHook).openConnection() as HttpURLConnection
        try {
            with(connection) {
                requestMethod = "POST"
                doOutput = true
                doInput = true
                outputStream.bufferedWriter().use {
                    it.write("payload={\"text\": \"$reportText\"}")
                    it.flush()
                }
                logger.debug("Response: ${inputStream.bufferedReader().readText()}")
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun buildReportText(sortedToggles: List<TeamTogglesList>): String {
        val text = StringBuilder().append("Obsolete feature toggles :information_source: $newLineEscaped")

        sortedToggles.forEach {
            text.append(newLineEscaped)
            text.append("Unit: *${it.team}*")
            text.append("$newLineEscaped$newLineEscaped")
            if (it.turnedOffs.isEmpty() and it.turnedOns.isEmpty()) {
                text.append("> No obsolete toggles.")
                text.append(newLineEscaped)
            }
            if (it.turnedOns.isNotEmpty()) {
                text.append("*Turned on toggles:*$newLineEscaped")
                text.append(getTogglesString(it.turnedOns))
                text.append(newLineEscaped)
            }
            if (it.turnedOffs.isNotEmpty()) {
                text.append("*Turned off toggles:*$newLineEscaped")
                text.append(getTogglesString(it.turnedOffs))
                text.append(newLineEscaped)
            }
            text.append("____________________________________________________$newLineEscaped$newLineEscaped")
        }
        return text.toString()
    }

    private fun sortToggles(suspiciousTogglesList: List<Toggle>): List<TeamTogglesList> {
        val sortedToggles = mutableListOf<TeamTogglesList>()
        developersToTeam.get().values.forEach { team ->
            val unitToggles = suspiciousTogglesList.filter { it.team == team }
            val turnedOnToggles =
                unitToggles.filter { it.isOn }.sortedByDescending { it.changeDate }
            val turnedOffToggles =
                unitToggles.filter { !it.isOn }.sortedByDescending { it.changeDate }
            sortedToggles.add(
                TeamTogglesList(
                    team,
                    turnedOffs = turnedOffToggles,
                    turnedOns = turnedOnToggles
                )
            )
        }
        return sortedToggles
    }

    private fun getTogglesString(toggles: List<Toggle>): StringBuilder {
        val togglesText = StringBuilder()
        toggles.forEach {

            togglesText.append(
                "> Since:$tabEscaped*${it.changeDate.format(formatter)}*$tabEscaped${it.toggleName}$newLineEscaped"
            )
        }
        return togglesText
    }

    private fun readJsonReport(): List<JsonToggle> {
        return Gson().fromJson(
            jsonReportFile.readText(),
            object : TypeToken<List<JsonToggle>>() {}.type
        )
    }

    private fun readBlameCodeLines(processRunner: ProcessRunner) =
        BlameParser().parseBlameCodeLines(processRunner.getBlame())

    private fun ProcessRunner.getBlame() =
        run(
            command = "git blame -w ${featureTogglesFile.path} -et",
            timeout = Duration.ofSeconds(10)
        ).getOrThrow()
}

private const val newLineEscaped = "\\n"
private const val tabEscaped = "\\t"
