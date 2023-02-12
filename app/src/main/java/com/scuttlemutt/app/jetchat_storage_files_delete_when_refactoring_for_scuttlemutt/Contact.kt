package com.scuttlemutt.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Entity data class representing a Contact database table
 *
 * Each instance of this Entity represents a row in the Contact database table
 *
 * Helps map keys to nicknames
 */

@Entity (tableName = "contacts", primaryKeys = ["publicKey", "nickname"])
data class Contact (
    @ColumnInfo(name = "publicKey")
    val publicKey: String,
    @ColumnInfo(name = "nickname")
    val nickname: String,
)