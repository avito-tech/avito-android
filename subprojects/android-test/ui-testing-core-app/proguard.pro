# ==== COPY FROM ANOTHER APP TO REPRODUCE THE ISSUE ====

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
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

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

# Stack traces will be more helpful with this
-keepattributes SourceFile,LineNumberTable

-dontwarn kotlin.**
-dontwarn org.w3c.dom.events.*
# TODO: Use bundled configs - https://github.com/adjust/android_sdk/issues/147
# Правила скопированы из https://github.com/adjust/android_sdk#proguard-settings в версии от 22.07.2020

-keep class com.adjust.sdk.** { *; }
-keep class com.google.android.gms.common.ConnectionResult {
    int SUCCESS;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
    com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
    java.lang.String getId();
    boolean isLimitAdTrackingEnabled();
}
-keep public class com.android.installreferrer.** { *; }

-optimizationpasses 3

# GooglePlayServices
# Custom rules in addition to bundled rules
# They look unnecessary but it's hard to verify

-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep class javax.inject.** { *; }

-dontwarn sun.misc.Unsafe
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*, SourceFile, LineNumberTable, Exceptions, Deprecated, RuntimeVisibleAnnotations

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

-keepclassmembers,allowoptimization enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keep,allowobfuscation class com.sumsub.sns.prooface.data.* { *; }
-keep class com.sumsub.sns.core.data.model.remote.** { *; }

-keep class org.** { *; }

-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
  public static void checkExpressionValueIsNotNull(java.lang.Object, java.lang.String);
  public static void checkFieldIsNotNull(java.lang.Object, java.lang.String);
  public static void checkFieldIsNotNull(java.lang.Object, java.lang.String, java.lang.String);
  public static void checkNotNull(java.lang.Object);
  public static void checkNotNull(java.lang.Object, java.lang.String);
  public static void checkNotNullExpressionValue(java.lang.Object, java.lang.String);
  public static void checkNotNullParameter(java.lang.Object, java.lang.String);
  public static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
  public static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String);
  public static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String, java.lang.String);
  public static void throwUninitializedPropertyAccessException(java.lang.String);
}

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**

-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }

# Ugly workaround for 4.2.2 and Support library
# See: https://code.google.com/p/android/issues/detail?id=78377
-keep class !android.support.v7.internal.view.menu.* implements android.support.v4.internal.view.SupportMenu
-keep class android.support.v7.** {*;}
-keep interface android.support.v7.** { *; }
-dontwarn android.support.v7.**

# Used transitively by facebook sdk and fresco according to dependency report
# TODO: Use bundled version https://github.com/BoltsFramework/Bolts-Android/issues/158

-keep class com.parse.** { *; }
-dontwarn com.parse.**


-keep class org.eclipse.mat.** { *; }
-keep class com.squareup.leakcanary.** { *; }
-dontwarn com.squareup.leakcanary.**
-keep class com.squareup.haha.** { *; }

# JsonRpcClient does reflection on generic parameters.
# - InnerClasses is required to use Signature and
# - EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# JsonRpcClient does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

-dontwarn okhttp3.**
-dontwarn java.nio.**
-dontwarn okio.**

-keep public enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Crashlytics 2.+
-keep class com.crashlytics.** { *; }
-keep class com.crashlytics.android.**
-keepattributes SourceFile, LineNumberTable, *Annotation*

# If you are using custom exceptions, add this line so that custom exception types are skipped during obfuscation:
-keep public class * extends java.lang.Exception

# For Fabric to properly de-obfuscate your crash reports, you need to remove this line from your ProGuard config:
# -printmapping mapping.txt
-ignorewarnings
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class com.google.android.material.R$* { *; }
# TODO: find an issue about embedding proguard rules in AAR
-keep public interface android.webkit.JavascriptInterface { *; }
# See another options in the library aar

-keep public class com.facebook.imageutils.** {
   public *;
}

-keepclassmembers class com.facebook.drawee.generic.GenericDraweeHierarchy { com.facebook.drawee.drawable.ForwardingDrawable mActualImageWrapper; }
-keep class com.facebook.drawee.drawable.RoundedBitmapDrawable { java.lang.ref.WeakReference mLastBitmap; }

#---------------Begin: proguard configuration for Gson  ----------
# Source: https://github.com/google/gson/blob/master/examples/android-proguard-example/proguard.cfg
# TODO: Use bundled rules https://github.com/google/gson/issues/1559

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# R8 can decide to replace instances of types that are never instantiated with null.
# So if instances of a given class are only created through deserialization from JSON,
# R8 will not see that class as instantiated leaving it as always null.
# Here we can't use `allowobfuscation` because of enums
# With `allowobfuscation` enum fields with @SerializedName are also obfuscated
# and GSON will throw FieldNotFoundException on release build
-keepclassmembers class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
# !!! WARNING !!! It misses non-annotated fields in enum
# enum class MyEnum {
#    @SerializedName("a")
#    A, <--- keep this field (if `allowobfuscation` doesn't present in rule above)
#    B  <--- obfuscated
# }
#---------------End: proguard configuration for Gson  ----------

# Workaround for r8 bug
# https://issuetracker.google.com/issues/140851070#comment49

-keepclassmembers class * {
    void zza(com.google.android.gms.common.internal.BaseGmsClient,int,android.os.IInterface);
}

# Logs
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Path is relative to the config file
-printconfiguration "build/outputs/mapping/release/configuration.pro"

-keep class androidx.core.app.CoreComponentFactory { <init>(); }
-keep class androidx.core.content.FileProvider { <init>(); }

