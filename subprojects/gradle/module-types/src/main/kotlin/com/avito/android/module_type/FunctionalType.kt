package com.avito.android.module_type

/**
 * Описывает тип модуля исходя из его содержимого
 */
public enum class FunctionalType {

    /**
     * Модуль с общими архитектурными абстракциями, которые могут использоваться по всему проекту.
     *
     * См. [:abstract](https://docs.k.avito.ru/mobile/android/architecture/modules-2/Modules/#abstract)
     */
    Abstract,

    /**
     * Модуль с интерфейсом к реализации функциональности.
     *
     * См. [:public](https://docs.k.avito.ru/mobile/android/architecture/modules-2/Modules/#public)
     */
    Public,

    /**
     * Модуль с реализацией функциональности.
     * Может использоваться как для реализации фичи, так и для реализации общей библиотеки.
     *
     * См. [:impl](https://docs.k.avito.ru/mobile/android/architecture/modules-2/Modules/#impl)
     */
    Impl,

    /**
     * Модуль с фейковой реализацией функциональности.
     *
     * См. [:fake](https://docs.k.avito.ru/mobile/android/architecture/modules-2/Modules/#fake)
     */
    Fake,

    /**
     * Модуль с реализацией функциональности.
     * Может быть подключен в качестве зависимости только к дебажной сборке.
     *
     * См. [:debug](https://docs.k.avito.ru/mobile/android/architecture/modules-2/Modules/#debug)
     */
    Debug,

    /**
     * Модуль с DI-компонентами, связывающими код из Public и Impl.
     *
     * Реализован на случай использования стратегии
     * [@MergeComponent в отдельном модуле](https://cf.avito.ru/pages/viewpage.action?pageId=261393720)
     */
    ImplWiring,

    /**
     * Модуль с DI-компонентами, связывающими код из Public и Fake.
     *
     * Реализован на случай использования стратегии
     * [@MergeComponent в отдельном модуле](https://cf.avito.ru/pages/viewpage.action?pageId=261393720)
     */
    FakeWiring,

    /**
     * Android application - модуль с плагином 'com.android.application' ('convention.kotlin-android-app')
     *
     * [Applications](https://docs.k.avito.ru/mobile/android/architecture/Modules/#apps)
     * См. [:demo](https://docs.k.avito.ru/mobile/android/architecture/modules-2/Modules/#demo)
     */
    Application,

    /**
     * Модуль с общими сущностями, для которых не целесообразно делать разделение на интерфейс и реализацию.
     * Является костылем, рекомендуется избегать.
     *
     * См. [Утилитные модули](https://docs.k.avito.ru/mobile/android/architecture/modules-2/Modules/#util)
     */
    Util,

    /**
     * Deprecated: создавайте [логические модули](https://docs.k.avito.ru/mobile/android/architecture/modules-2/Modules/).
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
     * [Features](https://docs.k.avito.ru/mobile/android/architecture/Modules/#avito-app)
     */
    @Deprecated("Feature-модули устарели, создавайте логические модули")
    Feature,

    /**
     * Deprecated: создавайте [логические модули](https://docs.k.avito.ru/mobile/android/architecture/modules-2/Modules/).
     *
     * Переиспользуемая библиотека, подключается в [Feature] модули или в другие библиотеки.
     * Пока для простоты считаем библиотеками все кроме feature модулей.
     *
     * [Modules types](https://docs.k.avito.ru/mobile/android/architecture/Modules/#types)
     */
    @Deprecated("Library-модули устарели, создавайте логические модули")
    Library,

    /**
     * Модуль с кастомными lint проверками, добавляем в lint конфигурации
     */
    Lint,

    /**
     * Модуль с dependency constraints, добавляем как platform зависимость.
     *
     * [Platform plugin](https://docs.gradle.org/current/userguide/java_platform_plugin.html)
     */
    Platform,

    /**
     * Модуль с тестовыми фикстурами для unit и instrumentation-тестов
     *
     * [test fixtures](https://docs.k.avito.ru/mobile/android/architecture/Modules/#text-fixtures)
     * См. [:test](https://docs.k.avito.ru/mobile/android/architecture/modules-2/Modules/#test)
     */
    Test,

    /**
     * Модуль с тестовыми фикстурами для unit и instrumentation-тестов для Debug модуля
     *
     * [test fixtures](https://docs.k.avito.ru/mobile/android/architecture/Modules/#text-fixtures)
     * См. [:test-debug](https://docs.k.avito.ru/mobile/android/architecture/modules-2/Modules/#test-debug)
     */
    TestDebug,

    /**
     * Модуль с тестовыми фикстурами для unit и instrumentation-тестов для Fake модуля
     *
     * [test fixtures](https://docs.k.avito.ru/mobile/android/architecture/Modules/#text-fixtures)
     * См. [:test-fake](https://docs.k.avito.ru/mobile/android/architecture/modules-2/Modules/#test-fake)
     */
    TestFake,
}
