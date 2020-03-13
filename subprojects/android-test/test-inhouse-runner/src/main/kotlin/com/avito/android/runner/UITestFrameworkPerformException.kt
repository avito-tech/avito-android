package com.avito.android.runner

@Deprecated("Since 2020.3.2. This class will be removed")
class UITestFrameworkPerformException(
    actionDescription: String,
    viewDescription: String,
    override val cause: Throwable?
) : UITestFrameworkException("Не удалось выполнить '$actionDescription' на view: '$viewDescription'", cause)
