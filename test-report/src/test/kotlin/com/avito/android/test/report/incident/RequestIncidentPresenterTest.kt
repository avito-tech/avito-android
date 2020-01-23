package com.avito.android.test.report.incident

import com.google.gson.JsonPrimitive
import org.intellij.lang.annotations.Language
import org.junit.Test

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
    fun dataIsJsonObject() {
        val result = RequestIncidentPresenter().customize(RequestIncidentException("kk", sampleJson, null))

        assert(result is IncidentPresenter.Result.OK)
        val data = (result as IncidentPresenter.Result.OK).chain[0].data
        assert(data !is JsonPrimitive)
    }
}
