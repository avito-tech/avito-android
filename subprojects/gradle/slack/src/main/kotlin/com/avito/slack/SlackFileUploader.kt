package com.avito.slack

import com.avito.android.Result
import com.avito.slack.model.SlackChannelId
import java.io.File

interface SlackFileUploader {

    fun uploadHtml(
        channelId: SlackChannelId,
        message: String,
        file: File
    ): Result<Unit>
}
