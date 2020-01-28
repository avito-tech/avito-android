package com.avito.android.ui.test

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.avito.android.test.page_object.PageObject
import com.avito.android.test.page_object.ViewElement
import com.avito.android.ui.R

class BackgroundDrawableScreen : PageObject() {
    val viewWithBackgroundRedColor: ViewElement = element(withId(R.id.background_view_color))
    val viewWithBackgroundCheckIcon: ViewElement = element(withId(R.id.background_view_image))
}
