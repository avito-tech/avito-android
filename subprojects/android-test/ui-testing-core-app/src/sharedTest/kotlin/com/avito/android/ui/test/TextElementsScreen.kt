package com.avito.android.ui.test

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.avito.android.test.page_object.SimpleScreen
import com.avito.android.test.page_object.TextElement
import com.avito.android.ui.R

class TextElementsScreen : SimpleScreen() {

    override val rootId: Int = R.id.text_elements

    val textView: TextElement = element(withId(R.id.text_view))

    val textViewLong: TextElement = element(withId(R.id.text_view_long))
}
