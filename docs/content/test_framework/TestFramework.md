# Test framework

## ViewElement

PageObject над View. Составляющие:

- InteractionContext - знания про родительский элемент, окружение
- Actions - действия с элементом
- Checks - проверки состояния

### Создание

Для создания используем фабричную функцию `element`:

```kotlin
val submitButton: ViewElement = element(withId(R.id.submit_button))
```

ViewMatcher будет учитывать родительский PageObject, унаследует его matcher.

### Вложенные ViewElement

Все PageObject могут быть вложенными, отражать реальную иерархию:

```kotlin
// parent -> container -> button

val container: ViewElement = element(withId(R.id.container))

val button: ViewElement = container.element(withId(R.id.button))
```

### Кастомный ViewElement

#### Для переиспользования

Бывает удобно отразить вложенность отдельным классом, чтобы переиспользовать в разных экранах.

```kotlin
// parent --> selector --> hint

val selector: ImageSelectorElement = element(withId(R.id.selector))

class ImageSelectorElement(interactionContext: InteractionContext) : ViewElement(interactionContext) {
    val hint: ViewElement = element(withId(R.id.hint))
}
```

#### Для кастомных actions, checks

Еще одна причина для кастомного ViewElement - переопределить кастомные actions, checks.
Примеры: `RatingBarElement`, `BottomSheetElement`.

#### Для дефолтного Matcher

Для переиспользуемых компонентов удобно держать внутри знания про дефолтный матчер.  
При создании элемента будем только дополнять его, но не заменять.  
Пока что не умеем модифицировать уже созданный interaction context, 
поэтому перехватываем при создании элемента:

```kotlin
class ElementWithEmbeddedMatcher : HandleParentContext, ViewElement {

    constructor(interactionContext: InteractionContext)
        : super(interactionContext.provideChildContext(defaultMatcher()))

    constructor(interactionContext: InteractionContext, matcher: Matcher<View>)
        : super(interactionContext.provideChildContext(Matchers.allOf(
        defaultMatcher(), matcher
    )))

}
```

`HandleParentContext` - маркерный интерфейс. 
Изменяет поведение функции `PageObject.element(matcher)`. 
Мы сами создаем дочерний контекст, потому что только мы знаем про дефолтный матчер.

## Screen

Это PageObject для экрана (activity, fragment, dialog, ...)

```kotlin
class PublicProfileScreen : PageObject(), Screen {
    ...
}
```

### rootId

Явно связываем PageObject с конкретным layout:

```kotlin
override val rootId: Int = com.avito.android.public_profile_stuff.R.id.public_profile_screen_root
```

- Помогает быстрее найти какой PageObject для этого экрана и в обратную сторону
- Все дочерние элементы в PageObject неявно проверяем на вхождение в этот layout
- Связывает экран с Gradle-модулем. Это нужно для работы импакт-анализа

### Элементы

PageObject содержит вложенные элементы:

```kotlin
val submitButton: ViewElement = element(withId(R.id.submit_button))
```

Используем фабричный метод `element`, чтобы создать вложенный элемент.
При каждом действии или проверке автоматически проверяем какой экран сейчас отображается.
