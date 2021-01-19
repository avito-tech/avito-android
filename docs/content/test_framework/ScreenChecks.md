# Screen checks

`Screen` is abstraction that represents `Activity`, `Fragment` or `View` in tests. \
`Screen` has a property `checks` of type `ScreenChecks` with single function `isScreenOpened`. \ 
`ScreenChecks` has a property `checkOnEachScreenInteraction`. It makes automatic execution `isScreenOpened` on each interaction with `ViewElement` on that `Screen`. \

## Default `Screen.checks`

???+ warning
    `checkOnEachScreenInteraction` works only if:
    - your `Screen` extends from `PageObject`
    - your `ViewElements` is created by `element(Matcher)` function

```kotlin
interface Screen {

    val checks: ScreenChecks
            get() = StrictScreenChecks(
                        screen = this,
                        checkOnEachScreenInteraction = false
                    )
}
```

## Already existed `ScreenChecks`

- `StrictScreenChecks`. `isScreenOpened` checks if `rootView.id` equals to `Screen.rootId`

## How to customize `isScreenOpened` check

### With writing a custom check

???+ info 
    If your `Screen`  doesn't support `StrictScreenChecks.isScreenOpened` behavior

```kotlin
import com.avito.android.screen.Screen

class MyScreen : Screen {
  
  override val checks = MyScreenChecks()  
  
  class MyScreenChecks : ScreenChecks {
    
    override fun isScreenOpened() {
     TODO("Your check logic")      
    }
  }      
}
```

### With extending from existent check

If `StrictScreenChecks.isScreenOpened` isn't enough for your `Screen`.

```kotlin
import com.avito.android.screen.Screen

class MyScreen : Screen {
  
  override val checks = MyScreenChecks()  
  
  class MyScreenChecks(screen: Screen) : StrictScreenChecks(screen = screen) {
    
    override fun isScreenOpened() {
        super.isScreenOpened()      
        TODO("Your check logic")      
    }
  }      
}
```


## How to make `isScreenOpened` executes on each interaction with `ViewElement`s

???+ warning 
    We recommend to set `checkOnEachScreenInteraction` true. In the future, we will make this behavior default.

### If your `ScreenChecks` extends from `StrictScreenChecks`

```kotlin
import com.avito.android.screen.Screen

class MyScreen : Screen {
  
  override val checks = MyScreenChecks()  
  
  class MyScreenChecks(screen: Screen) : StrictScreenChecks(screen = screen, checkOnEachScreenInteraction = true /*true by default*/) {}      
}
```

That's all `StrictScreenChecks` enable `checkOnEachScreenInteraction`

### If your `ScreenChecks` extends from default `ScreenChecks` interface

```kotlin
import com.avito.android.screen.Screen

class MyScreen : Screen {
  
  override val checks = MyScreenChecks()

    class MyScreenChecks : ScreenChecks {

        override val checkOnEachScreenInteraction = true

    }
}
``` 

## How to use `ScreenChecks.isScreenOpened` in your code if `checkOnEachScreenInteraction = false`?

???+ warning 
    This code could lead you to making mistakes:
    - You could forget to add the check manually 
    - You could add the check in to place belonging to another `Screen`

    This code increases a cognitive pressure

```kotlin
import com.avito.android.screen.Screen

val screen: Screen = TODO("Logic for initializing your Screen")
screen.checks.isScreenOpened()
```

## How to use the same `Screen` with different behavior

???+ info 
    It may be useful when one `Screen` contains different UI states.

```kotlin
fun Screen.myScreen(title: String? = null) = PublishParamsScreen(title)

class PublishParamsScreen(val title: String? = null) : PageObject(), Screen {
    
  class MyScreenChecks(screen: Screen, private val title: String?) : StrictScreenChecks(screen = screen) {
    
    override fun isScreenOpened() {
        super.isScreenOpened()      
        if (title != null) {
            title.checks.withText(title)
        }
    }
  }    
}
```

