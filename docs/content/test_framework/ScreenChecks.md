# Screen checks

`Screen` is abstraction that represents `Activity`, `Fragment` or `View` in tests.  
`Screen` has a property `checks` of type `ScreenChecks` with single function `isScreenOpened`.  
`ScreenChecks` has a property `checkOnEachScreenInteraction`. 
It executes `isScreenOpened()` on each interaction with `ViewElement` on the `Screen`.  

???+ warning
    `checkOnEachScreenInteraction` works only if `ViewElement` is created by `element()` function


## Manual check

```kotlin
screen.checks.isScreenOpened()
```

## Writing a custom check

```kotlin
class MyScreen : SimpleScreen() {
  
    override val checks = MyScreenChecks()

    class MyScreenChecks(screen: MyScreen) :
        SimpleScreenChecks<MyScreen>(screen = screen, checkOnEachScreenInteraction = true) {
    
        override fun screenOpenedCheck() {
            super.screenOpenedCheck()
            // Put additional checks here
        }
    }    
}
```

## How to use the same `Screen` with different behavior

It may be useful when one `Screen` contains different UI states.

```kotlin
fun Screen.myScreen(title: String? = null) = MyScreen(title)

class MyScreen(val title: String? = null) : SimpleScreen() {

    class Checks(screen: MyScreen) :
        SimpleScreenChecks<MyScreen>(screen = screen) {

        override fun screenOpenedCheck() {
            super.screenOpenedCheck()
            if (title != null) {
                title.checks.withText(title)
            }
        }
    }    
}
```

