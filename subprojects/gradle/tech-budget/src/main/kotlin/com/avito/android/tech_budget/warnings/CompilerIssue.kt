package com.avito.android.tech_budget.warnings

public class CompilerIssue(
    public val group: String,
    public val rule: String,
    public val debt: Int,
    public val location: String,
    public val message: String,
)
