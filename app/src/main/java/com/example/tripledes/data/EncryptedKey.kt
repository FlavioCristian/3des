package com.example.tripledes.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="EncryptedKey")
data class EncryptedKey(
    @PrimaryKey @ColumnInfo(name = "destination") val destination: String,
    @ColumnInfo(name = "value") val value: String
    )
