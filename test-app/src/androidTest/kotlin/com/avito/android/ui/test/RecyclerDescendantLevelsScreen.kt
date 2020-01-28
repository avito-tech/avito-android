package com.avito.android.ui.test

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.avito.android.test.InteractionContext
import com.avito.android.test.page_object.ListElement
import com.avito.android.test.page_object.PageObject
import com.avito.android.test.page_object.ViewElement
import com.avito.android.ui.R

class RecyclerDescendantLevelsScreen : PageObject() {

    val list = element<List>(withId(R.id.recycler))

    class List(interactionContext: InteractionContext) : ListElement(interactionContext) {

        val descendantLevelOne: DescendantLevelOne = listElement(withId(R.id.descendant_level_one))

        class DescendantLevelOne(interactionContext: InteractionContext) : ViewElement(interactionContext) {
            val descendantLevelTwo = element<DescendantLevelTwo>(
                withId(R.id.descendant_level_two)
            )
        }

        class DescendantLevelTwo(interactionContext: InteractionContext) : ViewElement(interactionContext) {
            val descendantLevelThree = element<ViewElement>(
                withId(R.id.descendant_level_three)
            )
        }
    }
}
