package com.avito.android.ui.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.RecyclerAsLayoutActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecyclerAsLayoutTest {

    private val intent = arrayListOf(
        "input",
        "input",
        "edit",
        "edit",
        "label",
        "label"
    )

    @get:Rule
    val rule = screenRule<RecyclerAsLayoutActivity>()

    @Test
    fun label_isAccessible() {
        rule.launchActivity(
            RecyclerAsLayoutActivity.intent(
                intent
            )
        )

        Screen.recyclerAsLayout.list.label().checks.withText("label4")
        Screen.recyclerAsLayout.list.label(4).checks.withText("label4")
        Screen.recyclerAsLayout.list.label(5).checks.withText("label5")
    }

    @Test
    fun editText_isAccessible() {
        rule.launchActivity(
            RecyclerAsLayoutActivity.intent(
                intent
            )
        )
        Screen.recyclerAsLayout.list.edit().checks.withHintText("edit2")
        Screen.recyclerAsLayout.list.edit(2).checks.withHintText("edit2")
        Screen.recyclerAsLayout.list.edit(3).checks.withHintText("edit3")
    }

    @Test
    fun textInput_isAccessible() {
        rule.launchActivity(
            RecyclerAsLayoutActivity.intent(
                intent
            )
        )

        Screen.recyclerAsLayout.list.input().checks.withHintText("input0")
        Screen.recyclerAsLayout.list.input(0).checks.withHintText("input0")
        Screen.recyclerAsLayout.list.input(1).checks.withHintText("input1")
    }
}
