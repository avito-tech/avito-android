package com.avito.android.test.report

public interface Report {

    /**
     * Add request info as current test step entry
     *
     * @param label one-liner you see in test step comments
     * @param content detailed info about request, accessible via click on label in report
     */
    public fun addHtml(label: String, content: String, wrapHtml: Boolean = true)

    /**
     * Add text entry with content [text] to the current report step
     *
     * @param label one-liner you see in test step comments
     *
     * Use it with big text. Behind the scenes it load text as file.
     */
    public fun addText(label: String, text: String)

    /**
     * Add comment entry with content [comment] to the current report step
     *
     * Use it with small text. Behind the scenes it inlines.
     */
    public fun addComment(comment: String)

    /**
     * Add screenshot entry with content [label] to the current report step
     */
    public fun addScreenshot(label: String)

    /**
     * Add entry with [assertionMessage] to the current report step
     */
    public fun addAssertion(label: String)
}
