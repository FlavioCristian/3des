package com.example.tripledes.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="Key")
data class Key(
    @PrimaryKey @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "value") val value: String
    )
