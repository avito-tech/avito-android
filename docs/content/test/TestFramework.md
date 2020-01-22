---
title: Тестовый фреймворк
type: docs
---

# Тестовый фреймворк

## PageObject

[PageObject - Martin Fowler](https://martinfowler.com/bliki/PageObject.html)

## Interaction Context

TODO: move to advanced section

Внутреннее состояние PO. 
Используем чтобы отразить иерархию View, унаследовать проверки.

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

Для переиспользуемых компонентов удобно держать внутри знания про дефолтный матчер.\
При создании элемента будем только дополнять его, но не заменять.\
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
- Связывает экран с gradle-модулем. Это нужно для работы импакт-анализа

### Элементы

PageObject содержит вложенные элементы:

```kotlin
val submitButton: ViewElement = element(withId(R.id.submit_button))
```

Используем фабричный метод `element`, чтобы создать вложенный элемент.
При каждом действии или проверке автоматически проверяем какой экран сейчас отображается.

### ScreenChecks

Каждый экран умеет проверять, открыт он сейчас или нет.\
Проверить можно вручную:

```kotlin
assertion("""Перешли на экран ...""") {
    Screen.vasPublish.checks.isOpened()
}
```

Недостатки очевидны:

- Добавляем проверки вручную, зачастую уже после того как столкнемся с ошибкой
- Проверки надо поддерживать
- Это шум. Если я обращаюсь к элементу экрана, то ожидаю что экран должен быть сейчас виден.

Мы умеем автоматически проверять какой экран открыт перед любым действием или проверкой.\
Для этого необходимо подготовить экран:

- Проверить что экран наследуется от `PageObject`
- Проверить что экран использует автоматическую проверку

```kotlin
override val checks: ScreenChecks = StrictScreenChecks(this)
```

Включим это по умолчанию в [MBS-7204](http://links.k.avito.ru/MBS7204) 

- Создавать вложенные элементы с помощью фабричной функции `element`:

```kotlin
val submitButton: ViewElement = element(withId(R.id.submit_button)) // 👍
val submitButton = ViewElement(withId(R.id.submit_button))          // 👎 Legacy
```

Так мы связываем вложенный элемент с экраном и используем это уже во всех его действиях:

```kotlin
submitButton.checks.displayedWithText("Отправить") // <-- здесь неявно проверим что текущий экран открыт
```

### Параметризация экрана

Часто бывает что один и тот-же экран умеет показывать разные данные, загруженные из API.\
Удобно знать про эти состояния и учитывать в проверках:

```kotlin
fun Screen.publishParamsWithTitle(title: String) = PublishParamsScreen(title)

class PublishParamsScreen(val title: String? = null) : PageObject(), Screen {
    
    ...
    if (title != null) {
        title.checks.withText(title)
    }
    ...
}
```

## How to test

### Network

TBD

### Analytics

Проверяем что событие доставлено до транспорта (Analytics) с нужными параметрами.\
Не нужно тестировать сам транспорт.

```kotlin
@get:Rule
val analytics = AnalyticsRule()

 @Test
fun screen_shown___ShowSearchMapEvent_should_be_sent() {
    screenRule.start()

    Screen.mapScreen.checks.isOpened()

    analytics.checks.assertEventTracked<ShowSearchMapEvent>()
}
```
