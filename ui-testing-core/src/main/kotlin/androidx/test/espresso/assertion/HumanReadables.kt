package androidx.test.espresso.assertion

import androidx.test.espresso.ViewAssertion
import com.avito.android.test.util.AppendableDescription

/**
 * Dead simple string representation of any [ViewAssertion]
 */
fun ViewAssertion.describe(): String {
    val result = StringBuilder()
    result.append("Check ")
    if (this is ViewAssertions.MatchesViewAssertion) {
        this.viewMatcher.describeTo(AppendableDescription(result))
    } else {
        result.append(this::class.java.simpleName)
    }
    return result.toString()
}
