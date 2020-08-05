package com.avito.android.test.screenshot_test.test

import androidx.appcompat.app.AppCompatActivity
import androidx.test.espresso.idling.CountingIdlingResource

abstract class IdlieableActivity: AppCompatActivity() {
    abstract var countingIdlingResource: CountingIdlingResource
}