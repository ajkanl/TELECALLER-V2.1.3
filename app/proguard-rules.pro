# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Preserve line numbers and source file names for production crash logs (obfuscated)
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,Deprecated,*Annotation*,Synthetic

# General optimizations and warnings silencing
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe

# =========================================================================
# 1. Android Room & SQLite Keep Rules
# =========================================================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class * implements androidx.room.RoomOpenHelper

# Keep database initializers and reflection accessors
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}

# Room Paging integration preservation
-dontwarn androidx.room.paging.**

# =========================================================================
# 2. SQLCipher / Zetetic Encryption Rules (Required for Encrypted Database)
# =========================================================================
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-dontwarn net.sqlcipher.**

# =========================================================================
# 3. Firebase (Auth, Firestore, Storage) Serialization & Models
# =========================================================================
# Keep all Firebase class APIs
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Preserve annotations and classes mapped by Firestore Reflection
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}

# Keep local data entity and domain model POJOs to prevent JSON mapping failures
-keep class com.example.data.model.** { *; }
-keep class com.example.domain.model.** { *; }
-keep class com.example.data.entity.** { *; }
-keep class com.example.data.local.entity.** { *; }
-keep class com.example.data.local.dto.** { *; }

# =========================================================================
# 4. Hilt & Dagger Dependency Injection Rules
# =========================================================================
# Prevent injection mapping reflection errors
-keep class * extends dagger.hilt.internal.GeneratedComponent
-keep class * implements dagger.hilt.internal.GeneratedComponent
-keep class * extends dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories$InternalViewModelFactory

-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}

# Keep Hilt viewmodels from being fully stripped/obfuscated
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * {
    <init>(...);
}

# Keep all modules since they provide bindings
-keep @dagger.Module class * { *; }

# Keep standard androidx lifecycle ViewModels so factory creation works
-keep public class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

-dontwarn dagger.hilt.**
-dontwarn com.google.common.**
