package de.salomax.sauterschnaeppchen.data

import android.content.Context
import androidx.room.*
import java.lang.IllegalArgumentException

@Database(entities = [Item::class], version = 1)
@TypeConverters(ConditionConverter::class, TargetSystemConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                synchronized(AppDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java, "app_database"
                    )
                        .fallbackToDestructiveMigration()
                        //.addCallback(roomCallback)
                        .build()
                }
            }
            return instance!!
        }
    }

}

class ConditionConverter {
    @TypeConverter
    fun toEnum(int: Int?): Condition? {
        return int?.let { Condition.valueOf(it) }
    }

    @TypeConverter
    fun fromEnum(condition: Condition?): Int? {
        return condition?.ordinal
    }
}


class TargetSystemConverter {
    @TypeConverter
    fun toEnum(int: Int?): TargetSystem? {
        return int?.let { TargetSystem.valueOf(it) }
    }

    @TypeConverter
    fun fromEnum(targetSystem: TargetSystem?): Int? {
        return targetSystem?.ordinal
    }
}
