package com.avito.android.ui.test

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.avito.android.test.InteractionContext
import com.avito.android.test.element.field.TextFieldElement
import com.avito.android.test.page_object.ListElement
import com.avito.android.test.page_object.PageObject
import com.avito.android.test.page_object.TextInputElement
import com.avito.android.test.page_object.ViewElement
import com.avito.android.ui.R

class RecyclerAsLayoutScreen : PageObject() {

    val list: List = element(withId(R.id.recycler))

    class List(interactionContext: InteractionContext) : ListElement(interactionContext) {
        fun input(position: Int? = null) = listElement<TextInputElement>(withId(R.id.input_layout), position)
        fun edit(position: Int? = null) = listElement<TextFieldElement>(withId(R.id.edit_text), position)
        fun label(position: Int? = null) = listElement<ViewElement>(withId(R.id.title), position)
    }
}
