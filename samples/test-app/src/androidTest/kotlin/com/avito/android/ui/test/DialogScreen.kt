package com.avito.android.ui.test

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.avito.android.test.page_object.PageObject
import com.avito.android.test.page_object.ViewElement
import com.avito.android.ui.R

class DialogScreen : PageObject() {
    val root: ViewElement = element(withId(R.id.dialog_root))
}
