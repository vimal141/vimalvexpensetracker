# General rules
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Data Models - Keep them to avoid issues with Room or any serialization
-keep class cashvexa.expensetracker.budget.money.data.model.** { *; }

# Room
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep class androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity
-keep class * extends androidx.room.Dao

# Koin (Dependency Injection)
-keep class io.insertkoin.** { *; }
-keepclassmembers class * {
    @org.koin.core.annotation.KoinInternalApi *;
}

# Lottie
-keep class com.airbnb.lottie.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

# ViewBinding
-keep class cashvexa.expensetracker.budget.money.databinding.** { *; }
-flattenpackagehierarchy
