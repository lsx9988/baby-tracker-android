# Room
-keep class * extends androidx.room.RoomDatabase { <init>(); }

# Keep data entities
-keep class com.babytracker.app.data.local.entity.** { *; }
