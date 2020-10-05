package com.avito.test.summary.analysis

import com.avito.report.model.CrossDeviceSuite
import com.avito.report.model.FailureOnDevice
import com.avito.report.model.HasFailures

internal fun CrossDeviceSuite.analyzeFailures(): Map<String, List<FailureOnDevice>> {
    return this.crossDeviceRuns
        .filter { it.status is HasFailures }
        .flatMap { (it.status as HasFailures).failures }
        .map { it.normalized() }
        .groupBy { it.failureMessage }
}

//visible for testing
internal fun normalize(failureMessage: String): String {
    return DEFAULT_FAILURE_MESSAGE_NORMALIZERS
        .fold(failureMessage, { message, normalizer -> normalizer.normalize(message) })
}

private fun FailureOnDevice.normalized(): FailureOnDevice {
    val normalizedMessage = normalize(this.failureMessage)
    return FailureOnDevice(this.device, normalizedMessage)
}

private val DEFAULT_FAILURE_MESSAGE_NORMALIZERS = listOf(
    DuplicateFailureMessageNormalizer,
    RegexFailureMessageNormalizer(Regex("@[0-9a-f]{4,}")),
    RegexFailureMessageNormalizer(Regex("Не удалось выполнить шаг")),
    RegexFailureMessageNormalizer(Regex("height=[0-9]{1,4}")),
    RegexFailureMessageNormalizer(Regex("\n"), " "),
    RegexToPatternMessageNormalizer(
        Regex("Expected: with text: is \"(.+)\"\\s+Got: \".+res-name=(.*?), .+text=(.*?),.+\""),
        "Во view: \"{2}\"\nожидался текст: \"{1}\"\nполучили: \"{3}\""
    ),
    RegexToPatternMessageNormalizer(
        Regex("([a-zA-Z].*?)\\{.+res-name=(.*?),.+}"),
        "{1}(R.id.{2})"
    ),
    RegexToPatternMessageNormalizer(
        Regex("No views in hierarchy found matching.+(id/[^\\W]+).+"),
        "Не найдена view в иерархии: {1}"
    ),
    RegexToPatternMessageNormalizer(
        Regex("Found 0 items matching holder with view.+"),
        "Не найдена view в recycler "
    ),
    RegexToPatternMessageNormalizer(
        Regex(".+Perform action single click on.+: \\((.+)\\).+"),
        "Не удалось кликнуть по элементу в Recycler: {1}"
    ),
    RegexToPatternMessageNormalizer(
        Regex("Parameter specified as non-null is null.+parameter (.+)"),
        "Параметр {1} == null"
    )
)
