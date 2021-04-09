package com.avito.android.test.report

class StepException(
    val isPrecondition: Boolean,
    val action: String,
    val assertion: String?,
    cause: Throwable?
) : ReporterException(
    message = title(isPrecondition) + "\n" + data(isPrecondition, action, assertion),
    cause = cause
) {

    companion object {
        private fun slug(isPrecondition: Boolean) = if (isPrecondition) "precondition" else "шаг"

        fun title(isPrecondition: Boolean) = "Не удалось выполнить ${slug(isPrecondition)}"

        fun data(isPrecondition: Boolean, action: String, assertion: String?): String {
            return "${slug(isPrecondition).capitalize()}:\n${action.prependIndent()}"
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
