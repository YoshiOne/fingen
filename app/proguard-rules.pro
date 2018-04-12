# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Leonid\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
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

#-keep class !android.support.v7.internal.view.menu.**,** {*;}
#-dontwarn
#-ignorewarnings
#-dontshrink

#Butterknife
-keepattributes EnclosingMethod
# Retain generated class which implement ViewBinder.
-keep public class * implements butterknife.internal.ViewBinder { public <init>(); }
# Prevent obfuscation of types which use ButterKnife annotations since the simple name
# is used to reflectively look up the generated ViewBinder.
-keep class butterknife.*
-keepclasseswithmembernames class * { @butterknife.* <methods>; }
-keepclasseswithmembernames class * { @butterknife.* <fields>; }
-dontwarn butterknife.internal.*
-keep class **$$ViewBinder { *; }

#Billing
-keep class com.android.vending.billing.**
-assumenosideeffects class org.solovyev.android.checkout.Billing {
    public static void debug(...);
    public static void warning(...);
    public static void error(...);
}
-assumenosideeffects class org.solovyev.android.checkout.Check {
    static *;
}
-keep class com.android.vending.billing.**
-keepattributes *Annotation*
-keep public class com.android.vending.licensing.ILicensingService

#MP Android chart
-keep class com.github.mikephil.charting.** { *; }
-dontwarn io.realm.**

-dontwarn android.databinding.**
-keep class android.databinding.** { *; }

#OpenCSV
-dontwarn au.com.bytecode.opencsv.bean.**
-keep class au.com.bytecode.opencsv.bean.** { *; }
-dontwarn java.beans.**
-keep class java.beans.** { *; }
-dontwarn javax.annotation.**
-keep class javax.annotation.** { *; }
-dontwarn javax.naming.**
-keep class javax.naming.** { *; }

#Eventbus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

#Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

#Dropbox OkHttp and Servlet optional dependencies
-dontwarn okio.**
-dontwarn okhttp3.**
-dontwarn com.google.appengine.**
-dontwarn javax.servlet.**
-dontwarn com.squareup.okhttp.**

#Retrofit 2
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

#evernote.android.job
-dontwarn com.evernote.android.job.gcm.**
-dontwarn com.evernote.android.job.GcmAvailableHelper
-keep public class com.evernote.android.job.v21.PlatformJobService
-keep public class com.evernote.android.job.v14.PlatformAlarmService
-keep public class com.evernote.android.job.v14.PlatformAlarmReceiver
-keep public class com.evernote.android.job.JobBootReceiver
-keep public class com.evernote.android.job.JobRescheduleService