package com.avito.android.ui

sealed class SyntheticConstructorCase {

    object One : SyntheticConstructorCase()

    class Two(
        val code: Int = -1,
        val reason: String? = null,
        val error: Throwable? = null
    ) : SyntheticConstructorCase() {

        override fun toString(): String = "Two{code=$code, reason=\"$reason\", error=\"${error?.message}}\""
    }
}
