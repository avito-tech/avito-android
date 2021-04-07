package com.avito.android.test.report

import com.avito.api.resourcemanager.ResourceManagerException
import com.avito.report.model.Entry
import com.avito.report.model.Incident
import com.avito.report.model.IncidentElement
import com.avito.time.TimeMachineProvider
import com.avito.truth.ExtendedIterableSubject.Companion.assertIterable
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.concurrent.TimeUnit

@Suppress("MaxLineLength")
class ReportTest {

    private val timeMachine = TimeMachineProvider()

    @JvmField
    @RegisterExtension
    val report = ReportTestExtension(
        timeProvider = timeMachine
    )

    @BeforeEach
    fun before() {
        report.initTestCaseHelper()
        report.startTestCase()
    }

    @Test
    fun `report incident - step chain`() {
        report.registerIncident(
            StepException(
                false,
                "Тапнуть по кнопка",
                null,
                AssertionError("No views in hierarchy found matching: with id: com.avito.android.dev:id/variant_list")
            ),
            null
        )

        val reportPackage = report.reportTestCase()

        assertThat(reportPackage.incident).isNotNull()
        assertThat(reportPackage.incident!!.type).isEqualTo(Incident.Type.ASSERTION_FAILED)
        assertThat(reportPackage.incident!!.chain.size).isEqualTo(2)
        assertThat(reportPackage.incident!!.chain[0].message).isEqualTo("Не удалось выполнить шаг")
        assertThat(reportPackage.incident!!.chain[1].message).isEqualTo("No views in hierarchy found matching: with id: com.avito.android.dev:id/variant_list")
    }

