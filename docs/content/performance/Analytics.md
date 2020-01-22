---
title: Реалтайм-аналитика
type: docs
---

# Замеры перформанса экрана

## Что умеем замерять

### Пользовательские метрики

- время от тача до начала инициализации экрана (предынициализация)
- время от тача до окончания отрисовки каждого из потоков данных экрана

### Этапы работы экрана

- время инициализации экрана (`ScreenInitTracker`)
- время `DI` экрана (`ScreenDiInjectTracker`)
- время загрузки данных по сети и с диска (`наследники ContentLoadingTracker`)
- время обработки данных, например конвертации сетевых моделей в презентационные (`ViewDataPreparingTracker`) - не всегда используется, потому что не везде есть
- время отрисовки (с момента посылки команды на отрисовку до того как соответсвующий Runnable отработал на main thread)
- длительность инициализации и `DI` компонентов (сабмодулей)

Отправляем метрики в statsd для realtime-аналитики и clickstream для AB-тестирования и детального анализа.

Пользовательские метрики отражают ожидания пользователя: тапнул по экрану - получил, что хотел.

## Что нужно знать перед написанием кода

- определить тип экрана (с предзагрузкой данных, самостоятельный, субкомпонент)
- создать абстракцию трекера экрана в соответствии с флоу экрана [Типы флоу экранов](#типы-флоу-экранов)
- добавить замеры в код
- синхронизировать имя вашего экрана (screenName) и замеряемые типы контента с [таблицей](http://links.k.avito.ru/cfxNXAeB) (согласовать имена с iOS-платформой)
- проверить на дашборде, что ваши замеры действильно отправляются

## Типы замеряемых экранов

### Самостоятельный экран

- `Activity` без фрагментов
- `Activity` с `Fragment`, если он один на экране
- Конфигурация в которой есть `Activity` и несколько фрагментов одновременно на экране (В этом случае фрагменты выступают субкомпонентами)

### Cубкомпонент

- `Fragment`, который является одним из нескольких отображаемых фрагментов самостоятельного экрана (публичный профиль, главная, etc)
- `Fragment`, который является одним из шагов какого-нибудь флоу (подачи, выставления рейтинга продавцу, etc)

Если вы хотите все шаги какого-нибудь флоу затрекать как самостоятельные экраны, это также валидно.
Рассматривать фрагмент как самостоятельный экран, так как и как субкомпонент в этом случае - вопрос предпочтений.
Фрагмент, покрытый аналитикой, может и должен уметь выступать как самостоятельным экраном, так и субкомпонентом (элементом экрана).

### Экран с предзагрузкой данных

Когда экран осуществляет сетевые запросы и подготовку данных для отображения на следующем экране.
Например, при выставлении рейтинга продавцу, сначала нажимаем на рейтинг, открывается фейковый экран с лоадером.
Этот экран запрашивает данные для отображения следующего экрана, который будет известен по итогам запроса.
Если авторизация есть - переходим на флоу выставления рейтинга, если нет - на авторизацию.
С точки зрения пользователя: нажал на рейтинг, появился лоадер, появился экран выбора объявления, по которому будет выставляться рейтинг.
Разумно считать, что этап предварительной подготовки для экрана с выбором объявления является частью флоу рейтинга.


## Типы трекеров

- `ScreenDiInjectTracker` - трекает время инициализации `DI` и время предынициализации (от тача до начала `DI`)
Время предынициализации трекается автоматически.
- `ScreenInitTracker` - время инициализации (создание и подключение презентеров, роутеров, вью)
- `LocalContentLoadingTrackerImpl` - загрузка одного из потоков данных из локального хранилища, скажем из префов
- `ServerContentLoadingTrackerImpl` - загрузка одного из потоков данных с сервера
- `ViewDataPreparingTracker` - подготовка данных для экрана (конвертация сетевых моделей в презентационные, например)
- `ContentDrawingTracker` - отрисовка данных. Через `handler.post` учитываает время, которое текущее изменение простояло в очереди на мейн треде
- `UnknownScreenTracker` - трекер для экрана с предзагрузкой данных

Понятия:
- `screenName` - имя экрана, которое обычно передается на этапе создания `DI`
- `contentType` - название потока данных.
- `page` - номер страницы. Обычно используется для экранов с бесконечными списками.
Отправляется первые пять страниц (`page-001`..`page-005`), остальные отправляются как `page-etc`. Можно опустить.
- `failure` - либо `failure` либо `success`. Можно использовать свою константу для фейла.
- `SCREEN` - константа, которая используется для того, чтобы ScreenInitTracker и ScreenDiInjectTracker
работали по умолчанию в режиме для самостоятельного экрана.
- `NO_TIME` - константа, которая используется в аргументах фукнций трекинга некоторых трекеров, когда мы хотим отправить данные в `statsd`,
которые получили с прошлого экрана `UnknownScreenTracker`
- `SUCCESS` - дефолтная константа, которая используется в трекерах, которая обозначает, что в текущем потоке данных все операции прошли успешно.
Успешно получили данные с сети, например, потом успешно их отрисовали.

В случае `ScreenDiInjectTracker` и `ScreenInitTracker` contentType используется для отличения субкомпонента от компонента.
Например для экрана-субкомпонета выбора объявления, по которому будет выставляться рейтинг продавцу это будет `select-advert`.
В случае `ContentLoadingTracker`-ов, `ViewDataPreparingTracker`, `ContentDrawingTracker` используется для обозначения потока данных.
Например на главной странице это объявления `adverts` и шорткаты `shortcuts`.

Пользовательские метрики отправляются только первый раз при трекинге.
Этапы работы экрана отправляются каждый раз при вызове соответствующего метода трекинга.

Примеры вызовов методов трекеров:

```kotlin
interface ContentDrawingTracker : Tracker {

    fun trackContentDrawingTracker(page: Int? = null, failure: Boolean)

}
//затрекали успешную отрисовку (например, список объявлений)
tracker.trackContentDrawingTracker(failure = false)

//затрекали неуспешную отрисовку (например, заглушку с предложением повторить запрос)
tracker.trackContentDrawingTracker(failure = true)
```

```kotlin
interface ContentLoadingTracker : Tracker {

    fun trackContentLoading(page: Int? = null, failure: String, durationMs: Long = NO_TIME)

}

//затрекали успешную отрисовку 1 страницы (например, списка объявлений)
tracker.trackContentLoading(page = 1, failure = SUCCESS)

//затрекали неуспешную отрисовку 1 страницы  (например, заглушку с предложением повторить запрос)
tracker.trackContentLoading(page = 1, failure = FAILURE)
```

## Типы флоу экранов

Флоу экранов бывают двух типов: стандартный и с предзагрузкой данных.

### Стандартный флоу экранов

1. Пользователь нажимает на кнопку на экране А
1. Создается экран Б (здесь трекаем инициализацию и `DI`)
1. Экран Б грузит данные (трекаем загрузку потока данных)
1. Экран Б готовит презентационные данные (трекаем подготовку потока данных)
1. Экран Б отрисовывает презентационные данные (трекаем отрисовку потока данных)

### Флоу экранов с предзагрузкой

Этот флоу отличаются от стандартных тем, что данные для отображения экран грузит не сам:
данные для экрана грузит какой-либо предшествующий экран.

Экран Б из примеров - это экран с предзагрузкой [с экрана А].

Вариант 1

1. Пользователь нажимает на кнопку на экране А
1. Экран А грузит данные (Создаем `UnknownScreenTracker` и трекаем время загрузки данных)
1. Создается экран Б (здесь восстанавливаем данные с экрана A через `ScreenTransfer`)
1. Экран Б готовит презентационные данные
1. Экран Б отрисовывает презентационные данные

Пример: пользователь нажимает кнопку "разместить объявление". В зависимости от разных условий, пользователя могут отправить
на экран размещенного объявления, на экран оплаты размещений или на экран применения дополнительных услуг.

Вариант 2

1. Пользователь нажимает на кнопку на экране А
1. Экран А переходит на экран с крутилкой, где это происходит
Создаем `UnknownScreenTracker` и трекаем время инициализации, `DI` и загрузки данных
1. Создается экран Б (здесь восстанавливаем данные с экрана A через `ScreenTransfer`)
1. Экран Б готовит презентационные данные
1. Экран Б отрисовывает презентационные данные

Преимущественно такая логика представлена в iOS-платформе.

### Пример с главной страницы

{{<mermaid>}}
graph TD
    A(Тап по шорткату. Запоминаем дату тача) -->|ждем создания экрана| B(ОС создала экран Serp)
    B -->|пошел метод onCreate. Начинаем трекать DI | C(Dependency Injection)
    C -->|Здесь же начинаем трекать Init| D(Init. Закончили трекать когда подключились к презентеру)
    D --> E(load shortcuts)
    E --> G(prepare shortcuts)
    G --> K(draw shortcuts)
    D --> F(load serp)
    F --> H(prepare serp - переводим сетевые модели в презентационные)
    H --> L(draw serp)
{{</mermaid>}}

## Как замерять перформанс самостоятельного экрана или субкомпонента

В dagger dependency понадобится:

```kotlin
interface SelectAdvertDependencies : ComponentDependencies {
    ...

    fun screenTrackerFactory(): ScreenTrackerFactory

    fun timerFactory(): TimerFactory

}
```

Создаем dagger-модуль, который подключаем к компоненту экрана:

```kotlin
@Module(includes = [Declarations::class])
object SelectAdvertAnalyticsModule {

    @Provides
    @PerFragment
    @JvmStatic
    fun providesScreenInitTracker(
        screenTrackerFactory: ScreenTrackerFactory,
        @ScreenAnalytics screenName: String,
        @ScreenAnalytics isSubComponent: Boolean,
        factory: TimerFactory
    ): ScreenInitTracker {
        return if (isSubComponent) {
            screenTrackerFactory.createInitTracker(screenName, factory, CONTENT_TYPE_SELECT_ADVERT)
        } else {
            screenTrackerFactory.createInitTracker(screenName, factory)
        }
    }

    @Provides
    @PerFragment
    @JvmStatic
    internal fun providesScreenDiInjectTracker(
        screenTrackerFactory: ScreenTrackerFactory,
        @ScreenAnalytics screenName: String,
        @ScreenAnalytics isSubComponent: Boolean,
        factory: TimerFactory
    ): ScreenDiInjectTracker {
        return if (isSubComponent) {
            screenTrackerFactory.createDiInjectTracker(screenName, factory, CONTENT_TYPE_SELECT_ADVERT)
        } else {
            screenTrackerFactory.createDiInjectTracker(screenName, factory)
        }
    }

    @Provides
    @PerFragment
    @JvmStatic
    fun providesScreenFlowTrackerProvider(
        screenTrackerFactory: ScreenTrackerFactory,
        @ScreenAnalytics screenName: String,
        factory: TimerFactory
    ): ScreenFlowTrackerProvider {
        return screenTrackerFactory.createScreenFlowTrackerProvider(
            screenName,
            factory
        )
    }

    @Module
    internal interface Declarations {

        @Binds
        @PerFragment
        fun bindSelectAdvertTracker(tracker: SelectAdvertTrackerImpl): SelectAdvertTracker
    }

}
```

- isSubComponent: Boolean - если экран может использоваться как отдельный экран и как субкомпонент, этот флаг подскажет,
как правильно настроить трекеры для отправки данных
- screenName - имя самостоятельного экрана, или родительского в случае субкомпонента. Требует согласования с iOS [табличка](http://links.k.avito.ru/cfxNXAeB)
- CONTENT_TYPE_SELECT_ADVERT - строковая константа, которая помогает отличить данные текущего субкомпонента в графане от данных родительского экрана.
Если константу не передавать как аргумент функции, то трекеры будут работать как для самостоятельного экрана с именем screenName
- если ваш экран не будет выступать в качестве субкомпонента, то isSubComponent можно не использовать.

Субкомпоненты удобны, когда вам нужно информацию со всех шагов видеть в одном окне в графане.

Отличия самостоятельных экранов от субкомпонентов:
- У субкомпонента есть родитель - самостоятельный экран, который передает субкомпоненту собственный screen name.
- Все запросы на загрузку, подготовку и отрисовку буду относиться к screen name, который передал родитель
- субкомпонент имеет строковую константу `content-type`, которая прибавляется при отправке метрик инициализации и `DI` в `statsd`, 
чтобы отличать их от основного экрана
- У субкомпонентов трекается только абсолютное время `DI` и инициализации

Создаем абстракцию трекера:

```kotlin
class HomeTrackerImpl @Inject constructor(
    private val flowTrackerProvider: ScreenFlowTrackerProvider
    private val diInjectTracker: ScreenDiInjectTracker
    private val initTracker: ScreenInitTracker
    factory: TimerFactory
) : HomeTracker {

    private var locationFromSaveLoadTracker: ContentLoadingTracker? = null
    private var locationLoadTracker: ContentLoadingTracker? = null

    private var advertsLoadTracker: ContentLoadingTracker? = null
    private var advertsPrepareTracker: ViewDataPreparingTracker? = null
    private var advertsDrawingTracker: ContentDrawingTracker? = null

    private var shortcutsLoadTracker: ContentLoadingTracker? = null
    private var shortcutsPrepareTracker: ViewDataPreparingTracker? = null
    private var shortcutsDrawingTracker: ContentDrawingTracker? = null

    override fun trackDiInject(durationMs: Long) {
        diInjectTracker.track(durationMs)
    }

    override fun startInit() {
        initTracker.start()
    }

    override fun trackInit() {
        initTracker.trackInit()
    }

    override fun startLoadingLocation() {
        locationFromSaveLoadTracker =
            flowTrackerProvider.getContentLoadingFromLocalStorage(CONTENT_LOAD_LOCATION_FROM_SAVE).apply { start() }
    }

    override fun trackLocationLoadedFromSave(locationIdForLoad: String?) {
        locationFromSaveLoadTracker?.trackContentLoading(null, SUCCESS)
        locationFromSaveLoadTracker = null

        locationLoadTracker = if (locationIdForLoad != null) {
            flowTrackerProvider.getContentLoadingFromRemoteStorage(CONTENT_LOAD_LOCATION)
        } else {
            flowTrackerProvider.getContentLoadingFromLocalStorage(CONTENT_LOAD_LOCATION)
        }.apply { start() }
    }

    override fun trackLocationLoaded() {
        locationLoadTracker?.trackContentLoading(null, SUCCESS)
        locationLoadTracker = null
    }

    override fun trackLocationLoadError() {
        locationLoadTracker?.trackContentLoading(null, FAILURE)
        locationLoadTracker = null

        locationFromSaveLoadTracker?.trackContentLoading(null, FAILURE)
        locationFromSaveLoadTracker = null
    }

    override fun startLoadingAdverts() {
        advertsLoadTracker =
            flowTrackerProvider.getContentLoadingFromRemoteStorage(CONTENT_TYPE_ADVERT_ITEMS)
                .apply { start() }
    }

    override fun trackAdvertsLoaded(page: Int) {
        trackAdvertsLoaded(page, SUCCESS)
    }

    override fun trackAdvertsPrepare(page: Int) {
        trackAdvertsPrepare(page, false)
    }

    override fun startAdvertsDraw() {
        startAdvertsDrawingTracker()
    }

    override fun trackAdvertsDraw(page: Int) {
        trackAdvertsDraw(page, false)
    }

    override fun trackAdvertsLoadError(page: Int) {
        trackAdvertsLoaded(page, FAILURE)
    }

    override fun trackAdvertsErrorPrepare(page: Int) {
        trackAdvertsPrepare(page, true)
    }

    override fun trackAdvertsErrorDraw(page: Int) {
        trackAdvertsDraw(page, true)
    }

    private fun trackAdvertsLoaded(page: Int, result: String) {
        advertsLoadTracker?.trackContentLoading(page, result)
        advertsLoadTracker = null

        advertsPrepareTracker = flowTrackerProvider.getViewPreparing(CONTENT_TYPE_ADVERT_ITEMS).apply { start() }
    }

    private fun trackAdvertsPrepare(page: Int, failure: Boolean) {
        advertsPrepareTracker?.trackViewDataPreparing(page, failure)
        advertsPrepareTracker = null
    }

    private fun startAdvertsDrawingTracker() {
        advertsDrawingTracker = flowTrackerProvider.getContentDrawing(CONTENT_TYPE_ADVERT_ITEMS).apply { start() }
    }

    private fun trackAdvertsDraw(page: Int, failure: Boolean) {
        advertsDrawingTracker?.trackContentDrawingTracker(page, failure)
        advertsDrawingTracker = null
    }

    override fun startLoadingShortcuts(local: Boolean) {
        shortcutsLoadTracker = if (local) {
            flowTrackerProvider.getContentLoadingFromLocalStorage(CONTENT_TYPE_SHORTCUTS)
        } else {
            flowTrackerProvider.getContentLoadingFromRemoteStorage(CONTENT_TYPE_SHORTCUTS)
        }.apply { start() }
    }

    override fun trackShortcutsLoaded() {
        shortcutsLoadTracker?.trackContentLoading(null, SUCCESS)
        shortcutsLoadTracker = null
    }

    override fun startShortcutsPrepare() {
        shortcutsPrepareTracker = flowTrackerProvider.getViewPreparing(CONTENT_TYPE_SHORTCUTS).apply { start() }
    }

    override fun trackShortcutsPrepare() {
        shortcutsPrepareTracker?.trackViewDataPreparing(null, false)
        shortcutsPrepareTracker = null

        shortcutsDrawingTracker = flowTrackerProvider.getContentDrawing(CONTENT_TYPE_SHORTCUTS).apply { start() }
    }

    override fun trackShortcutsDraw() {
        shortcutsDrawingTracker?.trackContentDrawingTracker(null, false)
        shortcutsDrawingTracker = null
    }

    override fun stopShortcutsLoad() {
        shortcutsPrepareTracker = null
    }

    override fun trackShortcutsLoadError() {
        shortcutsLoadTracker?.trackContentLoading(null, FAILURE)
        shortcutsLoadTracker = null
    }

    override fun startReloadSession() {
        stopAllSessions()
    }

    override fun startLoadMoreSession() {
        stopAllSessions()
    }

    override fun stopLoadAdvertSession() {
        stopAllSessions()
    }
```

## Как выглядит трекер с предзагрузкой

```kotlin
interface UnknownScreenTracker {

    fun trackInit(durationMs: Long)

    fun trackDiInject(durationMs: Long)

    fun startLoading()

    fun trackLoading()

    fun toScreenTransfer(): ScreenTransfer
}
```

Вариант 1.
Необходимо затрекать только loading, потому что экран в целом отдельный и только запрос относится к целевому экрану.

Вариант 2.
Необходимо затрекать init, di, loading, потому что экран целиком занимается тем, что выясняет, какой экран будет следующим.

Метод `toScreenTransfer` отдает нам объект, хранящий все необходимые данные для передачи целевому экрану:

```kotlin
class ScreenTransfer(
    val initTime: Long,
    val loadingTime: Long,
    val diTime: Long
): Parcelable
```

Этот объект необходимо положить в интент для целевого экрана с помощью `BaseActivity.saturateIntentWithTrackingInfo`

```kotlin
override fun followDeepLink(deepLink: DeepLink) {
        deepLinkIntentFactory.getIntent(deepLink)
            ?.let { startActivity(saturateIntentWithTrackingInfo(it, tracker.toScreenTransfer())) }
    }
```

В целевом экране необходимо затрекать эти данные в onCreate:

```kotlin
if (savedInstanceState == null) {
    val screenTransfer = rescueScreenTransfer()
    screenTransfer?.let {
        tracker.recover(it)
    }
}
```

Код трекера

```kotlin
class RatingPublishTrackerImpl @Inject constructor(
    ...
    private val recovery: ScreenTransferRecovery
) : RatingPublishTracker {

    override fun recover(transfer: ScreenTransfer) {
        recovery.recover(transfer, RATING_PUBLISH_NAME, CONTENT_TYPE_PRELOAD)
    }
```

CONTENT_TYPE_PRELOAD - константа, которая позволяет правильно затрекать поток данных который был получен на предыдущем экране.

RATING_PUBLISH_NAME - имя текущего экрана

В даггер модуль добавить:

```kotlin
@Scope: PerActivity, PerFragment
@Binds
fun bindScreenTransferRecovery(recovery: ScreenTransferRecovery.Impl): ScreenTransferRecovery
```

## Как во время разработки проверить, что замеры отправляются

Можно смотреть лог, в нем будут подобные строчки:

`TIME:android-debug.355.os.29.screen-performance.absolute.SearchResults.-.content-loading-server.advertisements.page-001.Wi-Fi.-.success:992`

- `android-debug` - дебажная версия приложения, в релизной - `android`
- 355 - `version code`
- 29 - версия ОС
- `SearchResults` - `screenName`, имя экрана
- `content-loading-server` - загрузка данных с сервера
- `advertisements` - `contentType`, поток данных - объявления
- `success` - запрос закончился успешно
- 992 - количество миллиисекунд, которые длился запрос


Чтобы посмотреть отсылаемые в графану значения, нужно
- зайти на [дашборд](http://links.k.avito.ru/3L)
- В `Screen name` выбрать имя вашего экрана
- В `Release/debug` выбрать `android-debug` для `debug`-сборки, и `android` - для релизной
- Должны появиться значения на дашбордах

Данные в дашборде отображаются с 30-секундной задержкой через сервис агрегации метрик statsd.


### Пример в графане

Пример с экрана `RatingPublish`

![Пример](/image/rating_publish.png)

В панели Steps можно увидеть:

- `preinit` - время от тача по кнопке оставить рейтинг до начала `DI` экрана с флоу проставления рейтинга.
Этот этап включает промежуточный шаг, когда пользователь видел лоадер `preload`
- `init` - этап инициализации экрана `RatingPublish`
- `di-inject` - этап `DI` экрана `RatingPublish`
- `load preload` - Промежуточный этап, когда пользователь видел лоадер `preload`.
Затрекали с помощью `UnknownScreenTracker`
- `draw preload` - отрисовка данных полученных с прошлого экрана
- `load adverts-list` - этап загрузки с сервера списка объявлений на субкомпоненте-фрагменте `select-advert`
(выбор объявления, по которому будет проставляться рейтинг, первый шаг на флоу проставления рейтинга)
- `load next-step` - этап загрузки с сервера следующего этапа экрана `RatingPublish`
- `draw next-step, draw adverts-list` - время отрисовки. Не стал отдельно трекать подготовку, ее здесь нет.
Если бы залогировал, был бы еще этап подготовки: `parse next-step, parse adverts-list`
- `component init select-advert` - этап инициализации субкомпонента `select-advert`
- `component di preload, component init preload` - затрекали как субкомпонент время инициализации и `DI` с прошлого экрана
- `component di select-advert` - этап `DI` субкомпонента `select-advert`
