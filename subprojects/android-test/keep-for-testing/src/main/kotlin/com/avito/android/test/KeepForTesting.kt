package com.avito.android.test

//see TestMinimized.md

/**
 * keep,allowobfuscation class * {
 *     @com.avito.android.test.KeepForTesting <fields>;
 *     @com.avito.android.test.KeepForTesting <methods>;
 * }
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class KeepForTesting

/**
 * -keep,allowobfuscation @com.avito.android.test.KeepSyntheticConstructorsForTesting class * {
 *     synthetic <init>(...);
 * }
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class KeepSyntheticConstructorsForTesting
