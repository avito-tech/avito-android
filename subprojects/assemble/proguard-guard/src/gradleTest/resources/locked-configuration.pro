# The proguard configuration file for the following section is /private/var/folders/k8/dmdh3l614170yl4klcmvdd080000gn/T/junit2560000168188006963/app/build/intermediates/default_proguard_files/global/proguard-android-optimize.txt-7.2.1
# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html
#
# Starting with version 2.2 of the Android plugin for Gradle, this file is distributed together with
# the plugin and unpacked at build-time. The files in $ANDROID_HOME are no longer maintained and
# will be ignored by new version of the Android plugin for Gradle.

# Optimizations: If you don't want to optimize, use the proguard-android.txt configuration file
# instead of this one, which turns off the optimization flags.
# Adding optimization introduces certain risks, since for example not all optimizations performed by
# ProGuard works on all versions of Dalvik.  The following flags turn off various optimizations
# known to have issues, but the list may not be complete or up to date. (The "arithmetic"
# optimization can be used if you are only targeting Android 2.0 or later.)  Make sure you test
# thoroughly if you go this route.
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Preserve some attributes that may be required for reflection.
-keepattributes AnnotationDefault,
                EnclosingMethod,
                InnerClasses,
                RuntimeVisibleAnnotations,
                RuntimeVisibleParameterAnnotations,
                RuntimeVisibleTypeAnnotations,
                Signature

-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService
-keep public class com.google.android.vending.licensing.ILicensingService
-dontnote com.android.vending.licensing.ILicensingService
-dontnote com.google.vending.licensing.ILicensingService
-dontnote com.google.android.vending.licensing.ILicensingService

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# Keep setters in Views so that animations can still work.
-keepclassmembers public class * extends android.view.View {
    void set*(***);
    *** get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick.
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Preserve annotated Javascript interface methods.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# The support libraries contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version. We know about them, and they are safe.
-dontnote android.support.**
-dontnote androidx.**
-dontwarn android.support.**
-dontwarn androidx.**

# This class is deprecated, but remains for backward compatibility.
-dontwarn android.util.FloatMath

# Understand the @Keep support annotation.
-keep class android.support.annotation.Keep
-keep class androidx.annotation.Keep

-keep @android.support.annotation.Keep class * {*;}
-keep @androidx.annotation.Keep class * {*;}

-keepclasseswithmembers class * {
    @android.support.annotation.Keep <methods>;
}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}

-keepclasseswithmembers class * {
    @android.support.annotation.Keep <fields>;
}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}

-keepclasseswithmembers class * {
    @android.support.annotation.Keep <init>(...);
}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}

# These classes are duplicated between android.jar and org.apache.http.legacy.jar.
-dontnote org.apache.http.**
-dontnote android.net.http.**

# These classes are duplicated between android.jar and core-lambda-stubs.jar.
-dontnote java.lang.invoke.**

# End of content from /private/var/folders/k8/dmdh3l614170yl4klcmvdd080000gn/T/junit2560000168188006963/app/build/intermediates/default_proguard_files/global/proguard-android-optimize.txt-7.2.1
# The proguard configuration file for the following section is /private/var/folders/k8/dmdh3l614170yl4klcmvdd080000gn/T/junit2560000168188006963/app/proguard-rules.pro
-printconfiguration "merged-config.pro"
-keep class com.avito.security.** { *; }
# End of content from /private/var/folders/k8/dmdh3l614170yl4klcmvdd080000gn/T/junit2560000168188006963/app/proguard-rules.pro
# The proguard configuration file for the following section is /private/var/folders/k8/dmdh3l614170yl4klcmvdd080000gn/T/junit2560000168188006963/app/build/intermediates/aapt_proguard_file/release/aapt_rules.txt

# End of content from /private/var/folders/k8/dmdh3l614170yl4klcmvdd080000gn/T/junit2560000168188006963/app/build/intermediates/aapt_proguard_file/release/aapt_rules.txt
# The proguard configuration file for the following section is /private/var/folders/k8/dmdh3l614170yl4klcmvdd080000gn/T/junit2560000168188006963/lib/build/intermediates/consumer_proguard_dir/release/lib0/proguard.txt
-keepclasseswithmembers class * {
    @com.avito.android.jsonrpc.annotations.* <methods>;
}
# End of content from /private/var/folders/k8/dmdh3l614170yl4klcmvdd080000gn/T/junit2560000168188006963/lib/build/intermediates/consumer_proguard_dir/release/lib0/proguard.txt
# The proguard configuration file for the following section is <unknown>
-ignorewarnings
# End of content from <unknown>