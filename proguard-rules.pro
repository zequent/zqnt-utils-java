-dontwarn
-dontoptimize
-dontpreverify

# Quarkus reflection
-keep class io.quarkus.** { *; }

# CDI
-keep class jakarta.inject.** { *; }
-keep class jakarta.enterprise.** { *; }

# REST
-keep class jakarta.ws.rs.** { *; }

# application classes
-keep class org.example.** { *; }

# Public licensing API consumed by framework services
-keep class com.zqnt.utils.licensing.** { *; }

# main entrypoint
-keep class * {
    public static void main(java.lang.String[]);
}

# remove debug info
-renamesourcefileattribute SourceFile
-keepattributes Exceptions
