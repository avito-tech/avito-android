package com.avito.android.ui.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.ButtonsOverRecyclerWithCollapsingToolbarActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ButtonsOverRecyclerWithCollapsingToolbarTest {

    @get:Rule
    val rule = screenRule<ButtonsOverRecyclerWithCollapsingToolbarActivity>(launchActivity = true)

    @Test
    fun listElement_elementClicked_whenThereIsOverlappedButtonInScreenWithCollapsingToolbar() {
        Screen.buttonsOverRecycler.list.cellAt(90).click()
    }
}
