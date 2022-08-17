package com.avito.android.ui.test

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.avito.android.test.page_object.SimpleScreen
import com.avito.android.test.page_object.ToolbarElement
import com.avito.android.test.page_object.ViewElement
import com.avito.android.ui.R

class OverflowMenuScreen : SimpleScreen() {

    override val rootId: Int = R.id.root

    val toolbar = Toolbar()

    val label: ViewElement = element(withId(R.id.text))

    class Toolbar : ToolbarElement() {
        val menuItem = actionMenuItem("check")
    }
}
