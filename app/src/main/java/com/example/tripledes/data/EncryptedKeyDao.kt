package com.example.tripledes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EncryptedKeyDao {

    @get:Query("SELECT * FROM EncryptedKey ORDER BY destination")
    val all: List<EncryptedKey>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(key: EncryptedKey)

    @Delete
    suspend fun delete(key: EncryptedKey)
}