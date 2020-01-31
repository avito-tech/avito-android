package com.avito.report.model

sealed class Entry(
    val type: String,
    open val timeInSeconds: Long
) {

    data class File(
        val comment: String,
        val fileAddress: String,
        override val timeInSeconds: Long,
        val fileType: Type
    ) : Entry(type = fileType.name, timeInSeconds = timeInSeconds) {

        @Suppress("EnumEntryName")
        enum class Type {
            html,
            img_png,
            video,
            plain_text
        }
    }

    data class Comment(
        val title: String,
        override val timeInSeconds: Long
    ) : Entry(type = "comment", timeInSeconds = timeInSeconds)

    data class Field(
        val comment: String,
        val value: String,
        override val timeInSeconds: Long
    ) : Entry(type = "field", timeInSeconds = timeInSeconds)

    /**
     * Ничем кроме иконки в ReportViewer не отличается от Comment
     */
    data class Check(
        val title: String,
        override val timeInSeconds: Long
    ) : Entry(type = "check", timeInSeconds = timeInSeconds)
}
