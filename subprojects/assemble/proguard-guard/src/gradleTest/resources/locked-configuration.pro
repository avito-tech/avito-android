-optimizations !class/merging/*,!code/simplification/arithmetic,!code/simplification/cast,!field/*
-optimizationpasses 5
-allowaccessmodification
-dontusemixedcaseclassnames
-keepattributes AnnotationDefault,EnclosingMethod,InnerClasses,RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeVisibleTypeAnnotations,Signature
-verbose
-dontnote android.net.http.**,android.support.**,androidx.**,com.android.vending.licensing.ILicensingService,com.google.android.vending.licensing.ILicensingService,com.google.vending.licensing.ILicensingService,java.lang.invoke.**,org.apache.http.**
-dontwarn android.support.**,android.util.FloatMath,androidx.**
-ignorewarnings
-keepclassmembers class * {
    @android.webkit.JavascriptInterface
    <methods>;
}
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}
-keepclassmembers class * extends android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers public class * extends android.view.View {
    *** get*();
    void set*(***);
}
-keepclassmembers enum  * {
    public static ** valueOf(java.lang.String);
    public static **[] values();
}
-keepclasseswithmembers class * {
    @android.support.annotation.Keep
    <methods>;
}
-keepclasseswithmembers class * {
    @android.support.annotation.Keep
    <init>(...);
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep
    <methods>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep
    <init>(...);
}
-keepclasseswithmembers class * {
    @com.avito.android.jsonrpc.annotations.*
    <methods>;
}
-keepclasseswithmembers class * {
    @android.support.annotation.Keep
    <fields>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep
    <fields>;
}
-keepclasseswithmembers,includedescriptorclasses,allowshrinking class * {
    native <methods>;
}
-keep class android.support.annotation.Keep
-keep class androidx.annotation.Keep
-keep class com.avito.security.** {
    <fields>;
    <methods>;
}
-keep @android.support.annotation.Keep class * {
    <fields>;
    <methods>;
}
-keep @androidx.annotation.Keep class * {
    <fields>;
    <methods>;
}
-keep public class com.android.vending.licensing.ILicensingService
-keep public class com.google.android.vending.licensing.ILicensingService
-keep public class com.google.vending.licensing.ILicensingService
