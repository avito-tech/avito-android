package androidx.test.core.app

import android.content.Intent

fun Intent.hasInstrumentationActivityComponent(): Boolean {
    val instrumentationComponents: List<String> = listOf(
        InstrumentationActivityInvoker.EmptyActivity::class.java,
        InstrumentationActivityInvoker.EmptyFloatingActivity::class.java,
        InstrumentationActivityInvoker.BootstrapActivity::class.java
    ).map { it.name }

    return instrumentationComponents.contains(component?.className)
}
