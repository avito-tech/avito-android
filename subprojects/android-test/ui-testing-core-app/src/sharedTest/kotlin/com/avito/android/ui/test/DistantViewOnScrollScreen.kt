package com.avito.android.ui.test

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.avito.android.test.page_object.SimpleScreen
import com.avito.android.test.page_object.ViewElement
import com.avito.android.ui.R

class DistantViewOnScrollScreen : SimpleScreen() {

    override val rootId: Int = R.id.scroll

    val scroll: ViewElement = rootElement

    val view: ViewElement = element(withId(R.id.view))
}
