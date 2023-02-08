package com.example.compose.jetchat.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity data class representing a Bark
 *
 * Barks use public keys as identifiers
 *
 * Public keys can be mapped to nicknames with the contact database
 *
 */

@Entity (tableName = "barks", primaryKeys = ["srcPublicKey", "dstPublicKey", "seqNum"])
data class Bark (
    @ColumnInfo(name = "srcPublicKey")
    val srcPublicKey: String,
    @ColumnInfo(name = "dstPublicKey")
    val dstPublicKey: String,
    @ColumnInfo(name = "seqNum")
    val seqNum: Int,
    @ColumnInfo(name = "msg")
    val msg: String,
    @ColumnInfo(name = "timestamp")
    val timestamp: String,
)