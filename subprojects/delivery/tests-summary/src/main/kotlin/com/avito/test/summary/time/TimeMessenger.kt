package com.avito.test.summary.time

import com.avito.android.Result

internal interface TimeMessenger {

    fun createPost(channelId: String, message: String): Result<Unit>
}
