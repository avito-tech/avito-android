package com.avito.report

import com.avito.report.model.Team
import com.github.salomonbrys.kotson.isNotEmpty
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.plusAssign
import java.util.Base64

class ReportViewerQuery {

    private val encoder = Base64.getEncoder()

    /**
     * пример ответа: {"filter":{"error":1,"fail":1,"groups":["messenger"],"other":1}}
     */
    fun createQuery(onlyFailures: Boolean, team: Team): String {
        val query = jsonObject()

        if (onlyFailures) {
            query += "error" to 1
            query += "fail" to 1
            query += "other" to 1
        }

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
}
