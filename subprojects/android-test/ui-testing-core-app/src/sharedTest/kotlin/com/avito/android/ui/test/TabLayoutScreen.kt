package com.avito.android.ui.test

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.avito.android.test.page_object.SimpleScreen
import com.avito.android.test.page_object.TabLayoutElement
import com.avito.android.ui.R

class TabLayoutScreen : SimpleScreen() {

    override val rootId: Int = R.id.tabs_root

    val tabs: TabLayoutElement = element(withId(R.id.tabs))
}
