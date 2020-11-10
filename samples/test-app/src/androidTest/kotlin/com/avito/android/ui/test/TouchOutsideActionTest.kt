package com.avito.android.ui.test

import com.avito.android.test.app.core.screenRule
import com.avito.android.test.espresso.action.TouchOutsideAction
import com.avito.android.ui.DialogActivity
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test

class TouchOutsideActionTest {

    @get:Rule
    val rule = screenRule<DialogActivity>(launchActivity = true)


    @Test
    fun outsideClicked_activityFinished() {
        val activity = rule.activity
        Screen.dialogScreen.root.interactionContext.perform(TouchOutsideAction())
        assertTrue(activity.isFinishing)
    }
}
