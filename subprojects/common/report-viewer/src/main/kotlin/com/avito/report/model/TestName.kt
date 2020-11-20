package com.avito.report.model

/**
 * Название теста устроено так:
 * [        префикс      ][  юнит  ][фича][        название теста     ]
 * com.avito.android.test.messenger.chat.ReceiveMessageFromBlocked.test
 */
data class TestName(val className: String, val methodName: String) {

    constructor(name: String) : this(name.substringBeforeLast(delimiter), name.substringAfterLast(delimiter))

    val packageName: String = className.substringBeforeLast(delimiter)

    val name = "$className.$methodName"

    override fun toString(): String = name
}

// todo дать возможность указать юнит в аннотации
val TestName.team: Team
    get() = when {
        packageName.startsWith(domofondPrefix) -> Team("domofond")
        packageName.startsWith(avitoPrefix) -> {
            try {
                val unitPrefix = packageName.substringAfter("$avitoPrefix.").substringBefore(delimiter)
                Team(unitPrefix.replace("_", "-"))
            } catch (e: Exception) {
                Team.UNDEFINED
            }
        }
        else -> Team.UNDEFINED
    }

// todo убрать определение features из пакета, будут явно указаны в аннотации к тесту
val TestName.features: List<String>
    get() = when {
        packageName.startsWith(domofondPrefix) -> {
            packageName.substringAfter("$domofondPrefix.")
                .split(delimiter)
        }
        packageName.startsWith(avitoPrefix) -> packageName.substringAfter("$avitoPrefix.")
            .split(delimiter)
            .drop(1) // unit
        else -> emptyList()
    }

private const val delimiter = '.'
private const val avitoPrefix = "com.avito.android.test"
private const val domofondPrefix = "ru.domofond.test"
