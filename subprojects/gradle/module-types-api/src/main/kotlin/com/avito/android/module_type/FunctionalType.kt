package com.avito.android.module_type

/**
 * Описывает тип модуля исходя из его содержимого
 */
public enum class FunctionalType {

    @Deprecated("См. https://links.k.avito.ru/android-modules-2/#abstract")
    Abstract,

    /**
     * Модуль с интерфейсом к реализации функциональности и фейковой реализацией для демо-приложений.
     *
     * См. [:public](https://links.k.avito.ru/android-modules-2/#public)
     */
    Public,

    /**
     * Модуль с реализацией функциональности.
     * Может использоваться как для реализации фичи, так и для реализации общей библиотеки.
     *
     * См. [:impl](https://links.k.avito.ru/android-modules-2/#impl)
     */
    Impl,

    @Deprecated("Отказываемся в рамках TDR https://links.k.avito.ru/android-tdr-no-fake")
    Fake,

    /**
     * Модуль с реализацией функциональности.
     * Может быть подключен в качестве зависимости только к дебажной сборке.
     *
     * См. [:debug](https://links.k.avito.ru/android-modules-2/#debug)
     */
    Debug,

    /**
     * Модуль с DI-компонентами, связывающими код из Public и Impl.
     *
     * Реализован на случай использования стратегии
     * [@MergeComponent в отдельном модуле](https://cf.avito.ru/pages/viewpage.action?pageId=261393720)
     */
    @Deprecated("Стратегия @MergeComponent в отдельном модуле не используется, используется @ContributesSubcomponent")
    ImplWiring,

    /**
     * Модуль с DI-компонентами, связывающими код из Public и Fake.
     *
     * Реализован на случай использования стратегии
     * [@MergeComponent в отдельном модуле](https://cf.avito.ru/pages/viewpage.action?pageId=261393720)
     */
    @Deprecated("Стратегия @MergeComponent в отдельном модуле не используется, используется @ContributesSubcomponent")
    FakeWiring,

    /**
     * Модуль приложения, предназначенного для конечного пользователя.
     */
    UserApp,

    /**
     * Модуль демонстрационного приложения, используемого для разработки и тестирования.
     *
     * См. [:demo](https://links.k.avito.ru/android-modules-2/#demo)
     */
    DemoApp,

    /**
     * Модуль с общими сущностями, для которых не целесообразно делать разделение на интерфейс и реализацию.
     * Является костылем, рекомендуется избегать.
     *
     * См. [Утилитные модули](https://links.k.avito.ru/android-modules-2/#util)
     */
    Util,

    /**
     * Deprecated: создавайте [логические модули](https://links.k.avito.ru/android-modules-2).
     *
     * "Feature" модули: обособленная функциональность приложения,
     * с которой взаимодействуем опосредованно, через навигацию.
     *
     * Как правило это отдельный "экран": поиск, мессенджер, карточка объявления и т.п.
     *
     * Рассмотрим отличие от [Library] на примере профиля.
     * Если могу открыть по диплинку и посмотреть\отредактировать профиль - это фича.
     * Если это набор классов для получения информации \ редактирования профиля - это библиотека, ее используем в фичах.
     *
     * [Features](https://links.k.avito.ru/android-modules-1/#avito-app)
     */
    @Deprecated("Feature-модули устарели, создавайте логические модули")
    Feature,

    /**
     * Deprecated: создавайте [логические модули](https://links.k.avito.ru/android-modules-2).
     *
     * Переиспользуемая библиотека, подключается в [Feature] модули или в другие библиотеки.
     * Пока для простоты считаем библиотеками все кроме feature модулей.
     *
     * [Modules types](https://links.k.avito.ru/android-modules-1/#types)
     */
    @Deprecated("Library-модули устарели, создавайте логические модули")
    Library,

    /**
     * Модуль с кастомными lint проверками, добавляем в lint конфигурации
     */
    Lint,

    /**
     * Модуль с кастомным detekt кодом, добавляем в detekt конфигурации
     */
    Detekt,

    /**
     * Модуль c ksp, kapt кодом
     */
    CodeGenerators,

    /**
     * Модуль с dependency constraints, добавляем как platform зависимость.
     *
     * [Platform plugin](https://docs.gradle.org/current/userguide/java_platform_plugin.html)
     */
    Platform,

    /**
     * Модуль с тестовыми фикстурами для unit и instrumentation-тестов
     *
     * [test fixtures](https://links.k.avito.ru/android-modules-1/#text-fixtures)
     * См. [:test](https://links.k.avito.ru/android-modules-2/#test)
     */
    Test,

    /**
     * Модуль с тестовыми фикстурами для unit и instrumentation-тестов для Debug модуля
     *
     * [test fixtures](https://links.k.avito.ru/android-modules-1/#text-fixtures)
     * См. [:test-debug](https://links.k.avito.ru/android-modules-2/#test-debug)
     */
    TestDebug,

    @Deprecated("Заменяем на TestPublic в рамках TDR https://links.k.avito.ru/android-tdr-no-fake")
    TestFake,

    /**
     * Модуль с тестовыми фикстурами для unit и instrumentation-тестов для Public-модуля
     *
     * [test fixtures](https://links.k.avito.ru/android-modules-1/#text-fixtures)
     * См. [:test-public](https://links.k.avito.ru/android-modules-2/#test-public)
     */
    TestPublic,
}
