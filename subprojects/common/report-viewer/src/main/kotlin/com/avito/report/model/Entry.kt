package com.avito.report.model

sealed class Entry(
    val type: String
) {
    abstract val timeInMs: Long

    data class File(
        val comment: String,
        val fileAddress: String,
        override val timeInMs: Long,
        val fileType: Type
    ) : Entry(type = fileType.name) {

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
        override val timeInMs: Long
    ) : Entry(type = "comment")

    data class Field(
        val comment: String,
        val value: String,
        override val timeInMs: Long
    ) : Entry(type = "field")

    /**
     * Ничем кроме иконки в ReportViewer не отличается от Comment
     */
    data class Check(
        val title: String,
        override val timeInMs: Long
    ) : Entry(type = "check")
}
