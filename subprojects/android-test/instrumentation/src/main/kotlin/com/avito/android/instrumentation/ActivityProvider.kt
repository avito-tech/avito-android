package com.avito.android.instrumentation

import android.app.Activity

interface ActivityProvider {

    fun getCurrentActivity(): Activity?
}
