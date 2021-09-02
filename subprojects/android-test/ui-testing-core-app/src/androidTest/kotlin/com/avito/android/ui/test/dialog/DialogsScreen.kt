package com.avito.android.ui.test.dialog

import androidx.test.espresso.matcher.ViewMatchers
import com.avito.android.screen.ScreenChecks
import com.avito.android.test.page_object.DialogScreen
import com.avito.android.test.page_object.PopupScreen
import com.avito.android.test.page_object.SimpleScreen
import com.avito.android.test.page_object.ViewElement
import com.avito.android.ui.R

class DialogsScreen : SimpleScreen() {

    override val rootId: Int = R.id.content

    val label: ViewElement = element(ViewMatchers.withId(R.id.label))

    val popup = Popup()

    override val checks: ScreenChecks =
        SimpleScreenChecks(screen = this, checkOnEachScreenInteraction = true)

    class Popup : PopupScreen(
        matcher = ViewMatchers.withId(R.id.popup_content)
    ) {

        val label: ViewElement = element(ViewMatchers.withId(R.id.label))
    }
}

class DialogScreenWithDialogRoot : DialogScreen(
    ViewMatchers.withId(R.id.content)
) {
    val label: ViewElement = element(ViewMatchers.withId(R.id.label))
}

class DialogScreenWithPopupRoot : PopupScreen(
    ViewMatchers.withId(R.id.content)
) {
    val label: ViewElement = element(ViewMatchers.withId(R.id.label))
}
