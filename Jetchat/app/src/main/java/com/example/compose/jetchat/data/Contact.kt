package com.example.compose.jetchat.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity datga class representing a Contact database table
 *
 * Each instance of this Entity represents a row in the Contact database table
 *
 */

@Entity (tableName = "contacts", primaryKeys = ["publicKey", "nickname"])
data class Contact (
    @ColumnInfo(name = "publicKey")
    val publicKey: String,
    @ColumnInfo(name = "nickname")
    val nickname: String,
)