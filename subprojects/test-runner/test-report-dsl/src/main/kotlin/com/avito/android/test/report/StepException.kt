package com.avito.android.test.report

import java.util.Locale

public class StepException(
    public val isPrecondition: Boolean,
    public val action: String,
    public val assertion: String?,
    cause: Throwable?
) : RuntimeException(
    title(isPrecondition) + "\n" + data(isPrecondition, action, assertion),
    cause
) {

    public companion object {
        private fun slug(isPrecondition: Boolean) = if (isPrecondition) "precondition" else "шаг"

        public fun title(isPrecondition: Boolean): String = "Не удалось выполнить ${slug(isPrecondition)}"

        public fun data(isPrecondition: Boolean, action: String, assertion: String?): String {
            val capitalizedSlug = slug(isPrecondition).replaceFirstChar {
                if (it.isLowerCase()) {
                    it.titlecase(Locale.getDefault())
                } else {
                    it.toString()
                }
            }
            return "$capitalizedSlug:\n${action.prependIndent()}"
                .let {
                    if (assertion != null) {
                        "$it\nПроверка:\n${assertion.prependIndent()}"
                    } else {
                        it
                    }
                }
        }
    }
}
