package com.avito.android.ui.test

import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.avito.android.test.InteractionContext
import com.avito.android.test.page_object.ListElement
import com.avito.android.test.page_object.SimpleScreen
import com.avito.android.test.page_object.ViewElement
import com.avito.android.ui.R

class StatefulRecyclerViewAdapterScreen : SimpleScreen() {

    override val rootId: Int = R.id.recycler

    val list: List = element(withId(rootId))

    class List(override val interactionContext: InteractionContext) : ListElement(interactionContext) {

        fun cellWithTitle(title: String): Cell = listElement(hasDescendant(withText(title)))

        fun cellWithTitleCreatedByRecyclerViewInteractionContext(title: String): ViewElement =
            listElement(
                hasDescendant(withText(title))
            )

        class Cell(interactionContext: InteractionContext) : ViewElement(interactionContext) {
            val title: ViewElement = element(withId(R.id.title))
            val title2: ViewElement = element(withId(R.id.title2))
        }
    }
}
