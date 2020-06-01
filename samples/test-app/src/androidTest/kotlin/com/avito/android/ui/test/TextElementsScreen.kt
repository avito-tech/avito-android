package com.avito.android.ui.test

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.avito.android.test.page_object.PageObject
import com.avito.android.test.page_object.TextElement
import com.avito.android.ui.R

class TextElementsScreen : PageObject() {
    val textView: TextElement = element(withId(R.id.text_view))
    val textViewLong: TextElement = element(withId(R.id.text_view_long))
}