    @Test
    fun `report incident - resourceManager chain`() {
        val gson = Gson()
        val chain = listOf(
            IncidentElement(
                message = "Получен неожиданный ответ от 'service/pricing-adm', из-за этого: Не удалось установить скидку пользователю.\n" +
                    "Детали ошибки: В ответе от https://host.ru/some/thing получен HTTP код 400",
                code = 500,
                type = "external",
                origin = "resource-manager",
                data = gson.fromJson(
                    """{"request":{"url":"https://host.ru/some/thing","method":"POST","headers":{"Accept":"application/json","Content-Type":"application/json"}},"result":{"header":"HTTP/1.1 400 Bad Request\r\nServer: nginx\r\nDate: Sun, 19 May 2019 13:36:59 GMT\r\nContent-Type: application/json\r\nContent-Length: 75\r\nConnection: keep-alive\r\nKeep-Alive: timeout=75\r\nCache-Control: no-store, no-cache, must-revalidate, post-check=0, pre-check=0\r\nAllow: POST, OPTIONS\r\nX-Frame-Options: SAMEORIGIN\r\nVary: Origin\r\nX-XSS-Protection: 1; mode=block\r\nX-Content-Type-Options: nosniff\r\n\r\n","effective_url":"https://host.ru/some/thing","body": {"error":{"code":400,"message":"'turbo' is not one of ['fix', 'melting']"}}}}""",
                    JsonObject::class.java
                )
            ),
            IncidentElement(
                message = "Не удалось установить скидку пользователю.",
                code = 400,
                type = "internal",
                origin = "test-stand",
                className = "Avito\\QA\\DetailedException\\TestStandException"
            ),
            IncidentElement(
                message = "В ответе от https://host.ru/some/thing получен HTTP код 400",
                code = 400,
                type = "external",
                origin = "service/pricing-adm",
                className = "Avito\\QA\\DetailedException\\ExternalServiceException",
                data = gson.fromJson(
                    """{"request":{"url":"https://host.ru/some/thing","method":"POST","headers":{"Accept":"application/json","Content-Type":"application/json"}},"result":{"header":"HTTP/1.1 400 Bad Request\r\nServer: nginx\r\nDate: Sun, 19 May 2019 13:36:59 GMT\r\nContent-Type: application/json\r\nContent-Length: 75\r\nConnection: keep-alive\r\nKeep-Alive: timeout=75\r\nCache-Control: no-store, no-cache, must-revalidate, post-check=0, pre-check=0\r\nAllow: POST, OPTIONS\r\nX-Frame-Options: SAMEORIGIN\r\nVary: Origin\r\nX-XSS-Protection: 1; mode=block\r\nX-Content-Type-Options: nosniff\r\n\r\n","effective_url":"https://host.ru/some/thing","body": {"error":{"code":400,"message":"'turbo' is not one of ['fix', 'melting']"}}}}""",
                    JsonObject::class.java
                )
            )
        )

        report.registerIncident(
            exception = ResourceManagerException(
                message = "Resource manager error: http://host.ru (https://host.ru/some/thing)",
                cause = null,
                requestUrl = "http://host.ru",
                requestBody = """{ "pls": true }""",
                responseBody = null,
                incidentChain = chain
            ),
            screenshot = null
        )

        val reportPackage = report.reportTestCase()

        assertThat(reportPackage.incident).isNotNull()
        assertThat(reportPackage.incident!!.type).isEqualTo(Incident.Type.INFRASTRUCTURE_ERROR)
        assertThat(reportPackage.incident!!.chain.size).isEqualTo(4)
        assertThat(reportPackage.incident!!.chain[0].message).isEqualTo("Ошибка при обращении к http://host.ru")
        assertThat(reportPackage.incident!!.chain[0].data!!.toString()).isEqualTo(""""{ \"pls\": true }"""")
        assertThat(reportPackage.incident!!.chain[1].message).isEqualTo(
            "Получен неожиданный ответ от 'service/pricing-adm', из-за этого: Не удалось установить скидку пользователю.\n" +
                "Детали ошибки: В ответе от https://host.ru/some/thing получен HTTP код 400"
        )
        assertThat(reportPackage.incident!!.chain[1].data!!.toString()).isEqualTo("""{"request":{"url":"https://host.ru/some/thing","method":"POST","headers":{"Accept":"application/json","Content-Type":"application/json"}},"result":{"header":"HTTP/1.1 400 Bad Request\r\nServer: nginx\r\nDate: Sun, 19 May 2019 13:36:59 GMT\r\nContent-Type: application/json\r\nContent-Length: 75\r\nConnection: keep-alive\r\nKeep-Alive: timeout=75\r\nCache-Control: no-store, no-cache, must-revalidate, post-check=0, pre-check=0\r\nAllow: POST, OPTIONS\r\nX-Frame-Options: SAMEORIGIN\r\nVary: Origin\r\nX-XSS-Protection: 1; mode=block\r\nX-Content-Type-Options: nosniff\r\n\r\n","effective_url":"https://host.ru/some/thing","body":{"error":{"code":400,"message":"'turbo' is not one of ['fix', 'melting']"}}}}""")
        assertThat(reportPackage.incident!!.chain[2].message).isEqualTo("Не удалось установить скидку пользователю.")
        assertThat(reportPackage.incident!!.chain[2].data).isNull()
        assertThat(reportPackage.incident!!.chain[3].message).isEqualTo("В ответе от https://host.ru/some/thing получен HTTP код 400")
        assertThat(reportPackage.incident!!.chain[3].data!!.toString()).isEqualTo("""{"request":{"url":"https://host.ru/some/thing","method":"POST","headers":{"Accept":"application/json","Content-Type":"application/json"}},"result":{"header":"HTTP/1.1 400 Bad Request\r\nServer: nginx\r\nDate: Sun, 19 May 2019 13:36:59 GMT\r\nContent-Type: application/json\r\nContent-Length: 75\r\nConnection: keep-alive\r\nKeep-Alive: timeout=75\r\nCache-Control: no-store, no-cache, must-revalidate, post-check=0, pre-check=0\r\nAllow: POST, OPTIONS\r\nX-Frame-Options: SAMEORIGIN\r\nVary: Origin\r\nX-XSS-Protection: 1; mode=block\r\nX-Content-Type-Options: nosniff\r\n\r\n","effective_url":"https://host.ru/some/thing","body":{"error":{"code":400,"message":"'turbo' is not one of ['fix', 'melting']"}}}}""")
    }

    @Test
    fun `test assertion reported - no incident`() {
        step("step description", report, false) {
            assertion("assertion message") {}
        }

        val state = report.reportTestCase()

        assertThat(state.testCaseStepList)
            .hasSize(1)
        assertIterable(state.testCaseStepList[0].entryList)
            .containsExactlyOne(Entry.Check::class.java) {
                it.type == "check" && it.title == "assertion message"
            }
    }

    @Test
    fun `test assertion reported with multiple preconditions - no incident`() {
        precondition("first precondition", report, false) {
            assertion("first precondition assertion") {}
        }
        precondition("second precondition", report, false) {
            assertion("second precondition assertion") {}
        }
        step("step description", report, false) {
            assertion("assertion message") {}
        }

        val state = report.reportTestCase()

        assertThat(state.testCaseStepList)
            .hasSize(1)

        assertThat(state.testCaseStepList[0].number)
            .isEqualTo(0)
        assertIterable(state.testCaseStepList[0].entryList)
            .containsExactlyOne(Entry.Check::class.java) {
                it.type == "check" && it.title == "assertion message"
            }

        assertThat(state.preconditionStepList)
            .hasSize(2)

        assertThat(state.preconditionStepList[0].number)
            .isEqualTo(0)
        assertIterable(state.preconditionStepList[0].entryList)
            .containsExactlyOne(Entry.Check::class.java) {
                it.type == "check" && it.title == "first precondition assertion"
            }

        assertThat(state.preconditionStepList[1].number)
            .isEqualTo(1)
        assertIterable(state.preconditionStepList[1].entryList)
            .containsExactlyOne(Entry.Check::class.java) {
                it.type == "check" && it.title == "second precondition assertion"
            }
    }

    @Test
    fun `duplicate entries should be merged - only in consequent groups`() {
        step("Test step", report, false) {
            repeat(2) {
                report.addComment(
                    "performing ViewAction: Perform action single click on descendant view has child: (with text: is \"Адрес\" or with text: is \"Адрес компании\" or with text: is \"Место осмотра\" or with text: is \"Желаемый район\" or with text: is \"Место сделки\" or with text: is \"Место проживания\" or with text: is \"Место работы\" or with text: is \"Место оказания услуг\") on 0-th item matching: holder with view: (has descendant: has child: (with text: is \"Адрес\" or with text: is \"Адрес компании\" or with text: is \"Место осмотра\" or with text: is \"Желаемый район\" or with text: is \"Место сделки\" or with text: is \"Место проживания\" or with text: is \"Место работы\" or with text: is \"Место оказания услуг\") or has child: (with text: is \"Адрес\" or with text: is \"Адрес компании\" or with text: is \"Место осмотра\" or with text: is \"Желаемый район\" or with text: is \"Место сделки\" or with text: is \"Место проживания\" or with text: is \"Место работы\" or with text: is \"Место оказания услуг\")) on RecyclerView(id=recycler_view)"
                )
            }

            report.addComment("its fine")

            repeat(3) {
                report.addComment(
                    "performing ViewAction: Perform action single click on descendant view has child: (with text: is \"Адрес\" or with text: is \"Адрес компании\" or with text: is \"Место осмотра\" or with text: is \"Желаемый район\" or with text: is \"Место сделки\" or with text: is \"Место проживания\" or with text: is \"Место работы\" or with text: is \"Место оказания услуг\") on 0-th item matching: holder with view: (has descendant: has child: (with text: is \"Адрес\" or with text: is \"Адрес компании\" or with text: is \"Место осмотра\" or with text: is \"Желаемый район\" or with text: is \"Место сделки\" or with text: is \"Место проживания\" or with text: is \"Место работы\" or with text: is \"Место оказания услуг\") or has child: (with text: is \"Адрес\" or with text: is \"Адрес компании\" or with text: is \"Место осмотра\" or with text: is \"Желаемый район\" or with text: is \"Место сделки\" or with text: is \"Место проживания\" or with text: is \"Место работы\" or with text: is \"Место оказания услуг\")) on RecyclerView(id=recycler_view)"
                )
            }
        }

        val state = report.reportTestCase()
        val stepEntries = state.testCaseStepList.first().entryList

        assertThat(stepEntries).hasSize(4)

        stepEntries[0].assertImage("stub")
        stepEntries[1].assertCommentIs(
            "[x2] performing ViewAction: Perform action single click on descendant view has child: (with text: is \"Адрес\" or with text: is \"Адрес компании\" or with text: is \"Место осмотра\" or with text: is \"Желаемый район\" or with text: is \"Место сделки\" or with text: is \"Место проживания\" or with text: is \"Место работы\" or with text: is \"Место оказания услуг\") on 0-th item matching: holder with view: (has descendant: has child: (with text: is \"Адрес\" or with text: is \"Адрес компании\" or with text: is \"Место осмотра\" or with text: is \"Желаемый район\" or with text: is \"Место сделки\" or with text: is \"Место проживания\" or with text: is \"Место работы\" or with text: is \"Место оказания услуг\") or has child: (with text: is \"Адрес\" or with text: is \"Адрес компании\" or with text: is \"Место осмотра\" or with text: is \"Желаемый район\" or with text: is \"Место сделки\" or with text: is \"Место проживания\" or with text: is \"Место работы\" or with text: is \"Место оказания услуг\")) on RecyclerView(id=recycler_view)"
        )
        stepEntries[2].assertCommentIs("its fine")
        stepEntries[3].assertCommentIs(
            "[x3] performing ViewAction: Perform action single click on descendant view has child: (with text: is \"Адрес\" or with text: is \"Адрес компании\" or with text: is \"Место осмотра\" or with text: is \"Желаемый район\" or with text: is \"Место сделки\" or with text: is \"Место проживания\" or with text: is \"Место работы\" or with text: is \"Место оказания услуг\") on 0-th item matching: holder with view: (has descendant: has child: (with text: is \"Адрес\" or with text: is \"Адрес компании\" or with text: is \"Место осмотра\" or with text: is \"Желаемый район\" or with text: is \"Место сделки\" or with text: is \"Место проживания\" or with text: is \"Место работы\" or with text: is \"Место оказания услуг\") or has child: (with text: is \"Адрес\" or with text: is \"Адрес компании\" or with text: is \"Место осмотра\" or with text: is \"Желаемый район\" or with text: is \"Место сделки\" or with text: is \"Место проживания\" or with text: is \"Место работы\" or with text: is \"Место оказания услуг\")) on RecyclerView(id=recycler_view)"
        )
    }

    @Test
    fun `duplicate entries should be merged - consequent entries with different timestamps`() {
        step("Test step", report, false) {
            report.addComment(
                "performing ViewAction: Perform action single click on descendant view has child: (with text: is \"Адрес\" or with text: is \"Адрес компании\" or with text: is \"Место осмотра\" or with text: is \"Желаемый район\" or with text: is \"Место сделки\" or with text: is \"Место проживания\" or with text: is \"Место работы\" or with text: is \"Место оказания услуг\") on 0-th item matching: holder with view: (has descendant: has child: (with text: is \"Адрес\" or with text: is \"Адрес компании\" or with text: is \"Место осмотра\" or with text: is \"Желаемый район\" or with text: is \"Место сделки\" or with text: is \"Место проживания\" or with text: is \"Место работы\" or with text: is \"Место оказания услуг\") or has child: (with text: is \"Адрес\" or with text: is \"Адрес компании\" or with text: is \"Место осмотра\" or with text: is \"Желаемый район\" or with text: is \"Место сделки\" or with text: is \"Место проживания\" or with text: is \"Место работы\" or with text: is \"Место оказания услуг\")) on RecyclerView(id=recycler_view)"
            )
            timeMachine.moveForwardOn(1, TimeUnit.SECONDS)
            repeat(2) {
                report.addComment(
                    "performing ViewAction: Perform action single click on descendant view has child: (with text: is \"Адрес\" or with text: is \"Адрес компании\" or with text: is \"Место осмотра\" or with text: is \"Желаемый район\" or with text: is \"Место сделки\" or with text: is \"Место проживания\" or with text: is \"Место работы\" or with text: is \"Место оказания услуг\") on 0-th item matching: holder with view: (has descendant: has child: (with text: is \"Адрес\" or with text: is \"Адрес компании\" or with text: is \"Место осмотра\" or with text: is \"Желаемый район\" or with text: is \"Место сделки\" or with text: is \"Место проживания\" or with text: is \"Место работы\" or with text: is \"Место оказания услуг\") or has child: (with text: is \"Адрес\" or with text: is \"Адрес компании\" or with text: is \"Место осмотра\" or with text: is \"Желаемый район\" or with text: is \"Место сделки\" or with text: is \"Место проживания\" or with text: is \"Место работы\" or with text: is \"Место оказания услуг\")) on RecyclerView(id=recycler_view)"
                )
            }
            timeMachine.moveForwardOn(1, TimeUnit.SECONDS)
            report.addComment(
                "performing ViewAction: Perform action single click on descendant view has child: (with text: is \"Адрес\" or with text: is \"Адрес компании\" or with text: is \"Место осмотра\" or with text: is \"Желаемый район\" or with text: is \"Место сделки\" or with text: is \"Место проживания\" or with text: is \"Место работы\" or with text: is \"Место оказания услуг\") on 0-th item matching: holder with view: (has descendant: has child: (with text: is \"Адрес\" or with text: is \"Адрес компании\" or with text: is \"Место осмотра\" or with text: is \"Желаемый район\" or with text: is \"Место сделки\" or with text: is \"Место проживания\" or with text: is \"Место работы\" or with text: is \"Место оказания услуг\") or has child: (with text: is \"Адрес\" or with text: is \"Адрес компании\" or with text: is \"Место осмотра\" or with text: is \"Желаемый район\" or with text: is \"Место сделки\" or with text: is \"Место проживания\" or with text: is \"Место работы\" or with text: is \"Место оказания услуг\")) on RecyclerView(id=recycler_view)"
            )
        }

        val state = report.reportTestCase()
        val stepEntries = state.testCaseStepList.first().entryList

        assertThat(stepEntries).hasSize(2)
        stepEntries[0].assertImage("stub")
        stepEntries[1].assertCommentIs(
            "[x4] performing ViewAction: Perform action single click on descendant view has child: (with text: is \"Адрес\" or with text: is \"Адрес компании\" or with text: is \"Место осмотра\" or with text: is \"Желаемый район\" or with text: is \"Место сделки\" or with text: is \"Место проживания\" or with text: is \"Место работы\" or with text: is \"Место оказания услуг\") on 0-th item matching: holder with view: (has descendant: has child: (with text: is \"Адрес\" or with text: is \"Адрес компании\" or with text: is \"Место осмотра\" or with text: is \"Желаемый район\" or with text: is \"Место сделки\" or with text: is \"Место проживания\" or with text: is \"Место работы\" or with text: is \"Место оказания услуг\") or has child: (with text: is \"Адрес\" or with text: is \"Адрес компании\" or with text: is \"Место осмотра\" or with text: is \"Желаемый район\" or with text: is \"Место сделки\" or with text: is \"Место проживания\" or with text: is \"Место работы\" or with text: is \"Место оказания услуг\")) on RecyclerView(id=recycler_view)"
        )
    }

    private fun Entry.assertImage(comment: String) {
        assertThat(type).isEqualTo("img_png")
        assertThat((this as Entry.File).comment).isEqualTo(comment)
    }

    private fun Entry.assertCommentIs(comment: String) {
        assertThat(type).isEqualTo("comment")
        assertThat((this as Entry.Comment).title).isEqualTo(comment)
    }
}
