---
title: Dependency Injection with Dagger
type: docs
---

# Dagger

## Материалы

### Обязательно прочесть

- [Документация](https://google.github.io/dagger/users-guide)
- [Тредик на гитхабе с tips & tricks для kotlin](https://github.com/google/dagger/issues/900)

### Рекомендую к изучению

- [Видео от одного из авторов Dagger Ron Shapiro, о том, как правильно его готовить](https://www.youtube.com/watch?v=PBrhRvhF00k)
- [Немного устаревшая статья про правильное приготовление dagger](https://medium.com/square-corner-blog/keeping-the-daggers-sharp-%EF%B8%8F-230b3191c3f)

{{< hint info>}}
⚠️ Нельзя постигнуть даггер, не смотря какой код он генерирует!
{{< /hint >}}

## Как правильно работать с Dagger

- Поддерживаемость прежде всего. Граф зависимостей делай маскимально простой и понятный
- `Application Component` имеет минимальный скоуп - содержит не более необходимого чилсла зависимостей
Если зависимость может быть не глобальной, оне должа быть в скоупе экрана, а не приложения
- Меньше мокай в `instrumentation`-тестах

### `Provides` → `Binds`

Используй `Binds` вместо `Provides` по-возможности всегда\
👍 Dagger генерирует меньше классов и получается более эффективный код.\
👍 Проще писать `Module`-классы и изменять код, как в конструкторах, так и в `Module`-классах.\
В самом простом случае необходимо добавить к объявлению класса `@Inject constructor` и переделать метод класса на абстрактный, который связывает интерфейс и реализацию:

Было:

```kotlin
@Provides
@PerFragment
internal fun provideSearchDeepLinkInteractor(
    api: SearchApi,
    searchParamsConverter: SearchParamsConverter,
    schedulersFactory: SchedulersFactory
): SearchDeepLinkInteractor {
    return SearchDeepLinkInteractorImpl(api, searchParamsConverter, schedulersFactory)
}
```

Стало:

```kotlin
@Binds
@PerFragment
fun bindsSearchDeepLinkInteractor(interactor: SearchDeepLinkInteractorImpl): SearchDeepLinkInteractor
```

Было:

```kotlin
class SearchDeepLinkInteractorImpl(
    private val api: SearchApi,
    private val searchParamsConverter: SearchParamsConverter,
    private val schedulers: SchedulersFactory
) : SearchDeepLinkInteractor
```

Стало:

```kotlin
class SearchDeepLinkInteractorImpl @Inject constructor(
    private val api: SearchApi,
    private val searchParamsConverter: SearchParamsConverter,
    private val schedulers: SchedulersFactory
) : SearchDeepLinkInteractor
```

### Когда использовать `@Provides`

Когда идет любое конфигурирование/инициализация, например `features`.
Dagger - инструмент `DI`. Старайтесь минимально заниматься решением задач не связанных с DI через Dagger.
Меньше кода в Dagger-модуле - лучше.

```kotlin
@Provides
@PerFragment
internal fun providePermissionHelper(features: Features, dialogRouter: DialogRouter): PermissionHelper {
    val permissionHelper = FragmentPermissionHelper(fragment)
    return if (features.geoPermissionDialog.value) {
        LocationPermissionHelperWrapper(permissionHelper, dialogRouter)
    } else {
        permissionHelper
    }
}
```

Если нужно мокать методы модуля для тестирования. Мокать `binds`-методы не имеет смысла - это просто источник информации для процессора Dagger.\
Инстанциирование сторонних классов. Невозможно прописать `Inject` в конструктор класса, к исходному коду которого нет доступа.

```kotlin
@Provides
@JvmStatic
internal fun provideAdapterPresenter(provider: ItemBinder): AdapterPresenter {
return SimpleAdapterPresenter(provider, provider)
}
```

### Static providers: object

При использовании `@Provides` объявляй класс `object`.

👍 В этом случае R8 удалит всю инициализацию и может заинлайнить методы (они будут действительно статическими).

```kotlin
@Module
object Module {

    @Provides
    fun provideObject(): Object {
        return Object()
    }
}
```

👎 Нельзя мокать модули в тестах, что не часто нужно.

### IntoSet / IntoMap

Используй `IntoSet` (`IntoMap`). Проставь `JvmSuppressWildcards`, чтобы Dagger понял какого типа вам нужны данные.\
👍 Позволяет в разных модулях собирать необходимые вам объекты в список.\
Чтобы подключить новый класс и он долетел куда нужно достаточно соблюсти интерфейс, и добавить `IntoSet` (`IntoMap`) 

```kotlin
@Binds
@IntoSet
fun bindCategorySettingsItemBlueprint(blueprint: CategorySettingsItemBlueprint): ItemBlueprint<*, *>

@Binds
@IntoSet
fun bindLogoWithVersionSettingsItemBlueprint(blueprint: LogoWithVersionSettingsItemBlueprint): ItemBlueprint<*, *>

@Provides
    @PerActivity
    internal fun provideItemBinder(
        blueprints: Set<@JvmSuppressWildcards ItemBlueprint<*, *>>
    ): ItemBinder {
        return with(ItemBinder.Builder()) {
            blueprints.forEach {
                registerItem(it)
            }
            build()
        }
    }
```

👎 В сложной иерархии модулей может потеряться контроль над тем, что прилетает из графа dagger.\
👎 Необходимо использовать квалификтаторы (`Named`), в случае если интерфейсы клэшатся и нужно сделать два набора данных

### Scope overuse

Не злоупотребляй скоупами без надобности.\
Каждый скоуп, кроме `Reusable` (`Singleton`, `PerActivity`, `PerFragment`, etc) порождает использование Dagger-класса `DoubleCheck`, 
который реализует проверку `DoubleCheckLock` для гарантий `Singleton`.

```kotlin
public T get() {
    Object result = instance;
    if (result == UNINITIALIZED) {
      synchronized (this) {
        result = instance;
        if (result == UNINITIALIZED) {
          result = provider.get();
          instance = reentrantCheck(instance, result);
          /* Null out the reference to the provider. We are never going to need it again, so we
           * can make it eligible for GC. */
          provider = null;
        }
      }
    }
    return (T) result;
  }
```


`Reusable` scope порождает проверку `SingleCheck`, которая менее строгая:

```kotlin
public T get() {
    Object local = instance;
    if (local == UNINITIALIZED) {
      // provider is volatile and might become null after the check, so retrieve the provider first
      Provider<T> providerReference = provider;
      if (providerReference == null) {
        // The provider was null, so the instance must already be set
        local = instance;
      } else {
        local = providerReference.get();
        instance = local;

        // Null out the reference to the provider. We are never going to need it again, so we can
        // make it eligible for GC.
        provider = null;
      }
    }
    return (T) local;
  }
```

### Component.Builder → Component-Factory

👍 Ошибка когда при создании компонента забывают вызвать один из методов билдера вылетит во время компиляции а не в рантайме.

Было:

```kotlin
interface SettingsComponent {

    fun inject(activity: SettingsActivity)

    @Component.Builder
    interface Builder {

        fun settingsDependencies(settingsDependencies: SettingsDependencies): Builder

        fun locationDependencies(locationDependencies: LocationDependencies): Builder

        @BindsInstance
        fun state(state: Kundle?): Builder

        @BindsInstance
        fun resources(resources: Resources): Builder

        @BindsInstance
        fun settingsItemsStream(settingsItemsStream: PublishRelay<String>): Builder

        fun build(): SettingsComponent

    }
}
```

Стало:

```kotlin
interface SettingsComponent {

    fun inject(activity: SettingsActivity)

    @Component.Factory
    interface Builder {

        fun create(
            settingsDependencies: SettingsDependencies,
            locationDependencies: LocationDependencies,
            @BindsInstance state: Kundle?,
            @BindsInstance resources: Resources,
            @BindsInstance settingsItemsStream: PublishRelay<String>
        ): SettingsComponent

    }
}
```

Лучше именовать параметры при вызове, чтобы не перепутать, если есть аргументы одного типа идущие последовательно:

```kotlin
DaggerSettingsComponent.factory()
            .create(
                settingsDependencies = findComponentDependencies(),
                locationDependencies = findComponentDependencies(),
                state = savedInstanceState?.getKundle(KEY_SETTINGS_PRESENTER),
                resources = resources,
                settingsItemsStream = PublishRelay.create()
            )
            .inject(this)
```

### BindInstance → stateless module

👍 Вы сможете провайдить объект в дерево Dagger не добавляя его в конструктор модуля.\
Это сделает его статическим, что хорошо. См. пункт 1 про статические провайдеры.

Было:

```kotlin
@Component(modules = [SettingsModule::class])
interface SettingsComponent {

fun inject(activity: SettingsActivity)
    @Component.Factory
    interface Factory {
        fun module(module: SettingsModule): Builder

        fun create(): SettingsComponent
    }
}

class SettingsModule(val kundle: Kundle) {...}
```

Стало:

```kotlin
@Component(modules = [SettingsModule::class])
interface SettingsComponent {

fun inject(activity: SettingsActivity)

    @Component.Factory
    interface Factory {

        @BindsInstance
        fun create(state: Kundle?): Factory
    }
}

object : SettingsModule() {...}
```

Хорошее правило:\
✅ `Singleton` (`PerActivity`, `PerFragment`), если важна гарантия единственности\
✅ `Reusable` - если singleton нужен для оптимизации\
✅ во всех остальных случаях избегайте скоупов 

## Перевод субкомпонентов уровня `Application` в компоненты

### Проблема

`Subcomponents` в сгенерированном коде - это вложенный класс в `Component`.\
Поэтому использовать `subcomponent` для фичей\экранов дорого:

- Ломает `compilation avoidance`: каждое изменение субкомпонента приводит к перегенерации компонента в avito (самый тяжелый модуль с тестами).
- Хуже масштабируется: `ApplicationComponent` распухает, содержит все фичи, растет время его компиляции.
- Дольше инициализации `ApplicationComponent`

### Решение

- Не использовать `subcomponent` на уровне `Application`, выделить отдельные Dagger-компоненты в gradle-модулях.
- Внутри компонента на уровне модули использовать субкомпоненты уже не проблема, не аффектит все приложение.

Тогда основной даггер-граф из `Application` вообще может не перегенерироваться при изменении в `gradle`-модулях (бывшие субкомпоненты).

### Алгоритм

1) Выявить интерфейсы, которые нужны и будут прилетать из основного (`application`) дерева Dagger.\
Найти их можно по использованию. Они инжектятся напрямую, либо используются в модулях.\
Если сразу непонятно, какие интерфейсы понадобятся, можно сделать пустой интерфейс и заполнять его по мере того, как Dagger будет сообщать о том, что не хватает.

```kotlin
interface SettingsDependencies : ComponentDependencies {

}
```

2) Выделить их в отдельный интерфейс, отнаследовав от `ComponentDependencies`:\
⚠️ Это самый сложный этап, где Dagger ругается что ему не хватает из основного графа, а вы выписываете недостающее.\
Для часто используемых общих зависимостей выделили CoreComponentDependencies.

```kotlin
interface SettingsDependencies : ComponentDependencies {

    fun activityIntentFactory(): ActivityIntentFactory

    fun context(): Context

    fun startupStorage(): StartupStorage

    fun deviceIdProvider(): DeviceIdProvider

    fun schedulersFactory(): SchedulersFactory

    fun profileInfoStorage(): ProfileInfoStorage

    fun deviceMetrics(): DeviceMetrics

    fun debugIntentFactory(): DebugIntentFactory

}
```

3) Eсли используются дополнительные модули (например, SavedLocationInteractorModule), у них могут быть готовые интерфейсы:

```kotlin
interface LocationDependencies : ComponentDependencies {

    fun locationApi(): LocationApi

    fun savedLocationStorage(): SavedLocationStorage

}
```

4) Отнаследовать ApplicationComponent от интерфейсов из прошлых шагов:

