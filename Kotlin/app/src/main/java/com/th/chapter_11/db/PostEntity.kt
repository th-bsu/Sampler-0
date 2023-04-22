package com.th.chapter_11.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name="productId") val productId: Long,
    @ColumnInfo(name="name") val name: String,
    @ColumnInfo(name="amount") val amount: Float,
    @ColumnInfo(name="value") val value: Float
)
