package com.th.chapter_11.db

// TH: helps transfer list of items between Activities.
import androidx.room.ColumnInfo
import androidx.room.Entity
import java.io.Serializable

// TH: represents composite primary key.
@Entity(tableName = "recommendations", primaryKeys = ["recommendationId","productId","shelter","row","offset"])
data class RecommendationEntity(
    @ColumnInfo(name="recommendationId") val recommendationId: Long,
    @ColumnInfo(name="author") val author: String,
    @ColumnInfo(name="row") val row: Int,
    @ColumnInfo(name="offset") var offset: Int,
    @ColumnInfo(name="shelter") val shelter: String,
    @ColumnInfo(name="productId") val productId: Long,
    @ColumnInfo(name="url") val url: String
): Serializable {

    fun setOffsetNew(offset: Int) { this.offset=offset }

}