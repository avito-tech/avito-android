package androidx.test.core.app

import android.content.Intent

/**
 * TODO: Remove after MBS-11523
 */
fun Intent.hasInstrumentationActivityComponent(): Boolean {
    val instrumentationComponents: List<String> = listOf(
        InstrumentationActivityInvoker.EmptyActivity::class.java.name,
        InstrumentationActivityInvoker.EmptyFloatingActivity::class.java.name,
        InstrumentationActivityInvoker.BootstrapActivity::class.java.name
    )

    return instrumentationComponents.contains(component?.className)
}
