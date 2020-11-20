package com.avito.android.test.action

import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import com.avito.android.test.UITestConfig
import com.avito.android.test.interceptor.ActionInterceptor
import com.avito.android.test.waitToPerform
import org.hamcrest.Matcher

@Deprecated("use interaction context")
class OnViewActionsDriver(private val matcher: Matcher<View>) : ActionsDriver {

    private val interaction: ViewInteraction
        get() = Espresso.onView(matcher)

    override fun perform(vararg actions: ViewAction) {
        interaction.waitToPerform(
            actions.map { action ->
                ActionInterceptor.Proxy(
                    action,
                    UITestConfig.actionInterceptors
                )
            }
        )
    }
}
