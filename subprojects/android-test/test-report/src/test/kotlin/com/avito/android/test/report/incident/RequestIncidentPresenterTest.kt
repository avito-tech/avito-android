package com.avito.android.test.report.incident

import com.avito.truth.ResultSubject.Companion.assertThat
import com.google.common.truth.Truth.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class RequestIncidentPresenterTest {

    @Language("JSON")
    private val sampleJson = """{
  "item": [
    {
      "category_id": 116,
      "count": 1,
      "is_hide_phone": "no",
      "is_phone_only": "no",
      "status": "active"
    }
  ],
  "settings": {
    "avito_host": "https://host.ru",
    "request_id": "android.BuyVAS_BySMSAttached.29.0.158.8638"
  },
  "user": [
    {
      "balance": {
        "bonus": 10000
      },
      "is_phone_verified": "yes",
      "listing_fee": {
        "mode": "never"
      },
      "location_id": 637640,
      "phone": {
        "number": "82001000001",
        "type": "mobile"
      },
      "type": "private"
    }
  ]
}"""

    @Test
    fun `data contains part of a json as is`() {
        val result = RequestIncidentPresenter().customize(
            RequestIncidentException(
                message = "kk",
                body = sampleJson,
                cause = null
            )
        )

        assertThat(result).isSuccess().withValue {
            assertThat(it[0].data).contains(""""avito_host": "https://host.ru"""")
        }
    }
}
