package com.avito.reportviewer

import androidx.annotation.RequiresApi
import com.avito.report.model.Team
import com.github.salomonbrys.kotson.isNotEmpty
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.plusAssign
import java.util.Base64

public class ReportViewerQuery(
    private val base64Encoder: (source: ByteArray) -> String
) {

    /**
     * Result example:
     *
     *```json
     * {"filter":{"error":1,"fail":1,"groups":["messenger"],"other":1}}
     * ```
     */
    public fun createQuery(onlyFailures: Boolean, team: Team): String {
        val query = jsonObject()

        if (onlyFailures) {
            query += "success" to 0
        }
        query += "skip" to 0

        if (team != Team.UNDEFINED && team != Team("domofond")) {
            query += jsonObject("groups" to jsonArray(team.name))
        }

        return if (query.isNotEmpty()) {
            val resultFilter = jsonObject("filter" to query)
            "?q=${base64Encoder(resultFilter.toString().toByteArray())}"
        } else {
            ""
        }
    }

    /**
     * Result example:
     *
     * ```json
     * {"filter":{"search": "com.avito.android.test.Test::click_bottom_button__close_the_screen" }}
     * ```
     */
    public fun createQuery(
        testClass: String,
        testMethod: String
    ): String {
        val query = jsonObject()
        query += "search" to "$testClass::$testMethod"
        val resultFilter = jsonObject("filter" to query)
        return "?q=${base64Encoder(resultFilter.toString().toByteArray())}"
    }

    public companion object {

        @RequiresApi(26)
        public fun createForJvm(): ReportViewerQuery = ReportViewerQuery(
            base64Encoder = Base64.getEncoder()::encodeToString
        )
    }
}
