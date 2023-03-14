package com.example.tripledes.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="User")
data class User(
    @PrimaryKey @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "public_key") val publicKey: String
    )
