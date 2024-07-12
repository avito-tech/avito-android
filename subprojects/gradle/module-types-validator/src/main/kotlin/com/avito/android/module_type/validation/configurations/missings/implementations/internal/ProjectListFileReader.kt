package com.avito.android.module_type.validation.configurations.missings.implementations.internal

internal class ProjectListFileReader(
    private val reportFileText: String,
) {
    private val projectRegex = Regex("(?<=--- Project ').*(?='$)")

    fun readProjectList(): List<String> {
        return reportFileText.lines().mapNotNull { line ->
            projectRegex.find(line)?.value
        }
    }
}
