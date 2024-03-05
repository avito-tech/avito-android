package com.avito.android.test.page_object

import android.view.View
import androidx.test.espresso.action.PrecisionDescriber
import androidx.test.espresso.action.SwipeDirection
import androidx.test.espresso.action.Swiper
import com.avito.android.test.InteractionContext
import com.avito.android.test.SimpleInteractionContext
import com.avito.android.test.action.Actions
import com.avito.android.test.action.ActionsImpl
import com.avito.android.test.checks.Checks
import com.avito.android.test.checks.ChecksImpl
import com.avito.android.test.matcher.NoViewMatcher
import org.hamcrest.Matcher

public open class ViewElement private constructor(
    override val interactionContext: InteractionContext,
    override val matcher: Matcher<View>,
    override val actions: Actions,
    override val checks: Checks
) : PageObjectElement(), Actions {

    /**
     * "Use constructor with interaction context. " +
     * "Otherwise, you can get actions and checks in a wrong screen or window." +
     * "See [PageObject.element] to create an instance with correct interaction context inside page objects." +
     * "For a custom class use `ViewElement(interactionContext)`"
     * TODO: make this constructor private and remove matcher
     */
    public constructor(
        matcher: Matcher<View>,
        interactionContext: InteractionContext = SimpleInteractionContext(matcher),
        actions: Actions = ActionsImpl(interactionContext),
        checks: Checks = ChecksImpl(interactionContext)
    ) : this(interactionContext, matcher, actions, checks)

    @Suppress("DEPRECATION")
    public constructor(interactionContext: InteractionContext) :
        this(
            NoViewMatcher(),
            interactionContext,
            ActionsImpl(interactionContext),
            ChecksImpl(interactionContext)
        )

    @Suppress("DEPRECATION")
    public constructor(interactionContext: InteractionContext, checks: Checks) :
        this(NoViewMatcher(), interactionContext, ActionsImpl(interactionContext), checks)

    override fun click(): Unit = actions.click()

    override fun longClick(): Unit = actions.longClick()

    override fun scrollTo(): Unit = actions.scrollTo()

    override fun swipe(direction: SwipeDirection, speed: Swiper, precision: PrecisionDescriber): Unit =
        actions.swipe(direction, speed, precision)

    override fun read(allowBlank: Boolean): String = actions.read(allowBlank)
}
