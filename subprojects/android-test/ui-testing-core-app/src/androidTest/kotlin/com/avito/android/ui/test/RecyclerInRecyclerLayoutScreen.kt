package com.avito.android.ui.test

import android.widget.FrameLayout
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.avito.android.test.InteractionContext
import com.avito.android.test.page_object.ListElement
import com.avito.android.test.page_object.SimpleScreen
import com.avito.android.test.page_object.ViewElement
import com.avito.android.ui.R

class RecyclerInRecyclerLayoutScreen : SimpleScreen() {

    override val rootId: Int = R.id.recycler

    val list: List = element(withId(rootId))

    class List(interactionContext: InteractionContext) : ListElement(interactionContext) {

        val horizontalList: InnerList = listElement(withId(R.id.inner_recycler))

        class InnerList(interactionContext: InteractionContext) : ListElement(interactionContext) {

            fun cellWithTitle(
                title: String,
                position: Int? = null
            ): Cell = listElement(
                hasDescendant(withText(title)),
                position
            )

            fun cellAt(position: Int): Cell = listElement(
                ViewMatchers.isAssignableFrom(FrameLayout::class.java),
                position
            )

            class Cell(interactionContext: InteractionContext) : ViewElement(interactionContext) {
                val title: ViewElement = element(withId(R.id.title))
            }
        }
    }
}
