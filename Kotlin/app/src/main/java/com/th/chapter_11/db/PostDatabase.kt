package com.th.chapter_11.db

import androidx.room.Database
import androidx.room.RoomDatabase

// TH: marks as RoomDatabase.
@Database(
    // TH: lists entities (i.e. tables) in database.
    entities = [
        PostEntity::class,
        RecommendationEntity::class
    ],
    // TH: represents database version.
    version  = 1
)

// TH: specifies interface methods for database interaction.
// TH: PostDao           --- PostEntity.
// TH: RecommendationDao --- RecommendationEntity.
abstract class PostDatabase: RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun recommendationDao(): RecommendationDao
}