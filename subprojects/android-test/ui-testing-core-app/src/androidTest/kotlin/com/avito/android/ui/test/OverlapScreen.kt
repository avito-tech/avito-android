package com.avito.android.ui.test

import androidx.test.espresso.matcher.ViewMatchers
import com.avito.android.test.page_object.SimpleScreen
import com.avito.android.test.page_object.ViewElement
import com.avito.android.ui.R

class OverlapScreen : SimpleScreen() {

    override val rootId: Int = R.id.root

    val snackButton: ViewElement = element(ViewMatchers.withId(R.id.snack_button))

    val overlappedText: ViewElement = element(ViewMatchers.withId(R.id.overlapped_text))

    val redGroup: ViewElement = element(ViewMatchers.withId(R.id.red_group))

    val greenGroup: ViewElement = element(ViewMatchers.withId(R.id.green_group))
}
