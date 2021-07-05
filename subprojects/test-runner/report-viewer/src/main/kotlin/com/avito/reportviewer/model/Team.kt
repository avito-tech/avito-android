package com.avito.reportviewer.model

import java.io.Serializable

/**
 * Команда, которой принадлежит тест (юнит в Авито, но назван иначе, чтобы не было конфликтов с kotlin.Unit)
 */
public data class Team(val name: String) : Serializable {

    public companion object {
        public val UNDEFINED: Team = Team("undefined")
    }
}
