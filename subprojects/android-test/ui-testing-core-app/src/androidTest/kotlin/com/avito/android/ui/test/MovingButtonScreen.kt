package com.avito.android.ui.test

import androidx.test.espresso.matcher.ViewMatchers
import com.avito.android.test.page_object.PageObject
import com.avito.android.test.page_object.ViewElement
import com.avito.android.ui.R

class MovingButtonScreen : PageObject() {

    val movedButton: ViewElement = element(ViewMatchers.withId(R.id.moving_button))

    val movedButtonClickIndicatorView: ViewElement = element(ViewMatchers.withId(R.id.moving_button_clicked_text_view))
}
