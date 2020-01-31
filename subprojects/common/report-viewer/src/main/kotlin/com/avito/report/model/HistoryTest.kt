package com.avito.report.model

data class HistoryTest(
    val id: String,
    val tags: List<String>
) {
    //Строка имеет вид "buildCommit:gitHash"
    fun getBuildCommit(): String? {
        return tags.findLast { it.startsWith("buildCommit") }
            ?.split(":")
            ?.last()
    }
}
