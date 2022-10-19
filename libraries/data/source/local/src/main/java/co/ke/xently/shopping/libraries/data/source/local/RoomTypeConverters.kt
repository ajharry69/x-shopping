package co.ke.xently.shopping.libraries.data.source.local

import androidx.room.TypeConverter
import java.util.*

object RoomTypeConverters {
    object DateConverter {
        @TypeConverter
        fun dateToLong(date: Date): Long = date.time

        @TypeConverter
        fun longToDate(date: Long): Date = Date(date)
    }
}