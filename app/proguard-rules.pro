# ===== 应用数据层（完整保留，防止 R8 移除/混淆） =====
-keep class com.tacke.benchmark.data.api.** { *; }
-keep class com.tacke.benchmark.data.model.** { *; }
-keep class com.tacke.benchmark.data.repository.** { *; }

# ===== Retrofit 官方规则 =====
# Retrofit 接口方法参数名用于反射构建请求
-keep,allowobfuscation,allowshrinking class retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking interface retrofit2.Callback
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# 保留 Retrofit suspend 函数的 Continuation 参数
-keepclassmembers class * {
    public *** *(kotlin.coroutines.Continuation);
}

# 保留 Retrofit 接口方法的参数名
-keepclassmembers interface * {
    @retrofit2.http.* <methods>;
}
-keepclassmembers,allowshrinking class * {
    @retrofit2.http.* <methods>;
}

# ===== 保留注解和签名 =====
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepattributes *Annotation*
-keepattributes Exceptions

# ===== Gson 规则 =====
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keep class com.google.gson.reflect.TypeToken { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ===== OkHttp & Okio =====
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ===== Kotlin & Coroutines =====
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler
-keep class kotlinx.coroutines.** { *; }

# Kotlin 反射支持（Gson + Kotlin data class 需要）
-keep class kotlin.reflect.** { *; }
-dontwarn kotlin.reflect.**
