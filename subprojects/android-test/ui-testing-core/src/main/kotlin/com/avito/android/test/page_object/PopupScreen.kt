package com.avito.android.test.page_object

import android.view.View
import androidx.annotation.CallSuper
import com.avito.android.screen.BaseScreenChecks
import com.avito.android.screen.Screen
import com.avito.android.screen.ScreenChecks
import com.avito.android.test.InteractionContext
import com.avito.android.test.context.PopupInteractionContext
import org.hamcrest.Matcher

abstract class PopupScreen(
    matcher: Matcher<View>
) : PageObject(), Screen {

    final override val rootId: Int = Screen.UNKNOWN_ROOT_ID

    override val interactionContext: InteractionContext by lazy {
        PopupInteractionContext(matcher) {
            if (checks.checkOnEachScreenInteraction) {
                checks.isScreenOpened()
            }
        }
    }

    override val checks: ScreenChecks =
        PopupScreenChecks(screen = this, checkOnEachScreenInteraction = true)

    val rootElement = element<ViewElement>()

    open class PopupScreenChecks(
        screen: PopupScreen,
        override val checkOnEachScreenInteraction: Boolean = true
    ) : BaseScreenChecks<PopupScreen>(screen) {

        @CallSuper
        override fun screenOpenedCheck() {
            screen.rootElement.checks.exists()
            screen.rootElement.checks.isDisplayed()
        }
    }
}
