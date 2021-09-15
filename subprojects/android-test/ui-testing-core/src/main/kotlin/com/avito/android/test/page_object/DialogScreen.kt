package com.avito.android.test.page_object

import android.view.View
import androidx.annotation.CallSuper
import com.avito.android.screen.BaseScreenChecks
import com.avito.android.screen.Screen
import com.avito.android.screen.ScreenChecks
import com.avito.android.test.InteractionContext
import com.avito.android.test.context.DialogInteractionContext
import org.hamcrest.Matcher

abstract class DialogScreen(
    matcher: Matcher<View>
) : PageObject(), Screen {

    final override val rootId: Int = Screen.UNKNOWN_ROOT_ID

    override val interactionContext: InteractionContext by lazy {
        DialogInteractionContext(matcher) {
            if (checks.checkOnEachScreenInteraction) {
                checks.isScreenOpened()
            }
        }
    }

    override val checks: ScreenChecks =
        DialogScreenChecks(screen = this, checkOnEachScreenInteraction = true)

    val rootElement = element<ViewElement>()

    open class DialogScreenChecks<T : DialogScreen>(
        screen: T,
        override val checkOnEachScreenInteraction: Boolean = true
    ) : BaseScreenChecks<T>(screen) {

        @CallSuper
        override fun screenOpenedCheck() {
            screen.rootElement.checks.exists()
            screen.rootElement.checks.isDisplayed()
        }
    }
}
