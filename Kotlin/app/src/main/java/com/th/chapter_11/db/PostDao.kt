package com.th.chapter_11.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// TH: marks as Data Access Object.
// TH: specifies database interactions.
@Dao
interface PostDao {

    // TH: inserts list of posts into database.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPosts(posts: PostEntity)

    // TH: gets observed within given life cycle.
    // TH: notifies observer about modification of wrapped data.
    @Query("SELECT * FROM posts")
    fun loadPosts(): LiveData<List<PostEntity>>

}