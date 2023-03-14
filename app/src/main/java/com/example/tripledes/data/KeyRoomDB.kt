package com.example.tripledes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tripledes.FirstFragment.Companion.PRIVATE_KEY
import com.example.tripledes.FirstFragment.Companion.PUBLIC_KEY

@Database(entities = [Key::class, User::class, EncryptedKey::class], version = 1, exportSchema = false)
abstract class KeyRoomDB : RoomDatabase(){

    abstract fun keyDao() : KeyDao

    abstract fun userDao(): UserDao

    abstract fun encryptedKeyDao(): EncryptedKeyDao

    companion object {

        @Volatile
        private var  INSTANCE : KeyRoomDB? = null


        fun getDatabase(context: Context): KeyRoomDB{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KeyRoomDB::class.java,
                    "key_database"
                ).allowMainThreadQueries().build()

                INSTANCE = instance

                instance
            }
        }

        fun getPrivateKey(context: Context): String {
            val keys = getDatabase(context).keyDao().all
            var result = ""
            keys.forEach { key ->
                if (key.name == PRIVATE_KEY) {
                    result = key.value
                }
            }
            return result
        }

        fun getPublicKey(context: Context): String {
            val keys = getDatabase(context).keyDao().all
            var result = ""
            keys.forEach { key ->
                if (key.name == PUBLIC_KEY) {
                    result = key.value
                }
            }
            return result
        }

        fun getUsers(context: Context): ArrayList<User> {
            val users = getDatabase(context).userDao().all
            return ArrayList(users)
        }
    }
}