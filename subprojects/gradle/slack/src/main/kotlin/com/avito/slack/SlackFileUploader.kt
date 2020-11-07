package com.avito.slack

import com.avito.slack.model.SlackChannel
import org.funktionale.tries.Try
import java.io.File

interface SlackFileUploader {

    fun uploadHtml(
        channel: SlackChannel,
        message: String,
        file: File
    ): Try<Unit>
}
