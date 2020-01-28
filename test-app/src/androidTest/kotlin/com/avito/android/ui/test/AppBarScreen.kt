package com.avito.android.ui.test

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.avito.android.test.page_object.AppBarElement
import com.avito.android.test.page_object.PageObject
import com.avito.android.test.page_object.ViewElement
import com.avito.android.ui.R

class AppBarScreen : PageObject() {
    val appBar: AppBarElement = element(withId(R.id.appbar))
    val testView: ViewElement = element(withId(R.id.view_in_collapsing_toolbar))
}
