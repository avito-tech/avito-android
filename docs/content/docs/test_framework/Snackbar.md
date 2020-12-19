# Snackbar

**Why do we need a custom component for snackbar testing?** \
We can't predict when snackbar will appear or disappear because we have no API to watch that behaviour. \
So it's hard to realize our test failed because: there are no snackbars, snackbar has already disappeared or it hasn't appeared yet.

**How our library works?** \
We remember all snackbar showings through the test and give you an ability to check that snackbar showing history. 

## How to test snackbars with our library?

1. Add dependencies to Gradle

{{< tabs "applyPlugin" >}}
{{< tab "Kotlin" >}}
Add to your `build.gradle.kts`

```kotlin
dependencies {
    implementation("com.avito.android:snackbar-proxy:$version")
    implementation("com.google.android.material:material:$androidXVersion")
    androidTestImplementation("com.avito.android:snackbar-rule:$version")  
}
```

{{< /tab >}}
{{< tab "Groovy" >}}
Add to your `build.gradle`

```groovy
dependencies {
    implementation("com.avito.android:snackbar-proxy:$version")
    implementation("com.google.android.material:material:$androidXVersion")
    androidTestImplementation("com.avito.android:snackbar-rule:$version")  
}
```

{{< /tab >}}
{{< /tabs >}}

2. Replace `com.google.android.material.snackbar.Snackbar.show()` by our wrapper function

```kotlin
import com.avito.android.snackbar.proxy.showSnackbar
import com.google.android.material.snackbar.Snackbar

val snackbar: Snackbar = TODO("Make a snackbar")
snackbar.showSnackbar()
```

3. Use our `com.avito.android.test.app.second.SnackbarRule` in tests

```kotlin
import com.avito.android.test.app.second.SnackbarRule
import org.junit.Test
import org.junit.Rule

class SomeTest {
    
    @get:Rule
    private val rule = SnackbarRule() 

    @Test  
    fun test() {
        // test logic
  
        rule.assertIsShownWith("text")
        rule.assertIsShownWith(Matchers.Is("text"))
        rule.assertIsShownLastWith("text")
        rule.assertIsShownLastWith(Matchers.Is("text"))
    } 
}
```
