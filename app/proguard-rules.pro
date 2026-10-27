# ===== 应用数据层（完整保留，防止 R8 移除/混淆） =====
-keep class com.tacke.benchmark.data.api.** { *; }

# com.tacke.benchmark.data.model.** 里包含 Kotlin data class。
# 早期这里是 -keep class ... { *; }，会把 <init> 与 copy$default 一并保留，
# 导致 R8 无法把 `Intrinsics.checkNotNullParameter` 内联掉，进而需要引用 kotlin.jvm.internal.Intrinsics
# 的 Kotlin 元数据（Object.getClass()），在没有 kotlin-reflect 的运行时上极易触发
# "Attempt to invoke virtual method 'java.lang.Class java.lang.Object.getClass()' on a null object reference"。
# 现在只保留字段/构造器/注解，允许 R8 正常内联空值检查。
-keepclassmembers,allowshrinking class com.tacke.benchmark.data.model.** { <fields>; }
-keepclassmembers,allowshrinking class com.tacke.benchmark.data.model.** { <init>(...); }
-keep,allowoptimization class com.tacke.benchmark.data.model.**

# Gson 用到的自定义 TypeAdapter / 反序列化器
-keepclassmembers class * implements com.google.gson.JsonDeserializer { <init>(...); }
-keepclassmembers class * implements com.google.gson.JsonSerializer { <init>(...); }

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
# 注意：kotlin-reflect 并未作为依赖打包，缺少 -dontwarn 时 R8 会误判这条规则需要引入反射，
# 反而把 Kotlin 元数据相关的类保留下来。
-dontwarn kotlin.reflect.**
-keepclassmembers class kotlin.Metadata { public <methods>; }
