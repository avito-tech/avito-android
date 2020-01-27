package com.avito.android.monitoring

import okhttp3.Response

interface HttpTracker {

    fun trackRequest(response: Response)
}
