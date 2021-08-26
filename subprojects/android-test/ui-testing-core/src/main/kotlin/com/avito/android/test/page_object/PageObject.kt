package com.avito.android.test.page_object

import android.view.View
import androidx.test.espresso.matcher.ViewMatchers
import com.avito.android.screen.Screen
import com.avito.android.test.InteractionContext
import com.avito.android.test.SimpleInteractionContext
import org.hamcrest.Matcher

abstract class PageObject {

    // TODO: remove default implementation after migrating clients to specific implementations in MBS-11808
    open val interactionContext: InteractionContext by lazy {
        if (this is Screen) {
            SimpleInteractionContext(ViewMatchers.isRoot()) {
                if (checks.checkOnEachScreenInteraction) {
                    checks.isScreenOpened()
                }
            }
        } else {
            SimpleInteractionContext(ViewMatchers.isRoot())
        }
    }

    // TODO: Matcher<View> - слишком общий, нужно сузить до конкретного типа или по аналогии с kakao
    protected inline fun <reified T> PageObject.element(matcher: Matcher<View>): T {
        val handleOriginalContextItself = HandleParentContext::class.java.isAssignableFrom(T::class.java)

        return if (handleOriginalContextItself) {
            T::class.java.getConstructor(InteractionContext::class.java, Matcher::class.java)
                .newInstance(this.interactionContext, matcher)
        } else {
            T::class.java.getConstructor(InteractionContext::class.java)
                .newInstance(this.interactionContext.provideChildContext(matcher))
        }
    }

    protected inline fun <reified T> PageObject.element(): T {
        val constructor = T::class.java.getConstructor(InteractionContext::class.java)
        return constructor.newInstance(this.interactionContext)
    }
}

/**
 * Marker interface
 *
 * It means that element wants to be created with an original parent interaction context.
 * PageObject.element(<matcher from user>)
 *   -> constructor(<parent interaction context>, <matcher from user>)
 */
interface HandleParentContext
