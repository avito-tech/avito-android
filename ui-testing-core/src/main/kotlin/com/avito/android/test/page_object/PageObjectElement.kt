package com.avito.android.test.page_object

import android.view.View
import com.avito.android.test.action.Actions
import com.avito.android.test.checks.Checks
import org.hamcrest.Matcher


// TODO: hide or deprecate private api
abstract class PageObjectElement : PageObject(), Actions {
    @Deprecated("Don't use this matcher directly. Make child interaction context instead.")
    abstract val matcher: Matcher<View>
    abstract val actions: Actions
    abstract val checks: Checks
}

