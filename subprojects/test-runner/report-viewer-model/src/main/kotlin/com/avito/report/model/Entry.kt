package com.avito.report.model

public sealed class Entry(
    public val type: String
) {

    public abstract val timeInSeconds: Long

    public data class File(
        val comment: String,
        val fileAddress: FileAddress,
        override val timeInSeconds: Long,
        val fileType: Type
    ) : Entry(type = fileType.name) {

        @Suppress("EnumEntryName")
        public enum class Type {
            html,
            img_png,
            video,
            plain_text
        }

        // for test fixtures
        public companion object
    }

    public data class Comment(
        val title: String,
        override val timeInSeconds: Long
    ) : Entry(type = "comment")

    public data class Field(
        val comment: String,
        val value: String,
        override val timeInSeconds: Long
    ) : Entry(type = "field")

    /**
     * Ничем кроме иконки в ReportViewer не отличается от Comment
     */
    public data class Check(
        val title: String,
        override val timeInSeconds: Long
    ) : Entry(type = "check")
}
