package com.avito.android.ui.test.dialog

import androidx.test.espresso.matcher.ViewMatchers
import com.avito.android.screen.Screen
import com.avito.android.test.page_object.PageObject
import com.avito.android.test.page_object.ViewElement
import com.avito.android.ui.R

class DialogsScreen : Screen, PageObject() {

    override val rootId: Int = R.id.content

    val content: ViewElement = element(ViewMatchers.withId(rootId))
}
