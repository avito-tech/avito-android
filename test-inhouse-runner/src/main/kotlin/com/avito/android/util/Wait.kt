package com.avito.android.util

import com.avito.android.test.UITestConfig

/**
 * Данная функция дает возможность объединить группу действий и/или проверок в атомарный блок.
 *
 * Что это значит?
 * Если во время выполнения этого атомарного блока что-то пойдет не так, весь блок будет запущен заного.
 *
 * Кейсы?
 * Например, экран выбора местоположения очищает поле поиска в момент определения местоположения. Данный момент мы не
 * контролируем и не можем его доставерно дождаться.
 * В таком случае мы объединяем все взаимодействие с этим полем в repeatGroup таким образом:
 *
 * waitForSuccess {
 *     searchElement.clearButton.click()
 *     searchElement.field.write(text)
 *     suggestList.checks.isNotEmpty()
 * }
 *
 * В итоге, приход неконтролируемого события приведет к повтору всего блока.
 */
inline fun waitForSuccess(
    triesCount: Int = 3,
    allowedExceptions: Set<Class<out Throwable>> = UITestConfig.waiterAllowedExceptions,
    action: () -> Unit
) {
    var timer = 0
    var caughtAllowedException: Throwable

    do {
        try {
            action.invoke()
            return
        } catch (e: Throwable) {
            val isExceptionAllowed =
                allowedExceptions.find { it.isAssignableFrom(e.javaClass) } != null

            when {
                isExceptionAllowed -> {
                    timer++
                    caughtAllowedException = e
                }
                else -> throw e
            }
        }
    } while (timer < triesCount)

    throw caughtAllowedException
}
