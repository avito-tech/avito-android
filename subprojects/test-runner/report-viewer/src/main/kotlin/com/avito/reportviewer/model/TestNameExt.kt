package com.avito.reportviewer.model

import com.avito.test.model.TestName
import com.avito.test.model.TestName.Companion.delimiter

/**
 * Название теста устроено так:
 * [        префикс      ][  юнит  ][фича][        название теста     ]
 * com.avito.android.test.messenger.chat.ReceiveMessageFromBlocked.test
 */

// todo дать возможность указать юнит в аннотации
public val TestName.team: Team
    get() = when {
        packageName.startsWith(domofondPrefix) -> Team("domofond")
        packageName.startsWith(avitoPrefix) ->
            try {
                val unitPrefix = packageName.substringAfter("$avitoPrefix.").substringBefore(delimiter)
                Team(unitPrefix.replace("_", "-"))
            } catch (e: Exception) {
                Team.UNDEFINED
            }
        else -> Team.UNDEFINED
    }

private const val avitoPrefix = "com.avito.android.test"
private const val domofondPrefix = "ru.domofond.test"
