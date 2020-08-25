package com.avito.android.activity

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.test.espresso.idling.CountingIdlingResource
import com.avito.android.test.screenshot_test.test.IdlieableActivity

class ScreenshotTestActivity : IdlieableActivity() {

    override val countingIdlingResource = CountingIdlingResource("CountingIdlingResource", true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault_DayNight)

        val params = ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT
        )
        val layout = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            orientation = LinearLayout.VERTICAL
            layoutParams = params
        }

        setContentView(layout)
    }
}
