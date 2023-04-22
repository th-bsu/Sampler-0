package com.th.chapter_11.db

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "selections", primaryKeys = ["productId","timeStampOrdered"])
data class SelectEntity(
    @ColumnInfo(name="productId")     val productId:     Long,   /* Back-End: Product */
    @ColumnInfo(name="Name")          val Name:          String, /* Back-End: Product */
    @ColumnInfo(name="amount")        val amount:        Int,    /* Back-End: Product */
    @ColumnInfo(name="value")         val value:         Double, /* Back-End: Product */
    @ColumnInfo(name="author")        var author:        String, /* */
    @ColumnInfo(name="status")        val status:        String, /* */
    @ColumnInfo(name="timeOrdered")   val timeOrdered:   String, /* */
    @ColumnInfo(name="timePending")   val timePending:   String, /* */
    @ColumnInfo(name="timeProcessed") val timeProcessed: String, /* */
    @ColumnInfo(name="timeShipped")   val timeShipped:   String, /* */
    @ColumnInfo(name="timeReceived")  val timeReceived:  String, /* */
)
