package com.avito.android.test.report.incident

import com.avito.android.test.report.StepException
import com.avito.report.model.IncidentElement
import com.google.common.truth.Correspondence
import com.google.common.truth.Truth.assertThat
import com.google.gson.JsonPrimitive
import org.junit.jupiter.api.Test

internal class IncidentChainTest {

    @Test
    fun `chain - contain three elements - two cause deep`() {
        val rootException = Exception("root", Exception("firstLevelDeep", Exception("secondLevelDeep")))

        val chain = IncidentChainFactory.Impl(setOf(), FallbackIncidentPresenter()).toChain(rootException)

        assertThat(chain)
            .comparingElementsUsing(incidentMessageCorrespondence)
            .containsExactly(
                IncidentElement("root"),
                IncidentElement("firstLevelDeep"),
                IncidentElement("secondLevelDeep")
            )
    }

    @Test
    fun `chain - contains both elements - root custom and cause`() {
        val rootException = AppCrashException(Exception("firstLevelDeep"))

        val chain =
            IncidentChainFactory.Impl(
                setOf(AppCrashIncidentPresenter()),
                FallbackIncidentPresenter()
            ).toChain(rootException)

        assertThat(chain)
            .comparingElementsUsing(incidentMessageCorrespondence)
            .containsExactly(
                IncidentElement("Crash приложения"),
                IncidentElement("firstLevelDeep")
            )
    }

    @Test
    fun `chain - contains both elements - root testcase and cause`() {
        val rootException = StepException(
            isPrecondition = true,
            action = "Нажать на кнопку",
            assertion = "Видно поле",
            cause = Exception("firstLevelDeep")
        )

        val chain =
            IncidentChainFactory.Impl(
                setOf(TestCaseIncidentPresenter()),
                FallbackIncidentPresenter()
            ).toChain(rootException)

        assertThat(chain)
            .comparingElementsUsing(incidentMessageCorrespondence)
            .containsExactly(
                IncidentElement(
                    message = "Не удалось выполнить precondition",
                    data = JsonPrimitive(
                        "Precondition:\n" +
                            "    Нажать на кнопку\n" +
                            "Проверка:\n" +
                            "    Видно поле"
                    )
                ),
                IncidentElement("firstLevelDeep")
            )
    }
}

private val incidentMessageCorrespondence: Correspondence<IncidentElement, IncidentElement> = Correspondence.from(
    { actual, expected ->
        if (actual != null && expected != null) {
            actual.message == expected.message && actual.data == expected.data
        } else false
    },
    "message are equal"
)
