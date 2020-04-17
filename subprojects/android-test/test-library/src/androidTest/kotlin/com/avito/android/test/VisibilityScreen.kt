package com.avito.android.test

import androidx.test.espresso.matcher.ViewMatchers
import com.avito.android.test.page_object.PageObject
import com.avito.android.test.page_object.ViewElement

class VisibilityScreen : PageObject() {
    val label: ViewElement = element(ViewMatchers.withId(R.id.text))
}
