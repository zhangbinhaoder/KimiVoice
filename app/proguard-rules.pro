# KimiVoice ProGuard Rules
# 优化后的混淆规则，保护代码安全性

# 保留行号信息（便于调试）
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 保留注解
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

# 保留所有native方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留自定义View
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 保留Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Gson规则
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# 保留项目中的数据类（用于JSON解析）
-keep class com.example.kimivoice.NetworkUtils$** { *; }

# OkHttp规则
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class okio.** { *; }

# Timber日志
-dontwarn timber.log.**
-keep class timber.log.** { *; }

# 保留项目核心类
-keep class com.example.kimivoice.KimiConstants { *; }
-keep class com.example.kimivoice.SecurityUtils { *; }
-keep class com.example.kimivoice.AudioUtils { *; }
-keep class com.example.kimivoice.NetworkUtils { *; }

# 保留Service和BroadcastReceiver
-keep class com.example.kimivoice.ListenService { *; }
-keep class com.example.kimivoice.CmdReceiver { *; }

# 优化选项
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# 移除日志（Release版本）
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# 保留崩溃信息
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile