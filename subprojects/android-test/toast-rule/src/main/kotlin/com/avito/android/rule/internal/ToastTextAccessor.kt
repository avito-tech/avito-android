package com.avito.android.rule.internal

import android.os.Build
import android.widget.TextView
import android.widget.Toast
import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.test.util.HiddenApiOpener

/**
 * Reads text from a Toast.
 * See API-specific implementations.
 */
internal interface ToastTextAccessor {

    /**
     * Reads text from Toast created by [Toast.makeText].
     * Returns null in case of custom view
     */
    fun text(toast: Toast): String?

    companion object {

        fun create(): ToastTextAccessor {
            val inAppToasts = Build.VERSION.SDK_INT < 30 ||
                InstrumentationRegistry.getInstrumentation().targetContext.applicationInfo.targetSdkVersion < 30

            return if (inAppToasts) {
                InAppToastTextAccessor()
            } else {
                SystemUIToastTextAccessor()
            }
        }
    }
}

/**
 * Before Android 11 toasts were shown inside application.
 * [Toast.makeText] inflated View and assigned it as [Toast.setView]
 *
 * See https://developer.android.com/about/versions/11/behavior-changes-11#toasts
 */
internal class InAppToastTextAccessor : ToastTextAccessor {

    override fun text(toast: Toast): String? {
        @Suppress("DEPRECATION")
        val view = requireNotNull(toast.view) {
            "Toast.view must be present in $toast"
        }
        val defaultTextView = view.findViewById<TextView>(android.R.id.message)
        return defaultTextView?.text?.toString()
    }
}

/**
 * Starting from Android 11 toasts are shown in system service.
 * [Toast.makeText] doesn't inflate View and stores only text.
 *
 * See https://developer.android.com/about/versions/11/behavior-changes-11#toasts
 */
internal class SystemUIToastTextAccessor : ToastTextAccessor {

    override fun text(toast: Toast): String? {
        val textField = toast.getFieldByReflection("mText") as CharSequence?
        return textField?.toString()
    }
}

internal fun Any.getFieldByReflection(fieldName: String): Any? {
    HiddenApiOpener.ensureUnseal()

    val clazz = this::class.java
    val declaredField = clazz.getDeclaredField(fieldName)
    requireNotNull(declaredField) {
        "Expected to find field $fieldName in $clazz. " +
            "Declared fields: ${clazz.declaredFields}." +
            "Probable reason: unsupported API version."
    }
    return declaredField
        .also { it.isAccessible = true }
        .get(this)
}
