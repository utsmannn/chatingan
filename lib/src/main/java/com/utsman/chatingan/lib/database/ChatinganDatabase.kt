package com.utsman.chatingan.lib.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.utsman.chatingan.lib.data.entity.ContactEntity
import com.utsman.chatingan.lib.data.entity.MessageEntity
import com.utsman.chatingan.lib.data.entity.MessageInfoEntity

@Database(
    entities = [ContactEntity::class, MessageInfoEntity::class, MessageEntity::class],
    version = 1
)
abstract class ChatinganDatabase : RoomDatabase() {
    abstract fun chatinganDao(): ChatinganDao

    companion object {
        @Volatile
        private var instance: ChatinganDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): ChatinganDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context,
                    ChatinganDatabase::class.java,
                    "chatingan_db"
                ).build().also { instance = it }
            }
        }
    }
}