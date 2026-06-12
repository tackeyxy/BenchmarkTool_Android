# 保留 Gson 序列化模型类
-keep class com.tacke.benchmark.data.model.** { <fields>; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn sun.misc.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler

# Retrofit - 保留接口方法
-keepclassmembers interface * {
    @retrofit2.http.* <methods>;
}
