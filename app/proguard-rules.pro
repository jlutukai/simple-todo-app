# ====================================
# Debugging - Preserve line numbers
# ====================================
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ====================================
# Kotlin Serialization
# ====================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep @Serializable classes
-keep,includedescriptorclasses class com.lutukai.simpletodoapp.**$$serializer { *; }
-keepclassmembers class com.lutukai.simpletodoapp.** {
    *** Companion;
}
-keepclasseswithmembers class com.lutukai.simpletodoapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ====================================
# Coroutines
# ====================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ====================================
# R8 Full Mode Compatibility
# ====================================
-keepattributes Signature
-keep class kotlin.Metadata { *; }
