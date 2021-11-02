package com.avito.android.ui.test

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.avito.android.test.page_object.ListElement
import com.avito.android.test.page_object.SimpleScreen
import com.avito.android.test.page_object.SwipeRefreshElement
import com.avito.android.ui.R

class SwipeRefreshScreen : SimpleScreen() {

    override val rootId: Int = R.id.swipe_refresh_root

    val list: ListElement = element(withId(R.id.swipe_refresh))

    val swipeRefreshElement: SwipeRefreshElement = element(withId(R.id.swipe_refresh))
}
