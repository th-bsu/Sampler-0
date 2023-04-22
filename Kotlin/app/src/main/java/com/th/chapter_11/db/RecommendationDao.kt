package com.th.chapter_11.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecommendationDao {

    // TH: inserts list of recommendations into database.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecommendations(recommendations: List<RecommendationEntity>)

    // TH: selects all recommendations from shelter.
    @Query("SELECT * FROM recommendations WHERE shelter=:shelter")
    abstract fun loadRecommendations(shelter: String): LiveData<List<RecommendationEntity>>

    // TH: selects all recommendations from shelter, row.
    @Query("SELECT * FROM recommendations WHERE shelter=:shelter AND `row`=:row")
    abstract fun loadRecommendations(shelter: String, row: String): LiveData<List<RecommendationEntity>>

    @Query("SELECT COUNT(DISTINCT `row`) FROM recommendations WHERE shelter=:shelter")
    abstract fun loadRecommendationsNumRow(shelter: String): Int

    @Query("SELECT COUNT(DISTINCT `offset`) FROM recommendations WHERE shelter=:shelter and `row`=:row")
    abstract fun loadRecommendationsNumCol(shelter: String, row: Int): Int

    // TH: deletes all recommendations by shelter.
    @Query("DELETE FROM recommendations WHERE shelter=:shelter")
    abstract fun clearRecommendations(shelter: String)

}