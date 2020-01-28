package com.avito.android.ui.test

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.avito.android.test.page_object.PageObject
import com.avito.android.test.page_object.TabLayoutElement
import com.avito.android.ui.R

class TabLayoutScreen : PageObject() {
    val tabs: TabLayoutElement = element(withId(R.id.tabs))
}