-keep class android.widget.Space { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.app.AlertController$RecycleListView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.view.menu.ActionMenuItemView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.view.menu.ExpandedMenuView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.view.menu.ListMenuItemView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.widget.ActionBarContainer { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.widget.ActionBarContextView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.widget.ActionBarOverlayLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.widget.ActionMenuView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.widget.ActivityChooserView$InnerLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.widget.AlertDialogLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.widget.AppCompatCheckedTextView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.widget.AppCompatEditText { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.widget.AppCompatImageButton { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.widget.AppCompatImageView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.widget.AppCompatTextView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.widget.ButtonBarLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.widget.ContentFrameLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.widget.DialogTitle { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.widget.FitWindowsFrameLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.widget.FitWindowsLinearLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.widget.LinearLayoutCompat { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.widget.SearchView { <init>(android.content.Context); }

-keep class androidx.appcompat.widget.SearchView$SearchAutoComplete { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.widget.SwitchCompat { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.widget.Toolbar { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.appcompat.widget.ViewStubCompat { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.browser.browseractions.BrowserActionsFallbackMenuView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.cardview.widget.CardView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.constraintlayout.helper.widget.Flow { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.constraintlayout.motion.widget.MotionLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.constraintlayout.widget.Barrier { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.constraintlayout.widget.ConstraintLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.constraintlayout.widget.Group { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.constraintlayout.widget.Guideline { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.coordinatorlayout.widget.CoordinatorLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.core.widget.NestedScrollView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.drawerlayout.widget.DrawerLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.fragment.app.FragmentContainerView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.legacy.widget.Space { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.percentlayout.widget.PercentRelativeLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.preference.UnPressableLinearLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.preference.internal.PreferenceImageView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.recyclerview.widget.RecyclerView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.swiperefreshlayout.widget.SwipeRefreshLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.viewpager.widget.ViewPager { <init>(android.content.Context, android.util.AttributeSet); }

-keep class androidx.viewpager2.widget.ViewPager2 { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.facebook.drawee.view.SimpleDraweeView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.exoplayer2.ui.AspectRatioFrameLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.exoplayer2.ui.PlayerView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.exoplayer2.ui.SubtitleView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.exoplayer2.ui.TrackSelectionView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.flexbox.FlexboxLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.gms.ads.formats.UnifiedNativeAdView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.gms.maps.MapView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.material.appbar.AppBarLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.material.appbar.CollapsingToolbarLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.material.appbar.MaterialToolbar { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.material.button.MaterialButton { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.material.button.MaterialButtonToggleGroup { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.material.card.MaterialCardView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.material.chip.Chip { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.material.chip.ChipGroup { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.material.datepicker.MaterialCalendarGridView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.material.floatingactionbutton.FloatingActionButton { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.material.internal.BaselineLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.material.internal.CheckableImageButton { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.material.internal.NavigationMenuItemView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.material.internal.NavigationMenuView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.material.snackbar.Snackbar$SnackbarLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.material.snackbar.SnackbarContentLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.material.tabs.TabLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.material.textfield.TextInputEditText { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.material.textfield.TextInputLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.material.textview.MaterialTextView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.material.timepicker.ChipTextInputComboView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.material.timepicker.ClockFaceView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.material.timepicker.ClockHandView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.android.material.timepicker.TimePickerView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.google.maps.android.ui.RotationLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.my.target.nativeads.views.IconAdView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.my.target.nativeads.views.MediaAdView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.my.target.nativeads.views.NativeAdContainer { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.otaliastudios.cameraview.CameraView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.wefika.flowlayout.FlowLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.yandex.mapkit.mapview.MapView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.yandex.mobile.ads.fullscreen.template.view.MediaViewContainer { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.yandex.mobile.ads.instream.view.InstreamMuteView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.yandex.mobile.ads.interstitial.template.InterstitialNativeAdView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.yandex.mobile.ads.interstitial.template.view.CallToActionView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.yandex.mobile.ads.interstitial.template.view.ColorizedRatingView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.yandex.mobile.ads.interstitial.template.view.CroppedTextView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.yandex.mobile.ads.interstitial.template.view.RoundImageView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.yandex.mobile.ads.nativeads.MediaView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.yandex.mobile.ads.nativeads.NativeAdView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.yandex.mobile.ads.rewarded.template.RewardedNativeAdView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.yandex.mobile.ads.rewarded.template.view.RewardTimerView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.yandex.mobile.ads.video.playback.view.VideoAdControlsContainer { <init>(android.content.Context, android.util.AttributeSet); }

-keep class com.yatatsu.powerwebview.PowerWebView { <init>(android.content.Context, android.util.AttributeSet); }

-keep class io.supercharge.shimmerlayout.ShimmerLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class org.webrtc.SurfaceViewRenderer { <init>(android.content.Context, android.util.AttributeSet); }

-keep class ru.rambler.libs.swipe_layout.SwipeLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class ru.serebryakovas.exradiolayout.widget.CompoundFrameLayoutRadioGroup { <init>(android.content.Context, android.util.AttributeSet); }

-keep class ru.serebryakovas.exradiolayout.widget.RadioFrameLayout { <init>(android.content.Context, android.util.AttributeSet); }

-keep class ru.tinkoff.scrollingpagerindicator.ScrollingPagerIndicator { <init>(android.content.Context, android.util.AttributeSet); }


-keep class org.webrtc.voiceengine.** {*;}
-keep class org.webrtc.** {*;}
-keepnames public class org.webrtc.voiceengine.** {*;}
-keepnames public class org.webrtc.** {*;}
-keepclasseswithmembernames class * { native <methods>; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep public class com.voximplant.sdk.* {
    public protected *;
}

-keep class avt.webrtc.voiceengine.** {*;}
-keep class avt.webrtc.** {*;}
-keepnames public class avt.webrtc.voiceengine.** {*;}
-keepnames public class avt.webrtc.** {*;}
-keepclasseswithmembernames class * { native <methods>; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Proguard rules for kotlin.serialization (https://github.com/Kotlin/kotlinx.serialization)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations

# kotlinx-serialization-json specific. Add this if you have java.lang.NoClassDefFoundError kotlinx.serialization.json.JsonObjectSerializer
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# End of content from /Users/Shared/gradle/caches/transforms-3/e287f12c239292550c682259bcfa4500/transformed/rules/lib/META-INF/proguard/retrofit2.pro
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/df36767a7e61ba65f9f0a09f77f46b79/transformed/voximplant-sdk-2.28.0/proguard.txt
-keep class org.webrtc.voiceengine.* {*;}
-keep class org.webrtc.* {*;}
-keep class org.webrtc.audio.* {*;}
-keepnames public class org.webrtc.voiceengine.* {*;}
-keepnames public class org.webrtc.* {*;}
-keepnames public class org.webrtc.audio.* {*;}
-keepclasseswithmembernames class * { native <methods>; }

-keepclassmembers enum * {
public static **[] values(); public static ** valueOf(java.lang.String);}

-keep public class com.voximplant.sdk.* {
public protected *;}
# End of content from /Users/Shared/gradle/caches/transforms-3/df36767a7e61ba65f9f0a09f77f46b79/transformed/voximplant-sdk-2.28.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/08e2781ba2f50ca1008c9aac176f80bb/transformed/sdk-1.1.3/proguard.txt
-keep class ru.uxfeedback.sdk.api.network.entities.* { *; }
-keep class ru.uxfeedback.pub.sdk.* { *; }
-dontwarn org.joda.convert.**
-dontwarn ru.serebryakovas.exradiolayout.**
-dontwarn org.chromium.net.**






# End of content from /Users/Shared/gradle/caches/transforms-3/08e2781ba2f50ca1008c9aac176f80bb/transformed/sdk-1.1.3/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/1497edeb394a6d936bdc4876d22dafd0/transformed/picasso-2.71828/proguard.txt
### OKHTTP

# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote okhttp3.internal.Platform


### OKIO

# java.nio.file.* usage which cannot be used at runtime. Animal sniffer annotation.
-dontwarn okio.Okio
# JDK 7-only method which is @hide on Android. Animal sniffer annotation.
-dontwarn okio.DeflaterSink

# End of content from /Users/Shared/gradle/caches/transforms-3/1497edeb394a6d936bdc4876d22dafd0/transformed/picasso-2.71828/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/d772086389810160884e6034437ae717/transformed/rules/lib/META-INF/proguard/okhttp3.pro
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform

# End of content from /Users/Shared/gradle/caches/transforms-3/d772086389810160884e6034437ae717/transformed/rules/lib/META-INF/proguard/okhttp3.pro
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/2894ecce90112fd0240a370e85a61e93/transformed/timber-5.0.1/proguard.txt
-dontwarn org.jetbrains.annotations.**

# End of content from /Users/Shared/gradle/caches/transforms-3/2894ecce90112fd0240a370e85a61e93/transformed/timber-5.0.1/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/15b589f326eb7257485b7d03b48ae33b/transformed/rules/lib/META-INF/com.android.tools/r8-from-1.6.0/kotlin-reflect.pro
# When editing this file, update the following files as well:
# - META-INF/com.android.tools/proguard/kotlin-reflect.pro
# - META-INF/com.android.tools/r8-upto-1.6.0/kotlin-reflect.pro
# - META-INF/proguard/kotlin-reflect.pro
# Keep Metadata annotations so they can be parsed at runtime.
-keep class kotlin.Metadata { *; }

# Keep generic signatures and annotations at runtime.
# R8 requires InnerClasses and EnclosingMethod if you keepattributes Signature.
-keepattributes InnerClasses,Signature,RuntimeVisible*Annotations,EnclosingMethod

# Don't note on API calls from different JVM versions as they're gated properly at runtime.
-dontnote kotlin.internal.PlatformImplementationsKt

# Don't note on internal APIs, as there is some class relocating that shrinkers may unnecessarily find suspicious.
-dontwarn kotlin.reflect.jvm.internal.**
# End of content from /Users/Shared/gradle/caches/transforms-3/15b589f326eb7257485b7d03b48ae33b/transformed/rules/lib/META-INF/com.android.tools/r8-from-1.6.0/kotlin-reflect.pro
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/f9e27325a45e1449a666a8bc3a0a02d7/transformed/rules/lib/META-INF/proguard/okio.pro
# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# End of content from /Users/Shared/gradle/caches/transforms-3/f9e27325a45e1449a666a8bc3a0a02d7/transformed/rules/lib/META-INF/proguard/okio.pro
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/c51d8c4b7e749603e56512e2b27ff763/transformed/uploadservice-4.5.2/proguard.txt
# Generated keep rule for Lifecycle observer adapter.
-if class net.gotev.uploadservice.observer.request.RequestObserver {
    <init>(...);
}
-keep class net.gotev.uploadservice.observer.request.RequestObserver_LifecycleAdapter {
    <init>(...);
}

# End of content from /Users/Shared/gradle/caches/transforms-3/c51d8c4b7e749603e56512e2b27ff763/transformed/uploadservice-4.5.2/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/1eea5fb49e0faf76bf2b05c65fddc88c/transformed/facebook-android-sdk-8.1.0/proguard.txt
# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepnames class com.facebook.FacebookActivity
-keepnames class com.facebook.CustomTabActivity

-keep class com.facebook.all.All

-keep public class com.android.vending.billing.IInAppBillingService {
    public static com.android.vending.billing.IInAppBillingService asInterface(android.os.IBinder);
    public android.os.Bundle getSkuDetails(int, java.lang.String, java.lang.String, android.os.Bundle);
}

# End of content from /Users/Shared/gradle/caches/transforms-3/1eea5fb49e0faf76bf2b05c65fddc88c/transformed/facebook-android-sdk-8.1.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/6366c2e4ed5b9cd5ac861f06582cbde4/transformed/rules/lib/META-INF/com.android.tools/r8-from-1.6.0/coroutines.pro
# Allow R8 to optimize away the FastServiceLoader.
# Together with ServiceLoader optimization in R8
# this results in direct instantiation when loading Dispatchers.Main
-assumenosideeffects class kotlinx.coroutines.internal.MainDispatcherLoader {
    boolean FAST_SERVICE_LOADER_ENABLED return false;
}

-assumenosideeffects class kotlinx.coroutines.internal.FastServiceLoaderKt {
    boolean ANDROID_DETECTED return true;
}

-keep class kotlinx.coroutines.android.AndroidDispatcherFactory {*;}

# Disable support for "Missing Main Dispatcher", since we always have Android main dispatcher
-assumenosideeffects class kotlinx.coroutines.internal.MainDispatchersKt {
    boolean SUPPORT_MISSING return false;
}

# Statically turn off all debugging facilities and assertions
-assumenosideeffects class kotlinx.coroutines.DebugKt {
    boolean getASSERTIONS_ENABLED() return false;
    boolean getDEBUG() return false;
    boolean getRECOVER_STACK_TRACES() return false;
}
# End of content from /Users/Shared/gradle/caches/transforms-3/6366c2e4ed5b9cd5ac861f06582cbde4/transformed/rules/lib/META-INF/com.android.tools/r8-from-1.6.0/coroutines.pro
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/490c80341e631fce5286b0dd2deadfd5/transformed/egloo-0.6.1/proguard.txt
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/yuya.tanaka/devel/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# End of content from /Users/Shared/gradle/caches/transforms-3/490c80341e631fce5286b0dd2deadfd5/transformed/egloo-0.6.1/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/358affc4e8faa96b06c4d5053b9f01e3/transformed/rules/lib/META-INF/proguard/coroutines.pro
# ServiceLoader support
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Same story for the standard library's SafeContinuation that also uses AtomicReferenceFieldUpdater
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}

# These classes are only required by kotlinx.coroutines.debug.AgentPremain, which is only loaded when
# kotlinx-coroutines-core is used as a Java agent, so these are not needed in contexts where ProGuard is used.
-dontwarn java.lang.instrument.ClassFileTransformer
-dontwarn sun.misc.SignalHandler
-dontwarn java.lang.instrument.Instrumentation
-dontwarn sun.misc.Signal

# End of content from /Users/Shared/gradle/caches/transforms-3/358affc4e8faa96b06c4d5053b9f01e3/transformed/rules/lib/META-INF/proguard/coroutines.pro
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/4083bdfabb7a298494479fec67be4975/transformed/firebase-messaging-20.2.4/proguard.txt
# Analytics library is optional.
# Access to this class is protected by try/catch(NoClassDefFoundError e)
# b/35686744 Don't fail during proguard if the class is missing from the APK.
-dontwarn com.google.android.gms.measurement.AppMeasurement*

# End of content from /Users/Shared/gradle/caches/transforms-3/4083bdfabb7a298494479fec67be4975/transformed/firebase-messaging-20.2.4/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/d9c7e6ce43159596b8d3dcb1b062c9a0/transformed/play-services-tagmanager-17.0.0/proguard.txt
# We keep all fields for every generated proto file as the runtime uses
# reflection over them that ProGuard cannot detect. Without this keep
# rule, fields may be removed that would cause runtime failures.
-keepclassmembers class * extends com.google.android.gms.internal.gtm.zzrc {
  <fields>;
}

# End of content from /Users/Shared/gradle/caches/transforms-3/d9c7e6ce43159596b8d3dcb1b062c9a0/transformed/play-services-tagmanager-17.0.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/c346214f15e58ee208562eb973681f81/transformed/play-services-tagmanager-api-17.0.0/proguard.txt
# We keep all fields for every generated proto file as the runtime uses
# reflection over them that ProGuard cannot detect. Without this keep
# rule, fields may be removed that would cause runtime failures.
-keepclassmembers class * extends com.google.android.gms.internal.gtm.zzrc {
  <fields>;
}

# End of content from /Users/Shared/gradle/caches/transforms-3/c346214f15e58ee208562eb973681f81/transformed/play-services-tagmanager-api-17.0.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/13ea00acebebc4b4417bc7f8011bca99/transformed/play-services-analytics-impl-17.0.0/proguard.txt
# We keep all fields for every generated proto file as the runtime uses
# reflection over them that ProGuard cannot detect. Without this keep
# rule, fields may be removed that would cause runtime failures.
-keepclassmembers class * extends com.google.android.gms.internal.gtm.zzrc {
  <fields>;
}

# End of content from /Users/Shared/gradle/caches/transforms-3/13ea00acebebc4b4417bc7f8011bca99/transformed/play-services-analytics-impl-17.0.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/fc7b3310336c359233c4490495fc67ec/transformed/play-services-measurement-17.4.4/proguard.txt
# We keep all fields for every generated proto file as the runtime uses
# reflection over them that ProGuard cannot detect. Without this keep
# rule, fields may be removed that would cause runtime failures.
-keepclassmembers class * extends com.google.android.gms.internal.measurement.zzib {
  <fields>;
}

# End of content from /Users/Shared/gradle/caches/transforms-3/fc7b3310336c359233c4490495fc67ec/transformed/play-services-measurement-17.4.4/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/67072386ff94a4ec5e777e42f4080935/transformed/play-services-measurement-api-17.4.4/proguard.txt
# Can be removed once we pull in a dependency on firebase-common that includes
# https://github.com/firebase/firebase-android-sdk/pull/1472/commits/856f1ca1151cdd88679bbc778892f23dfa34fc06#diff-a2ed34b5a38b4c6c686b09e54865eb48
-dontwarn com.google.auto.value.AutoValue
-dontwarn com.google.auto.value.AutoValue$Builder

# We keep all fields for every generated proto file as the runtime uses
# reflection over them that ProGuard cannot detect. Without this keep
# rule, fields may be removed that would cause runtime failures.
-keepclassmembers class * extends com.google.android.gms.internal.measurement.zzib {
  <fields>;
}

# End of content from /Users/Shared/gradle/caches/transforms-3/67072386ff94a4ec5e777e42f4080935/transformed/play-services-measurement-api-17.4.4/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/c982bf8aba06ca6a8e3adeedcda4512c/transformed/play-services-measurement-sdk-17.4.4/proguard.txt
# We keep all fields for every generated proto file as the runtime uses
# reflection over them that ProGuard cannot detect. Without this keep
# rule, fields may be removed that would cause runtime failures.
-keepclassmembers class * extends com.google.android.gms.internal.measurement.zzib {
  <fields>;
}

# End of content from /Users/Shared/gradle/caches/transforms-3/c982bf8aba06ca6a8e3adeedcda4512c/transformed/play-services-measurement-sdk-17.4.4/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/e5be6d084cdaa95d64fafad425b1bfdd/transformed/play-services-measurement-impl-17.4.4/proguard.txt
# We keep all fields for every generated proto file as the runtime uses
# reflection over them that ProGuard cannot detect. Without this keep
# rule, fields may be removed that would cause runtime failures.
-keepclassmembers class * extends com.google.android.gms.internal.measurement.zzib {
  <fields>;
}

# End of content from /Users/Shared/gradle/caches/transforms-3/e5be6d084cdaa95d64fafad425b1bfdd/transformed/play-services-measurement-impl-17.4.4/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/5fca22ba6dfa9835c023b207c3e97eec/transformed/mapkit-3.5.0-jetified/proguard.txt
-keep class com.yandex.mapkit.** { *; }

# End of content from /Users/Shared/gradle/caches/transforms-3/5fca22ba6dfa9835c023b207c3e97eec/transformed/mapkit-3.5.0-jetified/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/5e1e0e220d2ddb0174869527990ddae1/transformed/runtime-3.5.0-jetified/proguard.txt
-keep class com.yandex.maps.** { *; }
-keep class com.yandex.runtime.** { *; }

# End of content from /Users/Shared/gradle/caches/transforms-3/5e1e0e220d2ddb0174869527990ddae1/transformed/runtime-3.5.0-jetified/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/c1e3001f084fa63e89aa5eafd742fc33/transformed/play-services-ads-19.8.0/proguard.txt
-keep public class com.google.android.gms.ads.internal.ClientApi {
  <init>();
}

# We keep all fields for every generated proto file as the runtime uses
# reflection over them that ProGuard cannot detect. Without this keep
# rule, fields may be removed that would cause runtime failures.
-keepclassmembers class * extends com.google.android.gms.internal.ads.zzena {
  <fields>;
}

# Auto-generated proguard rule with obfuscated symbol
-dontwarn com.google.android.gms.ads.internal.util.zzac


# End of content from /Users/Shared/gradle/caches/transforms-3/c1e3001f084fa63e89aa5eafd742fc33/transformed/play-services-ads-19.8.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/392350aabe6ef7be484b52182a76db10/transformed/play-services-gass-19.8.0/proguard.txt
# We keep all fields for every generated proto file as the runtime uses
# reflection over them that ProGuard cannot detect. Without this keep
# rule, fields may be removed that would cause runtime failures.
-keepclassmembers class * extends com.google.android.gms.internal.ads.zzena {
  <fields>;
}

# End of content from /Users/Shared/gradle/caches/transforms-3/392350aabe6ef7be484b52182a76db10/transformed/play-services-gass-19.8.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/be97580559dd11376b5b1278f8bf0083/transformed/firebase-common-20.0.0/proguard.txt
-dontwarn com.google.firebase.platforminfo.KotlinDetector
-dontwarn com.google.auto.value.AutoValue
-dontwarn com.google.auto.value.AutoValue$Builder

# End of content from /Users/Shared/gradle/caches/transforms-3/be97580559dd11376b5b1278f8bf0083/transformed/firebase-common-20.0.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/cb94a6a3d2e34c9945ea65cc3acebbb2/transformed/face-detection-16.1.2/proguard.txt
# We keep all fields for every generated proto file as the runtime uses
# reflection over them that ProGuard cannot detect. Without this keep
# rule, fields may be removed that would cause runtime failures.
-keepclassmembers class * extends com.google.android.gms.internal.mlkit_vision_face_bundled.zzns {
  <fields>;
}

# This prevents the names of native methods from being obfuscated and prevents
# UnsatisfiedLinkErrors.
-keepclasseswithmembernames class * {
    native <methods>;
}

# End of content from /Users/Shared/gradle/caches/transforms-3/cb94a6a3d2e34c9945ea65cc3acebbb2/transformed/face-detection-16.1.2/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/bf8aac2987c249248bac266e265c8441/transformed/common-17.2.0/proguard.txt
# Annotations are implemented as attributes, so we have to explicitly keep them.
# Catch all which encompasses attributes like RuntimeVisibleParameterAnnotations
# and RuntimeVisibleTypeAnnotations
-keepattributes RuntimeVisible*Annotation*

# JNI is an entry point that's hard to keep track of, so there's
# an annotation to mark fields and methods used by native code.

# Keep the annotations that proguard needs to process.
-keep class com.google.android.apps.common.proguard.UsedBy*

# Just because native code accesses members of a class, does not mean that the
# class itself needs to be annotated - only annotate classes that are
# referenced themselves in native code.
-keep @com.google.android.apps.common.proguard.UsedBy* class * {
  <init>();
}
-keepclassmembers class * {
    @com.google.android.apps.common.proguard.UsedBy* *;
}

# End of content from /Users/Shared/gradle/caches/transforms-3/bf8aac2987c249248bac266e265c8441/transformed/common-17.2.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/d22143dad9432093bee864972fae5c32/transformed/play-services-base-17.6.0/proguard.txt
# b/35135904 Ensure that proguard will not strip the mResultGuardian.
-keepclassmembers class com.google.android.gms.common.api.internal.BasePendingResult {
  com.google.android.gms.common.api.internal.BasePendingResult$ReleasableResultGuardian mResultGuardian;
}

# End of content from /Users/Shared/gradle/caches/transforms-3/d22143dad9432093bee864972fae5c32/transformed/play-services-base-17.6.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/f193942fe428d8d4cd8671827c86812d/transformed/mytarget-sdk-5.13.4/proguard.txt
-dontwarn com.my.target.**

# handle a proguard warn on API <24
-dontwarn com.my.target.core.net.cookie.**

# tracking google advertising ID
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
    com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
    java.lang.String getId();
    boolean isLimitAdTrackingEnabled();
}
# End of content from /Users/Shared/gradle/caches/transforms-3/f193942fe428d8d4cd8671827c86812d/transformed/mytarget-sdk-5.13.4/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/5d3043ffb200feb9b6c3cbd2f509d031/transformed/mytracker-sdk-3.0.0/proguard.txt
-keep class com.my.tracker.** { *; }
-dontwarn com.my.tracker.**
-keep class com.google.android.gms.ads.identifier.** { *; }
-keep class com.android.installreferrer.** { *; }
-keep class com.android.vending.billing.** { *; }
-keep class com.android.billingclient.api.** { *; }
# End of content from /Users/Shared/gradle/caches/transforms-3/5d3043ffb200feb9b6c3cbd2f509d031/transformed/mytracker-sdk-3.0.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/c386b2951bc518f427c3bd66a0cb3770/transformed/play-services-ads-lite-19.8.0/proguard.txt
# Keep implementations of the AdMob mediation adapter interfaces. Adapters for
# third party ad networks implement these interfaces and are invoked by the
# AdMob SDK via reflection.

-keep class * implements com.google.android.gms.ads.mediation.MediationAdapter {
  public *;
}
-keep class * implements com.google.ads.mediation.MediationAdapter {
  public *;
}
-keep class * implements com.google.android.gms.ads.mediation.customevent.CustomEvent {
  public *;
}
-keep class * implements com.google.ads.mediation.customevent.CustomEvent {
  public *;
}
-keep class * extends com.google.android.gms.ads.mediation.MediationAdNetworkAdapter {
  public *;
}
-keep class * extends com.google.android.gms.ads.mediation.Adapter {
  public *;
}

# Keep classes used for offline ads created by reflection. WorkManagerUtil is
# created reflectively by callers within GMSCore and OfflineNotificationPoster
# is created reflectively by WorkManager.
-keep class com.google.android.gms.ads.internal.util.WorkManagerUtil {
  public *;
}
-keep class com.google.android.gms.ads.internal.offline.buffering.OfflineNotificationPoster {
  public *;
}
-keep class com.google.android.gms.ads.internal.offline.buffering.OfflinePingSender {
  public *;
}


# We keep all fields for every generated proto file as the runtime uses
# reflection over them that ProGuard cannot detect. Without this keep
# rule, fields may be removed that would cause runtime failures.
-keepclassmembers class * extends com.google.android.gms.internal.ads.zzena {
  <fields>;
}

# End of content from /Users/Shared/gradle/caches/transforms-3/c386b2951bc518f427c3bd66a0cb3770/transformed/play-services-ads-lite-19.8.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/8438cd6b9261bee59623b57c90ea9216/transformed/play-services-measurement-sdk-api-17.4.4/proguard.txt
# We keep all fields for every generated proto file as the runtime uses
# reflection over them that ProGuard cannot detect. Without this keep
# rule, fields may be removed that would cause runtime failures.
-keepclassmembers class * extends com.google.android.gms.internal.measurement.zzib {
  <fields>;
}

# End of content from /Users/Shared/gradle/caches/transforms-3/8438cd6b9261bee59623b57c90ea9216/transformed/play-services-measurement-sdk-api-17.4.4/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/187af0ad28a6af577e6e7f6c764d987d/transformed/play-services-measurement-base-17.4.4/proguard.txt
# We keep all fields for every generated proto file as the runtime uses
# reflection over them that ProGuard cannot detect. Without this keep
# rule, fields may be removed that would cause runtime failures.
-keepclassmembers class * extends com.google.android.gms.internal.measurement.zzib {
  <fields>;
}

# End of content from /Users/Shared/gradle/caches/transforms-3/187af0ad28a6af577e6e7f6c764d987d/transformed/play-services-measurement-base-17.4.4/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/6ad1e36d1c2247750314ac45902a9c8a/transformed/play-services-basement-17.4.0/proguard.txt
# Proguard flags for consumers of the Google Play services SDK
# https://developers.google.com/android/guides/setup#add_google_play_services_to_your_project

# Keep SafeParcelable NULL value, needed for reflection by DowngradeableSafeParcel
-keepclassmembers public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

# Needed for Parcelable/SafeParcelable classes & their creators to not get renamed, as they are
# found via reflection.
-keep class com.google.android.gms.common.internal.ReflectedParcelable
-keepnames class * implements com.google.android.gms.common.internal.ReflectedParcelable
-keepclassmembers class * implements android.os.Parcelable {
  public static final *** CREATOR;
}

# Keep the classes/members we need for client functionality.
-keep @interface android.support.annotation.Keep
-keep @android.support.annotation.Keep class *
-keepclasseswithmembers class * {
  @android.support.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
  @android.support.annotation.Keep <methods>;
}

# Keep androidX equivalent of above android.support to allow Jetification.
-keep @interface androidx.annotation.Keep
-keep @androidx.annotation.Keep class *
-keepclasseswithmembers class * {
  @androidx.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
  @androidx.annotation.Keep <methods>;
}

# Keep the names of classes/members we need for client functionality.
-keep @interface com.google.android.gms.common.annotation.KeepName
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
  @com.google.android.gms.common.annotation.KeepName *;
}

# Keep Dynamite API entry points
-keep @interface com.google.android.gms.common.util.DynamiteApi
-keep @com.google.android.gms.common.util.DynamiteApi public class * {
  public <fields>;
  public <methods>;
}

# Needed when building against pre-Marshmallow SDK.
-dontwarn android.security.NetworkSecurityPolicy

# Needed when building against Marshmallow SDK.
-dontwarn android.app.Notification

# Protobuf has references not on the Android boot classpath
-dontwarn sun.misc.Unsafe
-dontwarn libcore.io.Memory

# Internal Google annotations for generating Proguard keep rules.
-dontwarn com.google.android.apps.common.proguard.UsedBy*

# Annotations referenced by the SDK but whose definitions are contained in
# non-required dependencies.
-dontwarn javax.annotation.**
-dontwarn org.checkerframework.**

# End of content from /Users/Shared/gradle/caches/transforms-3/6ad1e36d1c2247750314ac45902a9c8a/transformed/play-services-basement-17.4.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/197dccb7bb7b2c0271a3dd0acde1ea72/transformed/material-1.4.0/proguard.txt
# Copyright (C) 2015 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# CoordinatorLayout resolves the behaviors of its child components with reflection.
-keep public class * extends androidx.coordinatorlayout.widget.CoordinatorLayout$Behavior {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>();
}

# Make sure we keep annotations for CoordinatorLayout's DefaultBehavior
-keepattributes RuntimeVisible*Annotation*

# Copyright (C) 2018 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# AppCompatViewInflater reads the viewInflaterClass theme attribute which then
# reflectively instantiates MaterialComponentsViewInflater using the no-argument
# constructor. We only need to keep this constructor and the class name if
# AppCompatViewInflater is also being kept.
-if class androidx.appcompat.app.AppCompatViewInflater
-keep class com.google.android.material.theme.MaterialComponentsViewInflater {
    <init>();
}


# End of content from /Users/Shared/gradle/caches/transforms-3/197dccb7bb7b2c0271a3dd0acde1ea72/transformed/material-1.4.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/0619f08cc2cf4b9cb21be9208a452456/transformed/preference-1.1.0/proguard.txt
# Copyright (C) 2015 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Preference objects are inflated via reflection
-keep public class androidx.preference.Preference {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keep public class * extends androidx.preference.Preference {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# End of content from /Users/Shared/gradle/caches/transforms-3/0619f08cc2cf4b9cb21be9208a452456/transformed/preference-1.1.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/dd09489b575bb7b12ac2b204115ad226/transformed/facebook-login-8.1.0/proguard.txt
# Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
#
# You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
# copy, modify, and distribute this software in source code or binary form for use
# in connection with the web services and APIs provided by Facebook.
#
# As with any software that integrates with the Facebook platform, your use of
# this software is subject to the Facebook Developer Principles and Policies
# [http://developers.facebook.com/policy/]. This copyright notice shall be
# included in all copies or substantial portions of the software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
# FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
# COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
# IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
# CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepnames class com.facebook.FacebookActivity
-keepnames class com.facebook.CustomTabActivity

-keep class com.facebook.login.Login

# End of content from /Users/Shared/gradle/caches/transforms-3/dd09489b575bb7b12ac2b204115ad226/transformed/facebook-login-8.1.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/828ca57ec866d2dca42fc93c51eca001/transformed/facebook-gamingservices-8.1.0/proguard.txt
# Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
#
# You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
# copy, modify, and distribute this software in source code or binary form for use
# in connection with the web services and APIs provided by Facebook.
#
# As with any software that integrates with the Facebook platform, your use of
# this software is subject to the Facebook Developer Principles and Policies
# [http://developers.facebook.com/policy/]. This copyright notice shall be
# included in all copies or substantial portions of the software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
# FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
# COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
# IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
# CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepnames class com.facebook.FacebookActivity
-keepnames class com.facebook.CustomTabActivity

-keep class com.facebook.gamingservices.GamingServices

# End of content from /Users/Shared/gradle/caches/transforms-3/828ca57ec866d2dca42fc93c51eca001/transformed/facebook-gamingservices-8.1.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/8dfff4cac71e7f3079f90d66c77b25f8/transformed/facebook-share-8.1.0/proguard.txt
# Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
#
# You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
# copy, modify, and distribute this software in source code or binary form for use
# in connection with the web services and APIs provided by Facebook.
#
# As with any software that integrates with the Facebook platform, your use of
# this software is subject to the Facebook Developer Principles and Policies
# [http://developers.facebook.com/policy/]. This copyright notice shall be
# included in all copies or substantial portions of the software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
# FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
# COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
# IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
# CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepnames class com.facebook.FacebookActivity
-keepnames class com.facebook.CustomTabActivity

-keep class com.facebook.share.Share

# End of content from /Users/Shared/gradle/caches/transforms-3/8dfff4cac71e7f3079f90d66c77b25f8/transformed/facebook-share-8.1.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/020f78b2fe9517272ff93be09b3c7469/transformed/facebook-common-8.1.0/proguard.txt
# Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
#
# You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
# copy, modify, and distribute this software in source code or binary form for use
# in connection with the web services and APIs provided by Facebook.
#
# As with any software that integrates with the Facebook platform, your use of
# this software is subject to the Facebook Developer Principles and Policies
# [http://developers.facebook.com/policy/]. This copyright notice shall be
# included in all copies or substantial portions of the software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
# FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
# COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
# IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
# CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepnames class com.facebook.FacebookActivity
-keepnames class com.facebook.CustomTabActivity

-keep class com.facebook.common.Common

# End of content from /Users/Shared/gradle/caches/transforms-3/020f78b2fe9517272ff93be09b3c7469/transformed/facebook-common-8.1.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/b499a8c75a097ab9d30902e057b23ce0/transformed/appcompat-1.3.1/proguard.txt
# Copyright (C) 2018 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# aapt is not able to read app::actionViewClass and app:actionProviderClass to produce proguard
# keep rules. Add a commonly used SearchView to the keep list until b/109831488 is resolved.
-keep class androidx.appcompat.widget.SearchView { <init>(...); }

# Never inline methods, but allow shrinking and obfuscation.
-keepclassmembernames,allowobfuscation,allowshrinking class androidx.appcompat.widget.AppCompatTextViewAutoSizeHelper$Impl* {
  <methods>;
}

# End of content from /Users/Shared/gradle/caches/transforms-3/b499a8c75a097ab9d30902e057b23ce0/transformed/appcompat-1.3.1/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/93e2bf57774b0e8ccb5c3fc2bba0b45c/transformed/exoplayer-ui-2.14.2/proguard.txt
# Proguard rules specific to the UI module.

# Constructor method accessed via reflection in TrackSelectionDialogBuilder
-dontnote androidx.appcompat.app.AlertDialog.Builder
-keepclassmembers class androidx.appcompat.app.AlertDialog$Builder {
  <init>(android.content.Context, int);
  public android.content.Context getContext();
  public androidx.appcompat.app.AlertDialog$Builder setTitle(java.lang.CharSequence);
  public androidx.appcompat.app.AlertDialog$Builder setView(android.view.View);
  public androidx.appcompat.app.AlertDialog$Builder setPositiveButton(int, android.content.DialogInterface$OnClickListener);
  public androidx.appcompat.app.AlertDialog$Builder setNegativeButton(int, android.content.DialogInterface$OnClickListener);
  public androidx.appcompat.app.AlertDialog create();
}
# Equivalent methods needed when the library is de-jetified.
-dontnote android.support.v7.app.AlertDialog.Builder
-keepclassmembers class android.support.v7.app.AlertDialog$Builder {
  <init>(android.content.Context, int);
  public android.content.Context getContext();
  public android.support.v7.app.AlertDialog$Builder setTitle(java.lang.CharSequence);
  public android.support.v7.app.AlertDialog$Builder setView(android.view.View);
  public android.support.v7.app.AlertDialog$Builder setPositiveButton(int, android.content.DialogInterface$OnClickListener);
  public android.support.v7.app.AlertDialog$Builder setNegativeButton(int, android.content.DialogInterface$OnClickListener);
  public android.support.v7.app.AlertDialog create();
}

# Don't warn about checkerframework and Kotlin annotations
-dontwarn org.checkerframework.**
-dontwarn kotlin.annotations.jvm.**
-dontwarn javax.annotation.**

# End of content from /Users/Shared/gradle/caches/transforms-3/93e2bf57774b0e8ccb5c3fc2bba0b45c/transformed/exoplayer-ui-2.14.2/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/5a3c65843fb4c013a1289549ccfa578b/transformed/recyclerview-1.1.0/proguard.txt
# Copyright (C) 2015 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# When layoutManager xml attribute is used, RecyclerView inflates
#LayoutManagers' constructors using reflection.
-keep public class * extends androidx.recyclerview.widget.RecyclerView$LayoutManager {
    public <init>(android.content.Context, android.util.AttributeSet, int, int);
    public <init>();
}

-keepclassmembers class androidx.recyclerview.widget.RecyclerView {
    public void suppressLayout(boolean);
    public boolean isLayoutSuppressed();
}
# End of content from /Users/Shared/gradle/caches/transforms-3/5a3c65843fb4c013a1289549ccfa578b/transformed/recyclerview-1.1.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/5b6337a9b8af2667799ca6bc5d9171fb/transformed/work-runtime-2.6.0/proguard.txt
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.InputMerger
# Keep all constructors on ListenableWorker, Worker (also marked with @Keep)
-keep public class * extends androidx.work.ListenableWorker {
    public <init>(...);
}
# We need to keep WorkerParameters for the ListenableWorker constructor
-keep class androidx.work.WorkerParameters

# End of content from /Users/Shared/gradle/caches/transforms-3/5b6337a9b8af2667799ca6bc5d9171fb/transformed/work-runtime-2.6.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/1e5686581873fdf7e44e75f71061e6e8/transformed/glide-4.11.0/proguard.txt
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Uncomment for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule

# End of content from /Users/Shared/gradle/caches/transforms-3/1e5686581873fdf7e44e75f71061e6e8/transformed/glide-4.11.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/11fc4091647153a4d3b3051d150929be/transformed/fragment-1.3.6/proguard.txt
# Copyright (C) 2020 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# The default FragmentFactory creates Fragment instances using reflection
-if public class ** extends androidx.fragment.app.Fragment
-keepclasseswithmembers,allowobfuscation public class <1> {
    public <init>();
}

# End of content from /Users/Shared/gradle/caches/transforms-3/11fc4091647153a4d3b3051d150929be/transformed/fragment-1.3.6/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/9a1c53d5df95286558d66eea15ce234b/transformed/android-job-1.4.2/proguard.txt
-dontwarn com.evernote.android.job.gcm.**
-dontwarn com.evernote.android.job.GcmAvailableHelper
-dontwarn com.evernote.android.job.work.**
-dontwarn com.evernote.android.job.WorkManagerAvailableHelper

-keep public class com.evernote.android.job.v21.PlatformJobService
-keep public class com.evernote.android.job.v14.PlatformAlarmService
-keep public class com.evernote.android.job.v14.PlatformAlarmReceiver
-keep public class com.evernote.android.job.JobBootReceiver
-keep public class com.evernote.android.job.JobRescheduleService
-keep public class com.evernote.android.job.gcm.PlatformGcmService
-keep public class com.evernote.android.job.work.PlatformWorker

# End of content from /Users/Shared/gradle/caches/transforms-3/9a1c53d5df95286558d66eea15ce234b/transformed/android-job-1.4.2/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/7739736080bcc51b11470381da1fe09e/transformed/facebook-applinks-8.1.0/proguard.txt
# Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
#
# You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
# copy, modify, and distribute this software in source code or binary form for use
# in connection with the web services and APIs provided by Facebook.
#
# As with any software that integrates with the Facebook platform, your use of
# this software is subject to the Facebook Developer Principles and Policies
# [http://developers.facebook.com/policy/]. This copyright notice shall be
# included in all copies or substantial portions of the software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
# FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
# COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
# IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
# CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepnames class com.facebook.FacebookActivity
-keepnames class com.facebook.CustomTabActivity

-keep class com.facebook.applinks.AppLinks

# End of content from /Users/Shared/gradle/caches/transforms-3/7739736080bcc51b11470381da1fe09e/transformed/facebook-applinks-8.1.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/4999500a4531d4d0b585d15b3020b960/transformed/facebook-messenger-8.1.0/proguard.txt
# Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
#
# You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
# copy, modify, and distribute this software in source code or binary form for use
# in connection with the web services and APIs provided by Facebook.
#
# As with any software that integrates with the Facebook platform, your use of
# this software is subject to the Facebook Developer Principles and Policies
# [http://developers.facebook.com/policy/]. This copyright notice shall be
# included in all copies or substantial portions of the software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
# FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
# COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
# IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
# CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepnames class com.facebook.messaging.analytics.reliability.ReliabilityInfo
-keepnames class com.facebook.messaging.analytics.reliability.ReliabilityInfo.Outcome

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepnames class com.facebook.FacebookActivity
-keepnames class com.facebook.CustomTabActivity

-keep class com.facebook.messenger.Messenger

# End of content from /Users/Shared/gradle/caches/transforms-3/4999500a4531d4d0b585d15b3020b960/transformed/facebook-messenger-8.1.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/979de0ff6063c904589ef77e231d8909/transformed/facebook-core-8.1.0/proguard.txt
# Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
#
# You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
# copy, modify, and distribute this software in source code or binary form for use
# in connection with the web services and APIs provided by Facebook.
#
# As with any software that integrates with the Facebook platform, your use of
# this software is subject to the Facebook Developer Principles and Policies
# [http://developers.facebook.com/policy/]. This copyright notice shall be
# included in all copies or substantial portions of the software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
# FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
# COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
# IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
# CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepclassmembers,allowoptimization enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepnames class com.facebook.FacebookActivity
-keepnames class com.facebook.CustomTabActivity

-keepnames class com.android.installreferrer.api.InstallReferrerClient
-keepnames class com.android.installreferrer.api.InstallReferrerStateListener
-keepnames class com.android.installreferrer.api.ReferrerDetails

-keep class com.facebook.core.Core

# keep class names and method names used by reflection by InAppPurchaseEventManager
-keep public class com.android.vending.billing.IInAppBillingService {
    public <methods>;
}
-keep public class com.android.vending.billing.IInAppBillingService$Stub {
    public <methods>;
}
# End of content from /Users/Shared/gradle/caches/transforms-3/979de0ff6063c904589ef77e231d8909/transformed/facebook-core-8.1.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/bf75627a50a7fbdfb42aae20c7d3fc5b/transformed/coordinatorlayout-1.1.0/proguard.txt
# Copyright (C) 2016 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# CoordinatorLayout resolves the behaviors of its child components with reflection.
-keep public class * extends androidx.coordinatorlayout.widget.CoordinatorLayout$Behavior {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>();
}

# Make sure we keep annotations for CoordinatorLayout's DefaultBehavior and ViewPager's DecorView
-keepattributes *Annotation*

# End of content from /Users/Shared/gradle/caches/transforms-3/bf75627a50a7fbdfb42aae20c7d3fc5b/transformed/coordinatorlayout-1.1.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/72ad67ab96214e12efeb5d1da89d9126/transformed/media-1.2.1/proguard.txt
# Copyright (C) 2017 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Prevent Parcelable objects from being removed or renamed.
-keep class android.support.v4.media.** implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Prevent Parcelable objects from being removed or renamed.
-keep class androidx.media.** implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
# End of content from /Users/Shared/gradle/caches/transforms-3/72ad67ab96214e12efeb5d1da89d9126/transformed/media-1.2.1/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/75a07758f877ec9f0ca0810f194f43f0/transformed/transition-1.2.0/proguard.txt
# Copyright (C) 2017 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Keep a field in transition that is used to keep a reference to weakly-referenced object
-keepclassmembers class androidx.transition.ChangeBounds$* extends android.animation.AnimatorListenerAdapter {
  androidx.transition.ChangeBounds$ViewBounds mViewBounds;
}

# End of content from /Users/Shared/gradle/caches/transforms-3/75a07758f877ec9f0ca0810f194f43f0/transformed/transition-1.2.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/78308b9eb79a431b278c60f0b5efb87a/transformed/vectordrawable-animated-1.1.0/proguard.txt
# Copyright (C) 2016 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# keep setters in VectorDrawables so that animations can still work.
-keepclassmembers class androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$* {
   void set*(***);
   *** get*();
}

# End of content from /Users/Shared/gradle/caches/transforms-3/78308b9eb79a431b278c60f0b5efb87a/transformed/vectordrawable-animated-1.1.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/857f21ab91ba6d0b1afd8837cf069b37/transformed/core-1.6.0/proguard.txt
# Never inline methods, but allow shrinking and obfuscation.
-keepclassmembernames,allowobfuscation,allowshrinking class androidx.core.view.ViewCompat$Api* {
  <methods>;
}
-keepclassmembernames,allowobfuscation,allowshrinking class androidx.core.view.WindowInsetsCompat$*Impl* {
  <methods>;
}
-keepclassmembernames,allowobfuscation,allowshrinking class androidx.core.app.NotificationCompat$*$Api*Impl {
  <methods>;
}
-keepclassmembernames,allowobfuscation,allowshrinking class androidx.core.os.UserHandleCompat$Api*Impl {
  <methods>;
}

# End of content from /Users/Shared/gradle/caches/transforms-3/471ff97f1c714d50b9bb830e8e9efc80/transformed/fingerprint-2.4.1/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/f9f0c1b0eace5478b1f8a80a2c1dfdc3/transformed/lifecycle-process-2.2.0/proguard.txt
# this rule is need to work properly when app is compiled with api 28, see b/142778206
-keepclassmembers class * extends androidx.lifecycle.EmptyActivityLifecycleCallbacks { *; }
# End of content from /Users/Shared/gradle/caches/transforms-3/f9f0c1b0eace5478b1f8a80a2c1dfdc3/transformed/lifecycle-process-2.2.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/25e92cf78b26c61a94ba769e8b34f834/transformed/lifecycle-runtime-2.3.1/proguard.txt
-keepattributes *Annotation*

-keepclassmembers enum androidx.lifecycle.Lifecycle$Event {
    <fields>;
}

-keep !interface * implements androidx.lifecycle.LifecycleObserver {
}

-keep class * implements androidx.lifecycle.GeneratedAdapter {
    <init>(...);
}

-keepclassmembers class ** {
    @androidx.lifecycle.OnLifecycleEvent *;
}

# this rule is need to work properly when app is compiled with api 28, see b/142778206
# Also this rule prevents registerIn from being inlined.
-keepclassmembers class androidx.lifecycle.ReportFragment$LifecycleCallbacks { *; }
# End of content from /Users/Shared/gradle/caches/transforms-3/25e92cf78b26c61a94ba769e8b34f834/transformed/lifecycle-runtime-2.3.1/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/3a6d4dc5f6e141aa324fab1369c32403/transformed/lifecycle-viewmodel-savedstate-2.3.1/proguard.txt
-keepclassmembers,allowobfuscation class * extends androidx.lifecycle.ViewModel {
    <init>(androidx.lifecycle.SavedStateHandle);
}

-keepclassmembers,allowobfuscation class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application,androidx.lifecycle.SavedStateHandle);
}

# End of content from /Users/Shared/gradle/caches/transforms-3/3a6d4dc5f6e141aa324fab1369c32403/transformed/lifecycle-viewmodel-savedstate-2.3.1/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/d09942b94c8f1480b7edfda3793cde2c/transformed/savedstate-1.1.0/proguard.txt
# Copyright (C) 2019 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

-keepclassmembers,allowobfuscation class * implements androidx.savedstate.SavedStateRegistry$AutoRecreated {
    <init>();
}

# End of content from /Users/Shared/gradle/caches/transforms-3/d09942b94c8f1480b7edfda3793cde2c/transformed/savedstate-1.1.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/ae5133d7963f4b39d0506afc2f91b5b1/transformed/versionedparcelable-1.1.1/proguard.txt
-keep class * implements androidx.versionedparcelable.VersionedParcelable
-keep public class android.support.**Parcelizer { *; }
-keep public class androidx.**Parcelizer { *; }
-keep public class androidx.versionedparcelable.ParcelImpl

# End of content from /Users/Shared/gradle/caches/transforms-3/ae5133d7963f4b39d0506afc2f91b5b1/transformed/versionedparcelable-1.1.1/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/19158b0c732214c456a3326615aa91e4/transformed/mobileads-4.3.0/proguard.txt
-keep class com.yandex.mobile.ads.** { *; }
-dontwarn com.yandex.mobile.ads.**

-keepattributes *Annotation*

# End of content from /Users/Shared/gradle/caches/transforms-3/19158b0c732214c456a3326615aa91e4/transformed/mobileads-4.3.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/01b7f35b0001740cccdf5b8e9399a890/transformed/exoplayer-core-2.14.2/proguard.txt
# Proguard rules specific to the core module.

# Constant folding for resource integers may mean that a resource passed to this method appears to be unused. Keep the method to prevent this from happening.
-keepclassmembers class com.google.android.exoplayer2.upstream.RawResourceDataSource {
  public static android.net.Uri buildRawResourceUri(int);
}

# Constructors accessed via reflection in DefaultRenderersFactory
-dontnote com.google.android.exoplayer2.ext.vp9.LibvpxVideoRenderer
-keepclassmembers class com.google.android.exoplayer2.ext.vp9.LibvpxVideoRenderer {
  <init>(long, android.os.Handler, com.google.android.exoplayer2.video.VideoRendererEventListener, int);
}
-dontnote com.google.android.exoplayer2.ext.av1.Libgav1VideoRenderer
-keepclassmembers class com.google.android.exoplayer2.ext.av1.Libgav1VideoRenderer {
  <init>(long, android.os.Handler, com.google.android.exoplayer2.video.VideoRendererEventListener, int);
}
-dontnote com.google.android.exoplayer2.ext.opus.LibopusAudioRenderer
-keepclassmembers class com.google.android.exoplayer2.ext.opus.LibopusAudioRenderer {
  <init>(android.os.Handler, com.google.android.exoplayer2.audio.AudioRendererEventListener, com.google.android.exoplayer2.audio.AudioSink);
}
-dontnote com.google.android.exoplayer2.ext.flac.LibflacAudioRenderer
-keepclassmembers class com.google.android.exoplayer2.ext.flac.LibflacAudioRenderer {
  <init>(android.os.Handler, com.google.android.exoplayer2.audio.AudioRendererEventListener, com.google.android.exoplayer2.audio.AudioSink);
}
-dontnote com.google.android.exoplayer2.ext.ffmpeg.FfmpegAudioRenderer
-keepclassmembers class com.google.android.exoplayer2.ext.ffmpeg.FfmpegAudioRenderer {
  <init>(android.os.Handler, com.google.android.exoplayer2.audio.AudioRendererEventListener, com.google.android.exoplayer2.audio.AudioSink);
}

# Constructors accessed via reflection in DefaultDataSource
-dontnote com.google.android.exoplayer2.ext.rtmp.RtmpDataSource
-keepclassmembers class com.google.android.exoplayer2.ext.rtmp.RtmpDataSource {
  <init>();
}

# Constructors accessed via reflection in DefaultDownloaderFactory
-dontnote com.google.android.exoplayer2.source.dash.offline.DashDownloader
-keepclassmembers class com.google.android.exoplayer2.source.dash.offline.DashDownloader {
  <init>(com.google.android.exoplayer2.MediaItem, com.google.android.exoplayer2.upstream.cache.CacheDataSource$Factory, java.util.concurrent.Executor);
}
-dontnote com.google.android.exoplayer2.source.hls.offline.HlsDownloader
-keepclassmembers class com.google.android.exoplayer2.source.hls.offline.HlsDownloader {
  <init>(com.google.android.exoplayer2.MediaItem, com.google.android.exoplayer2.upstream.cache.CacheDataSource$Factory, java.util.concurrent.Executor);
}
-dontnote com.google.android.exoplayer2.source.smoothstreaming.offline.SsDownloader
-keepclassmembers class com.google.android.exoplayer2.source.smoothstreaming.offline.SsDownloader {
  <init>(com.google.android.exoplayer2.MediaItem, com.google.android.exoplayer2.upstream.cache.CacheDataSource$Factory, java.util.concurrent.Executor);
}

# Constructors accessed via reflection in DefaultMediaSourceFactory
-dontnote com.google.android.exoplayer2.source.dash.DashMediaSource$Factory
-keepclasseswithmembers class com.google.android.exoplayer2.source.dash.DashMediaSource$Factory {
  <init>(com.google.android.exoplayer2.upstream.DataSource$Factory);
}
-dontnote com.google.android.exoplayer2.source.hls.HlsMediaSource$Factory
-keepclasseswithmembers class com.google.android.exoplayer2.source.hls.HlsMediaSource$Factory {
  <init>(com.google.android.exoplayer2.upstream.DataSource$Factory);
}
-dontnote com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource$Factory
-keepclasseswithmembers class com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource$Factory {
  <init>(com.google.android.exoplayer2.upstream.DataSource$Factory);
}
-dontnote com.google.android.exoplayer2.source.rtsp.RtspMediaSource$Factory
-keepclasseswithmembers class com.google.android.exoplayer2.source.rtsp.RtspMediaSource$Factory {
  <init>();
}

# End of content from /Users/Shared/gradle/caches/transforms-3/01b7f35b0001740cccdf5b8e9399a890/transformed/exoplayer-core-2.14.2/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/a6d236cc8056d6fc4df6ddea76ec5e49/transformed/room-runtime-2.2.5/proguard.txt
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# End of content from /Users/Shared/gradle/caches/transforms-3/a6d236cc8056d6fc4df6ddea76ec5e49/transformed/room-runtime-2.2.5/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/919e949cc19e3f6091da671b7fd1272f/transformed/lifecycle-viewmodel-2.3.1/proguard.txt
-keepclassmembers,allowobfuscation class * extends androidx.lifecycle.ViewModel {
    <init>();
}

-keepclassmembers,allowobfuscation class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}

# End of content from /Users/Shared/gradle/caches/transforms-3/919e949cc19e3f6091da671b7fd1272f/transformed/lifecycle-viewmodel-2.3.1/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/6db6872cb65e3446e29b9668f719e3bf/transformed/startup-runtime-1.0.0/proguard.txt
# This Proguard rule ensures that ComponentInitializers are are neither shrunk nor obfuscated.
# This is because they are discovered and instantiated during application initialization.
-keep class * extends androidx.startup.Initializer {
    # Keep the public no-argument constructor while allowing other methods to be optimized.
    <init>();
}

-assumenosideeffects class androidx.startup.StartupLogger

# End of content from /Users/Shared/gradle/caches/transforms-3/6db6872cb65e3446e29b9668f719e3bf/transformed/startup-runtime-1.0.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/c510a7781e265d76fe9c9c13e77cdd9c/transformed/exoplayer-extractor-2.14.2/proguard.txt
# Proguard rules specific to the extractor module.

# Methods accessed via reflection in DefaultExtractorsFactory
-dontnote com.google.android.exoplayer2.ext.flac.FlacExtractor
-keepclassmembers class com.google.android.exoplayer2.ext.flac.FlacExtractor {
  <init>(int);
}
-dontnote com.google.android.exoplayer2.ext.flac.FlacLibrary
-keepclassmembers class com.google.android.exoplayer2.ext.flac.FlacLibrary {
  public static boolean isAvailable();
}

# Don't warn about checkerframework and Kotlin annotations
-dontwarn org.checkerframework.**
-dontwarn kotlin.annotations.jvm.**
-dontwarn javax.annotation.**

# End of content from /Users/Shared/gradle/caches/transforms-3/c510a7781e265d76fe9c9c13e77cdd9c/transformed/exoplayer-extractor-2.14.2/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/da423f84356323fe00f72b175bf22b7f/transformed/exoplayer-common-2.14.2/proguard.txt
# Proguard rules specific to the common module.

# Don't warn about checkerframework and Kotlin annotations
-dontwarn org.checkerframework.**
-dontwarn kotlin.annotations.jvm.**
-dontwarn javax.annotation.**

# From https://github.com/google/guava/wiki/UsingProGuardWithGuava
-dontwarn java.lang.ClassValue
-dontwarn java.lang.SafeVarargs
-dontwarn javax.lang.model.element.Modifier
-dontwarn sun.misc.Unsafe

# Don't warn about Guava's compile-only dependencies.
# These lines are needed for ProGuard but not R8.
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.j2objc.annotations.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Workaround for https://issuetracker.google.com/issues/112297269
# This is needed for ProGuard but not R8.
-keepclassmembernames class com.google.common.base.Function { *; }

# End of content from /Users/Shared/gradle/caches/transforms-3/da423f84356323fe00f72b175bf22b7f/transformed/exoplayer-common-2.14.2/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/b086d66dc3d36b14626988bec99cbff9/transformed/transport-backend-cct-3.0.0/proguard.txt
-dontwarn com.google.auto.value.AutoValue
-dontwarn com.google.auto.value.AutoValue$Builder

# End of content from /Users/Shared/gradle/caches/transforms-3/b086d66dc3d36b14626988bec99cbff9/transformed/transport-backend-cct-3.0.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/8f4ac33e75402e3a0ce29a67bd2a2033/transformed/transport-runtime-3.0.0/proguard.txt
-dontwarn com.google.auto.value.AutoValue
-dontwarn com.google.auto.value.AutoValue$Builder

# End of content from /Users/Shared/gradle/caches/transforms-3/8f4ac33e75402e3a0ce29a67bd2a2033/transformed/transport-runtime-3.0.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/a758ce19759c35b8fbb2e989632aa22a/transformed/transport-api-3.0.0/proguard.txt
-dontwarn com.google.auto.value.AutoValue
-dontwarn com.google.auto.value.AutoValue$Builder

# End of content from /Users/Shared/gradle/caches/transforms-3/a758ce19759c35b8fbb2e989632aa22a/transformed/transport-api-3.0.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/28559d97111b217dbcb585b17e9c7550/transformed/firebase-components-17.0.0/proguard.txt
-dontwarn com.google.firebase.components.Component$Instantiation
-dontwarn com.google.firebase.components.Component$ComponentType

-keep class * implements com.google.firebase.components.ComponentRegistrar

# End of content from /Users/Shared/gradle/caches/transforms-3/28559d97111b217dbcb585b17e9c7550/transformed/firebase-components-17.0.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/dcb81caa9a69d4b695452410a3e9fa5e/transformed/firebase-encoders-json-18.0.0/proguard.txt

# End of content from /Users/Shared/gradle/caches/transforms-3/dcb81caa9a69d4b695452410a3e9fa5e/transformed/firebase-encoders-json-18.0.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/869729aed458c217bc15f594c2717cf8/transformed/rules/lib/META-INF/proguard/androidx-annotations.pro
-keep,allowobfuscation @interface androidx.annotation.Keep
-keep @androidx.annotation.Keep class * {*;}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}

-keepclassmembers,allowobfuscation class * {
  @androidx.annotation.DoNotInline <methods>;
}

# End of content from /Users/Shared/gradle/caches/transforms-3/869729aed458c217bc15f594c2717cf8/transformed/rules/lib/META-INF/proguard/androidx-annotations.pro
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/7006028b678018357b257536069fc340/transformed/annotation-experimental-1.1.0/proguard.txt
# Copyright (C) 2020 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Ignore missing Kotlin meta-annotations so that this library can be used
# without adding a compileOnly dependency on the Kotlin standard library.
-dontwarn kotlin.Deprecated
-dontwarn kotlin.Metadata
-dontwarn kotlin.ReplaceWith
-dontwarn kotlin.annotation.AnnotationRetention
-dontwarn kotlin.annotation.AnnotationTarget
-dontwarn kotlin.annotation.Retention
-dontwarn kotlin.annotation.Target

# End of content from /Users/Shared/gradle/caches/transforms-3/7006028b678018357b257536069fc340/transformed/annotation-experimental-1.1.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/ca7672f91087d92271d45795f382ebed/transformed/fbcore-2.3.0/proguard.txt
# Keep our interfaces so they can be used by other ProGuard rules.
# See http://sourceforge.net/p/proguard/bugs/466/
-keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip
-keep,allowobfuscation @interface com.facebook.soloader.DoNotOptimize

# Do not strip any method/class that is annotated with @DoNotStrip
-keep @com.facebook.common.internal.DoNotStrip class *
-keepclassmembers class * {
    @com.facebook.common.internal.DoNotStrip *;
}

# Do not strip any method/class that is annotated with @DoNotOptimize
-keep @com.facebook.soloader.DoNotOptimize class *
-keepclassmembers class * {
    @com.facebook.soloader.DoNotOptimize *;
}

# Keep native methods
-keepclassmembers class * {
    native <methods>;
}

# Do not strip SoLoader class and init method
-keep public class com.facebook.soloader.SoLoader {
    public static void init(android.content.Context, int);
}

-dontwarn okio.**
-dontwarn com.squareup.okhttp.**
-dontwarn okhttp3.**
-dontwarn javax.annotation.**
-dontwarn com.android.volley.toolbox.**
-dontwarn com.facebook.infer.**

# End of content from /Users/Shared/gradle/caches/transforms-3/ca7672f91087d92271d45795f382ebed/transformed/fbcore-2.3.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/d23111dea81827c9c7850a085f2a24e2/transformed/flexbox-2.0.1/proguard.txt
#
# Copyright 2016 Google Inc. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# The FlexboxLayoutManager may be set from a layout xml, in that situation the RecyclerView
# tries to instantiate the layout manager using reflection.
# This is to prevent the layout manager from being obfuscated.
-keepnames public class com.google.android.flexbox.FlexboxLayoutManager
# End of content from /Users/Shared/gradle/caches/transforms-3/d23111dea81827c9c7850a085f2a24e2/transformed/flexbox-2.0.1/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/ae575da058e1fd34ea35857496ac0dd7/transformed/securedtouch-sdk-4.2.0/proguard.txt
-keepclassmembers class com.securedtouch.model.*.** { <fields>; }
-keepclassmembers class com.securedtouch.model.* { <fields>; }

# End of content from /Users/Shared/gradle/caches/transforms-3/ae575da058e1fd34ea35857496ac0dd7/transformed/securedtouch-sdk-4.2.0/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/ecfabfd721bf0e7b73ff2e42d065ec4b/transformed/rules/lib/META-INF/proguard/rxjava2.pro
-dontwarn java.util.concurrent.Flow*
# End of content from /Users/Shared/gradle/caches/transforms-3/ecfabfd721bf0e7b73ff2e42d065ec4b/transformed/rules/lib/META-INF/proguard/rxjava2.pro
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/d02b9e5d45ce73dc6649c847c4379518/transformed/mobmetricalib-3.20.1/proguard.txt
-dontwarn com.yandex.metrica.**
-keeppackagenames com.yandex.metrica*

-keep class com.yandex.metrica.impl.ob.** { *; }
-dontwarn com.yandex.metrica.impl.ob.**
-keep class com.yandex.metrica.impl.ac.** { *; }
-dontwarn com.yandex.metrica.impl.ac.**
-keep class com.android.installreferrer.api.* { *; }
-dontwarn com.android.installreferrer.api.*
-keep class com.google.protobuf.nano.ym.* {
    *;
}

-keep class com.android.installreferrer.api.* {
    *;
}

-keep class kotlin.KotlinVersion {
    *;
}

# Important Yandex Metrica classes
-keep class com.yandex.metrica.CounterConfiguration
-keep public class com.yandex.metrica.MetricaService
-keep public interface com.yandex.metrica.IMetricaService
-keep public class com.yandex.metrica.MetricaEventHandler
-keep public class com.yandex.metrica.PreloadInfoContentProvider

-keep public class com.yandex.metrica.ConfigurationService {
    public <methods>;
    public <init>();
}

-keep public class com.yandex.metrica.ConfigurationJobService {
    public <methods>;
    public <init>();
}

# Yandex Metrica API
-keep public class com.yandex.metrica.YandexMetrica {
	public <methods>;
}

-keep public class com.yandex.metrica.YandexMetricaDefaultValues* {
    public static final *;
}

-keep public interface com.yandex.metrica.IReporter {
    public <methods>;
}

-keep public class com.yandex.metrica.DeferredDeeplinkParametersListener** {
    *;
}

-keep public class com.yandex.metrica.DeferredDeeplinkListener** {
    *;
}

-keep public class com.yandex.metrica.AppMetricaDeviceIDListener** {
    *;
}

-keep public class com.yandex.metrica.PreloadInfo* {
    public <methods>;
}

-keep public class com.yandex.metrica.profile.* {
    public <methods>;
}

-keep public class com.yandex.metrica.YandexMetricaConfig* {
    public <methods>;
    public <fields>;
}

-keep public class com.yandex.metrica.ReporterConfig* {
    public <methods>;
    public <fields>;
}

-keep public enum com.yandex.metrica.profile.GenderAttribute$Gender {
    *;
}

-keep public class com.yandex.metrica.Revenue* {
    public <methods>;
    public <fields>;
}

-keepclasseswithmembernames class com.yandex.metrica.impl.ac.NativeCrashesHelper {
    native <methods>;
}

-keep public class com.yandex.metrica.ecommerce.* {
    public <methods>;
}

-keep public class com.yandex.metrica.IIdentifierCallback** {
    *;
}

-keep public class com.yandex.metrica.IParamsCallback** {
    *;
}

# Bridge
-keep class com.yandex.metrica.p {
    <methods>;
}

-keep public interface com.yandex.metrica.p$Ucc {
    *;
}

-keep class com.yandex.metrica.h {
    <methods>;
}

-keep public class com.yandex.metrica.impl.interact.* {
    public *;
}

-keepclasseswithmembernames class com.yandex.metrica.impl.ac.NativeCrashesHelper {
    native <methods>;
}

-keep class com.yandex.metrica.uiaccessor.FragmentLifecycleCallback {
    public <methods>;
}

-keepattributes *Annotation*

# End of content from /Users/Shared/gradle/caches/transforms-3/d02b9e5d45ce73dc6649c847c4379518/transformed/mobmetricalib-3.20.1/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/44035eeaf1ab4c8915574f43ab26aa89/transformed/rules/lib/META-INF/proguard/rxjava3.pro
-dontwarn java.util.concurrent.Flow*
# End of content from /Users/Shared/gradle/caches/transforms-3/44035eeaf1ab4c8915574f43ab26aa89/transformed/rules/lib/META-INF/proguard/rxjava3.pro
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/e3c790a015fafb7a6fab8797ecc18b4e/transformed/core-1.0.2/proguard.txt

# End of content from /Users/Shared/gradle/caches/transforms-3/e3c790a015fafb7a6fab8797ecc18b4e/transformed/core-1.0.2/proguard.txt
# The proguard configuration file for the following section is /Users/Shared/gradle/caches/transforms-3/7fca7363566e87101cb928ba7d465728/transformed/volley-1.2.1/proguard.txt
# Prevent Proguard from inlining methods that are intentionally extracted to ensure locals have a
# constrained liveness scope by the GC. This is needed to avoid keeping previous request references
# alive for an indeterminate amount of time. See also https://github.com/google/volley/issues/114
-keepclassmembers,allowshrinking,allowobfuscation class com.android.volley.NetworkDispatcher {
    void processRequest();
}
-keepclassmembers,allowshrinking,allowobfuscation class com.android.volley.CacheDispatcher {
    void processRequest();
}

# End of content from /Users/Shared/gradle/caches/transforms-3/7fca7363566e87101cb928ba7d465728/transformed/volley-1.2.1/proguard.txt
# The proguard configuration file for the following section is <unknown>
-ignorewarnings
# End of content from <unknown>
