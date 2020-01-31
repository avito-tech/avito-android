package com.avito.report.model

/**
 * Наш внутренний маппинг [com.avito.report.internal.model.TestStatus]
 */
sealed class Status {

    object Success : Status() {
        override fun toString(): String = "Success"
    }

    /**
     * @param verdict строка с текстом об ошибке из последней попытки
     */
    data class Failure(val verdict: String, val errorHash: String) : Status() {
        override fun toString(): String = "Failure"
    }

    data class Skipped(val reason: String) : Status() {
        override fun toString(): String = "Skipped"
    }

    object Manual : Status() {
        override fun toString(): String = "Manual"
    }

    object Lost : Status() {
        override fun toString(): String = "Lost"
    }

    /**
     * используется к примеру для определения что нужно перезапускать
     */
    val isSuccessful: Boolean
        get() = when (this) {
            is Success, is Manual, is Skipped -> true
            is Failure, is Lost -> false
        }
}
