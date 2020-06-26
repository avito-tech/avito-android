# Screen checks

`Screen` is abstraction that represents `Activity`, `Fragment` or `View` in tests. \
`Screen` has a property `checks` of type `ScreenChecks` with single function `isScreenOpened` \ 
`ScreenChecks` has a property `checkOnEachScreenInteraction` which is responsible for: If we shall execute `isScreenOpened` on each interaction with `ViewElement` on that `Screen` \

Default behavior is:
- `Screen.checks` type is `StrictScreenChecks`
- `checkOnEachScreenInteraction` value is `false`
- `isScreenOpened` checks if `rootView.id` equals to `Screen.rootId` 

## How to use ScreenChecks directly?

```kotlin
import com.avito.android.screen.Screen

val screen: Screen = TODO("Logic for initializing your Screen")
screen.checks.isScreenOpened()
```

## How to customize `isScreenOpened` check

### Write fully custom check

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


### Extend from existent check on `rootId`

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

### Extend your `ScreenChecks` from `StrictScreenChecks`

```kotlin
import com.avito.android.screen.Screen

class MyScreen : Screen {
  
  override val checks = MyScreenChecks()  
  
  class MyScreenChecks(screen: Screen) : StrictScreenChecks(screen = screen, checkOnEachScreenInteraction = true /*true by default*/) {}      
}
```

That's all `StrictScreenChecks` enable `checkOnEachScreenInteraction`

### Override `checkOnEachScreenInteraction` in your `ScreenChecks`

```kotlin
import com.avito.android.screen.Screen

class MyScreen : Screen {
  
  override val checks = MyScreenChecks()  
  
  class MyScreenChecks : ScreenChecks {
    
   override val checkOnEachScreenInteraction = true

  }      
}
```