```kotlin
interface ApplicationComponent : BaseApplicationComponent,

    LocationDependencies,

    SettingsDependencies,

 ...

}
```

5) Добавить в `ComponentDependenciesModule` маппинг нового `ComponentDependencies`:

```kotlin
@Binds
@IntoMap
@ComponentDependenciesKey(SettingsDependencies::class)
abstract fun provideSettingsDependencies(component: ApplicationComponent): ComponentDependencies

@Binds
@IntoMap
@ComponentDependenciesKey(LocationDependencies::class)
abstract fun provideLocationDependencies(component: ApplicationComponent): ComponentDependencies
```

6) Изменить субкомпонент на компонент, или создать новый такого вида:

```kotlin
@PerActivity
@Component(
    dependencies = [SettingsDependencies::class, LocationDependencies::class], //Необходимые зависимости
    modules = [SettingsModule::class, SavedLocationInteractorModule::class] //Необходимые модули
)
interface SettingsComponent {

    fun inject(activity: SettingsActivity)

    @Component.Factory
    interface Builder {

        fun create(
            settingsDependencies: SettingsDependencies,
            locationDependencies: LocationDependencies,
            @BindsInstance state: Kundle?,
            @BindsInstance resources: Resources,
            @BindsInstance settingsItemsStream: PublishRelay<String>
        ): SettingsComponent
    }
}
```

