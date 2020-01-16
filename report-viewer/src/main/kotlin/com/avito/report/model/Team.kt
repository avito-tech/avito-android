package com.avito.report.model

import java.io.Serializable

/**
 * Команда, которой принадлежит тест (юнит в Авито, но назван иначе, чтобы не было конфликтов с kotlin.Unit)
 */
inline class Team(val name: String) : Serializable {
    companion object {
        val UNDEFINED = Team("undefined")
    }
}
