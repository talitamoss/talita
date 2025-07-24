# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep our main application class
-keep public class com.core.talita.AppMain { *; }

# Keep all data model classes
-keep class com.core.talita.PersonalData { *; }
-keep class com.core.talita.PersonalDataInterface { *; }
-keep class com.core.talita.PersonalDataType { *; }
-keep class com.core.talita.EncryptedData { *; }

# Keep plugin system classes
-keep class com.core.talita.plugins.** { *; }
-keep interface com.core.talita.api.** { *; }
-keep class * implements com.core.talita.api.DataCollectorPlugin { *; }

# Keep all classes that might be loaded dynamically
-keep class com.core.talita.collectors.** { *; }

# OSMDroid rules
-keep class org.osmdroid.** { *; }
-keep class org.metalev.multitouch.controller.** { *; }
-dontwarn org.osmdroid.**

# MPAndroidChart rules
-keep class com.github.mikephil.charting.** { *; }
-keep class com.github.mikephil.charting.data.** { *; }
-dontwarn com.github.mikephil.charting.data.realm.**

# Dagger rules
-dontwarn com.google.errorprone.annotations.**
-keep class dagger.* { *; }
-keep class javax.inject.* { *; }
-keep class * extends dagger.internal.Binding
-keep class * extends dagger.internal.ModuleAdapter
-keep class * extends dagger.internal.StaticInjection

# Keep annotations
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# ZXing (QR code) rules
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.** { *; }

# Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# JSON parsing
-keepattributes Signature
-keep class org.json.** { *; }

# SQLite database
-keep class * extends android.database.sqlite.SQLiteOpenHelper { *; }

# Encryption/Security classes
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }
-keep class android.security.** { *; }

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# WebView
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
    public *;
}

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Keep file provider paths
-keep class androidx.core.content.FileProvider { *; }

# Keep R class and its members
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Crashlytics (if you add it later)
#-keep class com.crashlytics.** { *; }
#-dontwarn com.crashlytics.**