7) В Activity/Fragment код изменится подобным образом:

```kotlin
import com.avito.android.di.findComponentDependencies
import com.avito.android.DaggerSettingsComponent

...

DaggerSettingsComponent.factory()
            .create(
                settingsDependencies = findComponentDependencies(),
                locationDependencies = findComponentDependencies(),
                state = savedInstanceState?.getKundle(KEY_SETTINGS_PRESENTER),
                resources = resources,
                settingsItemsStream = PublishRelay.create()
            )
            .inject(this)
```

⚠️ Важно добавить в импортах перед `SettingsComponent`  слово `Dagger` (`DaggerSettingsComponent`).
Дело в том, что это кодогенерированный класс и на этапе написания кода он недоступен.

Сбилдить и запустить. Все готово!

## Known Issues

### IDE не видит сгенерированные файлы

Компонент отображается красным, как будто его нет.

- Проверь что файлы kapt не добавлены в исключения: _Preferences > Editor > File Types > Ignore Files and Folders_

### Dagger: error.NonExistentClass cannot be provided / converted

```kotlin
error.NonExistentClass cannot be provided without an @Inject
    constructor or an @Provides-annotated method
```

```kotlin
e: MyClass.java:15: error: incompatible types: NonExistentClass cannot be converted to Annotation
    @error.NonExistentClass()
          ^
```

Dagger не видит этот класс из сгенерированного кода.\
Проверь что подключен соответствующий модуль с этим классом. 
Ошибка может врать, поэтому лучше проверить доступность всех классов, используемых в модуле.
