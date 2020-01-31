package com.avito.android.runner

class UITestFrameworkPerformException(
    actionDescription: String,
    viewDescription: String,
    override val cause: Throwable?
) : UITestFrameworkException("Не удалось выполнить '$actionDescription' на view: '$viewDescription'", cause)
