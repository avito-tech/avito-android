package com.avito.report

import com.avito.report.model.Team
import com.github.salomonbrys.kotson.isNotEmpty
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.plusAssign
import java.util.Base64

class ReportViewerQuery {

    // todo use a universal encoder (android / jvm)
    private val encoder by lazy { Base64.getEncoder() }

    /**
     * Result example:
     *
     *```json
     * {"filter":{"error":1,"fail":1,"groups":["messenger"],"other":1}}
     * ```
     */
    fun createQuery(onlyFailures: Boolean, team: Team): String {
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
            "?q=${encoder.encodeToString(resultFilter.toString().toByteArray())}"
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
    fun createQuery(
        testClass: String,
        testMethod: String
    ): String {
        val query = jsonObject()
        query += "search" to "$testClass::$testMethod"
        val resultFilter = jsonObject("filter" to query)
        return "?q=${encoder.encodeToString(resultFilter.toString().toByteArray())}"
    }
}
