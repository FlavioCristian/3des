package com.example.tripledes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {

    @get:Query("SELECT * FROM User")
    val all: List<User>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(key: User)

    @Delete
    suspend fun delete(key: User)
}