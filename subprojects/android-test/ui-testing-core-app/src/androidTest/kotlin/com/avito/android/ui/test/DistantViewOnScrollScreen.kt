package com.avito.android.ui.test

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.avito.android.test.page_object.PageObject
import com.avito.android.test.page_object.ViewElement
import com.avito.android.ui.R

class DistantViewOnScrollScreen : PageObject() {

    val scroll: ViewElement = element(withId(R.id.scroll))

    val view: ViewElement = element(withId(R.id.view))
}
