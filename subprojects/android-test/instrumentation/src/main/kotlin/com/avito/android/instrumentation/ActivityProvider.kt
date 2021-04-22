package com.avito.android.instrumentation

import android.app.Activity
import com.avito.android.Result

interface ActivityProvider {

    fun getCurrentActivity(): Result<Activity>
}
