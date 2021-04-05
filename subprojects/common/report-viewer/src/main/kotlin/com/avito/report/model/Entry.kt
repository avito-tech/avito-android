package com.avito.report.model

sealed class Entry(
    val type: String
) {
    abstract val timeInSeconds: Long

    data class File(
        val comment: String,
        val fileAddress: FileAddress,
        override val timeInSeconds: Long,
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
        override val timeInSeconds: Long
    ) : Entry(type = "comment")

    data class Field(
        val comment: String,
        val value: String,
        override val timeInSeconds: Long
    ) : Entry(type = "field")

    /**
     * Ничем кроме иконки в ReportViewer не отличается от Comment
     */
    data class Check(
        val title: String,
        override val timeInSeconds: Long
    ) : Entry(type = "check")
}
