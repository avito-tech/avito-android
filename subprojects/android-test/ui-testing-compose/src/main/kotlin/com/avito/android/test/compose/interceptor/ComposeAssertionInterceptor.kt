package com.avito.android.test.compose.interceptor

import android.view.View
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import com.avito.android.test.compose.assertion.ComposeAssertion
import com.avito.android.test.interceptor.AssertionInterceptor

public interface ComposeAssertionInterceptor : AssertionInterceptor {

    public fun intercept(assertion: ComposeAssertion, node: SemanticsNodeInteraction)

    override fun intercept(assertion: ViewAssertion, view: View?, noViewFoundException: NoMatchingViewException?) {
        // no-op
    }

    public class Proxy(
        public val source: ComposeAssertion,
        public val interceptors: List<AssertionInterceptor>
    ) : ComposeAssertion {
        override val name: String = source.name
        override val description: String? = source.description

        override fun check(view: SemanticsNodeInteraction) {
            interceptors.filterIsInstance<ComposeAssertionInterceptor>()
                .forEach { it.intercept(source, view) }
            source.check(view)
        }

        override fun toString(): String = source.toString()
    }
}
