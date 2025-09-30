# Keep all application classes
-keep class com.internetguard.pro.** { *; }

# Keep Room Database
-keep class androidx.room.** { *; }
-keep class com.internetguard.pro.data.** { *; }

# Keep Services
-keep class com.internetguard.pro.services.** { *; }

# Keep AI Engine
-keep class com.internetguard.pro.ai.** { *; }

# Keep UI Components
-keep class com.internetguard.pro.ui.** { *; }

# Keep Security Components
-keep class com.internetguard.pro.security.** { *; }

# Keep Network Components
-keep class com.internetguard.pro.network.** { *; }

# Keep Utils
-keep class com.internetguard.pro.utils.** { *; }

# Keep Gamification
-keep class com.internetguard.pro.gamification.** { *; }

# Keep Rules Engine
-keep class com.internetguard.pro.rules.** { *; }

# Keep Voice Control
-keep class com.internetguard.pro.voice.** { *; }

# Keep Profiles
-keep class com.internetguard.pro.profiles.** { *; }

# SQLCipher (if used)
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-dontwarn net.sqlcipher.**

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Keep Fragments
-keep class * extends androidx.fragment.app.Fragment { *; }

# Keep Adapters
-keep class * extends androidx.recyclerview.widget.RecyclerView$Adapter { *; }

# Keep all enums
-keepclassmembers enum * { *; }

# Keep Parcelables
-keep class * implements android.os.Parcelable { *; }

# Keep Serializable
-keep class * implements java.io.Serializable { *; }

# Security rules
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Remove debug information
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Obfuscate for security (removed -dontobfuscate)
-printmapping mapping.txt
