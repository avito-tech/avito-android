package com.avito.android.instrumentation

import android.app.Activity
import com.avito.android.Result

public interface ActivityProvider {

    public fun getCurrentActivity(): Result<Activity>
}
