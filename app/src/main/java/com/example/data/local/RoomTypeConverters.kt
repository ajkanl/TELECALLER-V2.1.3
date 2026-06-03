package com.example.data.local

import androidx.room.TypeConverter
import java.util.Date

/**
 * Custom Room TypeConverters to convert unsupported data types
 * like Java package/util Date to SQLite-compatible Long timestamps.
 */
class RoomTypeConverters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
