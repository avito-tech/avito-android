package com.avito.android.module_type

/**
 * Describes a module based on its contents.
 */
public enum class FunctionalType {

    @Deprecated("See https://links.k.avito.ru/android-modules-2/#abstract")
    Abstract,

    /**
     * A module that exposes an interface to a feature implementation and provides a fake implementation
     * for demo applications.
     *
     * See [:public](https://links.k.avito.ru/android-modules-2/#public).
     */
    Public,

    /**
     * A module that implements functionality.
     * It can implement either a feature or a shared library.
     *
     * See [:impl](https://links.k.avito.ru/android-modules-2/#impl).
     */
    Impl,

    @Deprecated("Deprecated by TDR https://links.k.avito.ru/android-tdr-no-fake")
    Fake,

    /**
     * A module that implements functionality.
     * It can only be added as a dependency of a debug build.
     *
     * See [:debug](https://links.k.avito.ru/android-modules-2/#debug).
     */
    Debug,

    /**
     * A module with DI components that connect code from Public and Impl modules.
     *
     * Supports the strategy of placing @MergeComponent in a separate module.
     */
    @Deprecated("The separate @MergeComponent module strategy is no longer used; use @ContributesSubcomponent")
    ImplWiring,

    /**
     * A module with DI components that connect code from Public and Fake modules.
     *
     * Supports the strategy of placing @MergeComponent in a separate module.
     */
    @Deprecated("The separate @MergeComponent module strategy is no longer used; use @ContributesSubcomponent")
    FakeWiring,

    /**
     * An application module intended for end users.
     */
    UserApp,

    /**
     * A demo application module used for development and testing.
     *
     * See [:demo](https://links.k.avito.ru/android-modules-2/#demo).
     */
    DemoApp,

    /**
     * A module with shared entities for which separating interface and implementation is impractical.
     * This is a workaround and should be avoided.
     *
     * See [utility modules](https://links.k.avito.ru/android-modules-2/#util).
     */
    Util,

    /**
     * Deprecated: create [logical modules](https://links.k.avito.ru/android-modules-2) instead.
     *
     * A Feature module contains a self-contained part of the application that is accessed indirectly
     * through navigation.
     *
     * It usually represents a separate screen, such as search, messenger, or an item details page.
     *
     * For example, a profile that can be opened by a deep link and viewed or edited is a feature.
     * A set of classes for retrieving or editing profile data is a library used by features.
     *
     * [Features](https://links.k.avito.ru/android-modules-1/#avito-app)
     */
    @Deprecated("Feature modules are deprecated; create logical modules instead")
    Feature,

    /**
     * Deprecated: create [logical modules](https://links.k.avito.ru/android-modules-2) instead.
     *
     * A reusable library added to [Feature] modules or other libraries.
     * For simplicity, every module other than a feature module is considered a library.
     *
     * [Modules types](https://links.k.avito.ru/android-modules-1/#types)
     */
    @Deprecated("Library modules are deprecated; create logical modules instead")
    Library,

    /**
     * A module with custom lint checks added to lint configurations.
     */
    Lint,

    /**
     * A module with custom detekt code added to detekt configurations.
     */
    Detekt,

    /**
     * A module with KSP or KAPT code.
     */
    CodeGenerators,

    /**
     * A module with dependency constraints added as a platform dependency.
     *
     * [Platform plugin](https://docs.gradle.org/current/userguide/java_platform_plugin.html)
     */
    Platform,

    /**
     * A module that logically groups other modules.
     * It is typically created by platform teams to address modularity concerns rather than by users
     * of the platform.
     */
    Composition,

    /**
     * A module with test fixtures for unit and instrumentation tests.
     *
     * [test fixtures](https://links.k.avito.ru/android-modules-1/#text-fixtures)
     * See [:test](https://links.k.avito.ru/android-modules-2/#test).
     */
    Test,

    /**
     * A module with unit and instrumentation test fixtures for a Debug module.
     *
     * [test fixtures](https://links.k.avito.ru/android-modules-1/#text-fixtures)
     * See [:test-debug](https://links.k.avito.ru/android-modules-2/#test-debug).
     */
    TestDebug,

    @Deprecated("Replaced by TestPublic according to TDR https://links.k.avito.ru/android-tdr-no-fake")
    TestFake,

    /**
     * A module with unit and instrumentation test fixtures for a Public module.
     *
     * [test fixtures](https://links.k.avito.ru/android-modules-1/#text-fixtures)
     * See [:test-public](https://links.k.avito.ru/android-modules-2/#test-public).
     */
    TestPublic,
}
