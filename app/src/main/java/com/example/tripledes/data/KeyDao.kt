package com.example.tripledes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface KeyDao {

    @get:Query("SELECT * FROM Key ORDER BY name")
    val all: List<Key>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(key: Key)

    @Delete
    suspend fun delete(key: Key)
}