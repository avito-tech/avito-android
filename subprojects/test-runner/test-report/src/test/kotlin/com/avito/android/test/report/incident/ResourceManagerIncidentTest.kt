package com.avito.android.test.report.incident

import com.avito.api.resourcemanager.ResourceManagerException
import com.avito.report.model.IncidentElement
import com.avito.truth.ResultSubject.Companion.assertThat
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class ResourceManagerIncidentTest {

    private val presenter = ResourceManagerIncidentPresenter()

    private val exceptionWithIncedentChain by lazy {
        ResourceManagerException(
            message = "Resource manager error: http://host.ru (https://host.ru/something)",
            cause = null,
            requestUrl = "http://host.ru",
            requestBody = "{\"pls\": true}",
            responseBody = null,
            incidentChain = listOf(
                IncidentElement("1"),
                IncidentElement("2"),
                IncidentElement("3")
            )
        )
    }

    @Test
    fun success_parsing() {
        val exception = ResourceManagerException(
            message = "Error message",
            cause = null,
            requestUrl = "http://host.ru",
            requestBody = "",
            responseBody = null,
            incidentChain = emptyList()
        )
        val result = presenter.customize(exception)

        assertThat(result).isSuccess()
    }

    @Test
    fun `incedent chain must be after main incedent`() {
        val result = presenter.customize(exceptionWithIncedentChain)

        assertThat(result).isSuccess().withValue { chain ->
            assertThat(chain.size)
                .isEqualTo(4)

            assertThat(chain[0].message)
                .isEqualTo("Ошибка при обращении к http://host.ru")

            assertThat(chain.subList(1, 4))
                .containsExactlyElementsIn(exceptionWithIncedentChain.incidentChain)
        }
    }

    @Test
    fun `can customize - true for sample`() {
        assertThat(presenter.canCustomize(exceptionWithIncedentChain)).isTrue()
    }

    @Test
    fun `can customize - false for different exception`() {
        assertThat(presenter.canCustomize(Exception("Something went wrong"))).isFalse()
    }
}
