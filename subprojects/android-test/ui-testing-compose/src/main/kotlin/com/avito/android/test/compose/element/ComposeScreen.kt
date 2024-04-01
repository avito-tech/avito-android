package com.avito.android.test.compose.element

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import com.avito.android.screen.Screen
import com.avito.android.test.compose.ComposeInteractionContext
import com.avito.android.test.compose.filter.ComposeFilter
import com.avito.android.test.page_object.SimpleScreen

/**
 * PageObject для Compose экрана.
 */
public abstract class ComposeScreen(
    private val semanticsProvider: SemanticsNodeInteractionsProvider
) : SimpleScreen(), ComposeElement {
    override val rootId: Int = Screen.UNKNOWN_ROOT_ID
    override val interactionContext: ComposeInteractionContext by lazy {
        ComposeInteractionContext(
            super.interactionContext,
            semanticsProvider,
            ComposeFilter.ANY_FILTER
        )
    }
}

/**
 * Создание PageObject'а экрана.
 */
public inline fun <reified R : ComposeScreen> SemanticsNodeInteractionsProvider.screen(): R {
    return R::class.java.getConstructor(
        SemanticsNodeInteractionsProvider::class.java
    ).newInstance(this)
}
