package com.avito.android.test.report.incident

import com.avito.report.model.IncidentElement

internal interface IncidentChainFactory {

    fun toChain(e: Throwable): List<IncidentElement>

    class Impl(
        private val customViewPresenters: Set<IncidentPresenter>,
        private val fallbackPresenter: IncidentPresenter
    ) : IncidentChainFactory {

        init {
            val genericException = GenericException()
            val badPresenters = customViewPresenters.filter { presenter -> presenter.canCustomize(genericException) }
            require(badPresenters.isEmpty()) {
                "Custom view presenter(s): ${badPresenters.map { it::class.java.name }} handles generic exception. " +
                    "It means incident chain may contain errors, " +
                    "because any custom exception can be handled by these presenters"
            }
        }

        override fun toChain(e: Throwable): List<IncidentElement> = toChainRecursive(listOf(), e)

        private fun toChainRecursive(accumulator: List<IncidentElement>, e: Throwable): List<IncidentElement> {
            return if (e.cause != null) {
                accumulator + e.toChainInternal() + toChainRecursive(accumulator, e.cause!!)
            } else {
                accumulator + e.toChainInternal()
            }
        }

        private fun Throwable.toChainInternal(): List<IncidentElement> {
            val canCustomize = customViewPresenters.filter { presenter -> presenter.canCustomize(this) }
            return when {
                canCustomize.size > 1 -> error(
                    "Multiple presenters ${canCustomize.map { it::class.java.name }} " +
                        "can handle ${this::class.java.name}, " +
                        "than could lead to undesirable behavior. Please narrow canCustomize expressions"
                )
                canCustomize.size == 1 -> canCustomize[0].customize(this).fold(
                    onSuccess = { it },
                    onFailure = { throwable -> customizeByFallback(CustomizeFailException(throwable, this)) }
                )
                else -> customizeByFallback(this)
            }
        }

        private fun customizeByFallback(e: Throwable): List<IncidentElement> {
            return fallbackPresenter.customize(e).fold(
                onSuccess = { it },
                onFailure = { error("Failed to customize by fallback customizer, please fix!") }
            )
        }
    }

    class CustomizeFailException(val failReason: Throwable, originalException: Throwable) :
        Throwable(message = originalException.message, cause = originalException)

    private class GenericException : Throwable(message = "Generic exception")
}
