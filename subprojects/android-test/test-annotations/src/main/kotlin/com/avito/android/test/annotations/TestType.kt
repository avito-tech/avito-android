package com.avito.android.test.annotations

//todo move to report module
/**
 * Kind.UI_COMPONENT
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class UIComponentTest

/**
 * Kind.E2E
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class E2ETest

/**
 * Kind.INTEGRATION
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class IntegrationTest

/**
 * Kind.MANUAL
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class ManualTest

/**
 * Kind.UI_COMPONENT_STUB
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class UIComponentStub

/**
 * Kind.E2E_STUB
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class E2EStub

/**
 * Kind.UNIT
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class UnitTest

//todo move to performance modules
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class PerformanceFunctionalTest

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class PerformanceComponentTest

//todo move to screenshot modules
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class ScreenshotTest

@Deprecated("2020.3.2; use @UIComponentTest", ReplaceWith("@UIComponentTest"))
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class ComponentTest

@Deprecated("2020.3.2; use @UIComponentTest", ReplaceWith("@UIComponentTest"))
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class PublishTest

@Deprecated("2020.3.2; use @UIComponentTest", ReplaceWith("@UIComponentTest"))
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class MessengerTest

@Deprecated("2020.3.2; use @E2ETest", ReplaceWith("@E2ETest"))
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class FunctionalTest

@Deprecated("2020.3.2; use @IntegrationTest", ReplaceWith("@IntegrationTest"))
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class InstrumentationUnitTest
