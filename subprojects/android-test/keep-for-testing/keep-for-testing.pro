# https://avito-tech.github.io/avito-android/docs/test/testminimized/

# TODO: Move to src/main/resources/META-INF/proguard - MBS-8473 (see blockers)

-keep class com.avito.android.test.KeepForTesting

-keep,allowobfuscation class * {
    @com.avito.android.test.KeepForTesting <fields>;
    @com.avito.android.test.KeepForTesting <methods>;
}

-keep class com.avito.android.test.KeepSyntheticConstructorsForTesting

-keep,allowobfuscation @com.avito.android.test.KeepSyntheticConstructorsForTesting class * {
    synthetic <init>(...);
}
