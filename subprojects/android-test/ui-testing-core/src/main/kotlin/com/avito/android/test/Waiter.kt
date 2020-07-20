package com.avito.android.test

import androidx.test.espresso.DataInteraction
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction

/**
 * This method also exposed to the client, for hacking purposes
 */
fun <T> waitFor(
    frequencyMs: Long = UITestConfig.waiterFrequencyMs,
    timeoutMs: Long = UITestConfig.waiterTimeoutMs,
    allowedExceptions: Set<Class<out Any>> = UITestConfig.waiterAllowedExceptions,
    sleepAction: (frequencyMs: Long) -> Unit = { Thread.sleep(it) },
    onWaiterRetry: (e: Throwable) -> Unit = UITestConfig.onWaiterRetry,
    action: () -> T
): T {
    return com.avito.android.waiter.waitFor(
        frequencyMs = frequencyMs,
        timeoutMs = timeoutMs,
        allowedExceptions = allowedExceptions,
        sleepAction = sleepAction,
        onWaiterRetry = onWaiterRetry,
        action = action
    )
}

/**
 * Use this extension instead of ViewInteraction.check() all over the place
 * if you have problems with IdlingResources configuration
 */
fun ViewInteraction.waitForCheck(assertion: ViewAssertion): ViewInteraction =
    waitFor { check(assertion) }

/**
 * Use this extension instead of ViewInteraction.perform() all over the place
 * if you have problems with IdlingResources configuration
 */
fun ViewInteraction.waitToPerform(vararg action: ViewAction): ViewInteraction =
    waitFor { perform(*action) }

/**
 * Use this extension instead of ViewInteraction.perform() all over the place
 * if you have problems with IdlingResources configuration
 */
fun ViewInteraction.waitToPerform(actions: List<ViewAction>): ViewInteraction =
    waitFor { perform(*actions.toTypedArray()) }

/**
 * Use this extension instead of DataInteraction.check() all over the place
 * if you have problems with IdlingResources configuration
 */
fun DataInteraction.waitForCheck(assertion: ViewAssertion): ViewInteraction =
    waitFor { check(assertion) }

/**
 * Use this extension instead of DataInteraction.perform() all over the place
 * if you have problems with IdlingResources configuration
 */
fun DataInteraction.waitToPerform(vararg action: ViewAction): ViewInteraction =
    waitFor { perform(*action) }

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
    allowedExceptions: Set<Class<out Any>> = UITestConfig.waiterAllowedExceptions,
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
