package com.avito.android.feedback

/**
 * An event for Instant Feedback.
 *
 * @param scenario survey slug in AvitoSurvey
 * @param arguments survey template arguments, must be declared in the survey as payloadFields
 */
public data class InstantFeedbackEvent(
    val scenario: String,
    val arguments: Map<String, String> = emptyMap(),
)
