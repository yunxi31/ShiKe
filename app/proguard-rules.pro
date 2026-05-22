# Pomodoro Alert Custom ProGuard Rules

# ----------------------------------------------------
# 基础混淆配置
# ----------------------------------------------------
-keepattributes Signature, InnerClasses, EnclosingMethod, SourceFile, LineNumberTable
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault

# ----------------------------------------------------
# Room 数据库模型与 DAO 保留
# ----------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-keep class * implements androidx.room.RoomDatabase$Callback
-keep class * extends androidx.room.RoomDatabase$Callback
-keep class com.pomodoroalert.data.** { *; }

# ----------------------------------------------------
# Retrofit & OkHttp & Gson 数据实体保留
# ----------------------------------------------------
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes *Annotation*
-keepclassmembers class * {
    @retrofit2.http.** <methods>;
}

# ----------------------------------------------------
# Hilt / Dependency Injection
# ----------------------------------------------------
-keep class * implements dagger.internal.DoubleCheck
-keep class * implements javax.inject.Provider
-keep class * extends android.app.Application
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider
