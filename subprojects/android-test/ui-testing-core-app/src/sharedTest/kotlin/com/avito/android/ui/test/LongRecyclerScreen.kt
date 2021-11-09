package com.avito.android.ui.test

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.avito.android.test.page_object.ListElement
import com.avito.android.test.page_object.SimpleScreen
import com.avito.android.ui.R

class LongRecyclerScreen : SimpleScreen() {

    override val rootId: Int = R.id.recycler

    val list: ListElement = element(withId(rootId))
}
