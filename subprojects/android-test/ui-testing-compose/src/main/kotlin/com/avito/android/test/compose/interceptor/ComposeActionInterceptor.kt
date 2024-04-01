package com.avito.android.test.compose.interceptor

import android.view.View
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.test.espresso.ViewAction
import com.avito.android.test.compose.action.ComposeAction
import com.avito.android.test.interceptor.ActionInterceptor

public interface ComposeActionInterceptor : ActionInterceptor {

    public fun intercept(action: ComposeAction, node: SemanticsNodeInteraction)

    override fun intercept(action: ViewAction, description: String, view: View) {
        // no-op
    }

    public class Proxy(
        public val source: ComposeAction,
        public val interceptors: List<ActionInterceptor>
    ) : ComposeAction {
        override val name: String = source.name
        override val description: String? = source.description

        override fun perform(view: SemanticsNodeInteraction) {
            interceptors.filterIsInstance<ComposeActionInterceptor>()
                .forEach { it.intercept(source, view) }
            source.perform(view)
        }

        override fun toString(): String = source.toString()
    }
}
