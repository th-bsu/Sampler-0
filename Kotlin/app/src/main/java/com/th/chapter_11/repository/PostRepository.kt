package com.th.chapter_11.repository

import androidx.lifecycle.LiveData
import com.th.chapter_11.api.Post
import com.th.chapter_11.db.PostEntity
import com.th.chapter_11.db.RecommendationEntity
import io.reactivex.rxjava3.core.Observable

interface PostRepository {

    // TH: gets observed within given life cycle.
    // TH: notifies observer about modification of wrapped data.
    fun getPosts(shelter: String): LiveData<List<PostEntity>>

    fun getPostsRxJava(
        productId: Long
    ): Observable<Post>

    // TH: gets LiveData for recommendations: shelter.
    fun getRecommendations(shelter: String): LiveData<List<RecommendationEntity>>

    // TH: gets LiveData for recommendations: shelter, row.
    fun getRecommendations(
        shelter: String,
        row: String
    ): LiveData<List<RecommendationEntity>>

    // TH: OBSOLETE !!
    // TH: inserts Recommendations into back-end, to either side of anchor.
    fun insertRecommendations(
        shelter: String,
        row: String,
        anchor: String,
        where: String
    ): LiveData<List<RecommendationEntity>>

    // TH: returns number of rows per shelter.
    fun getRecommendationsNumRow(shelter: String): Int
    fun getRecommendationsNumRowVar(): Int

    // TH: returns number of columns per row, per shelter.
    fun getRecommendationsNumColVar(): Int

    fun getPostMapper(): PostMapper

    fun getRecommendationsRxJava(
        shelter: String,
        row: String
    ): Observable<Post>

    /*
    // TH: whichSide==0: left, whichSide==1: right.
    */
    fun insertRecommendationsRxJava(
        shelter: String,
        anchorRow: String,
        anchorIndex: String,
        whichSide: Int,
        productId: String
    ): Observable<Int>

    fun removeRecommendationsRxJava(
        shelter: String,
        row: String,
        deleteActionSetSortedString: String,
        deleteActionSetSortedBarString: String,
        deleteActionSetTotalString: String,
    ): Observable<Int>

    fun deleteRecommendations(
        productId: Long,
        recommendationId: Long
    ): Observable<Int>

    fun insertRecommendations(
        productId: Long,
        recommendationId: Int,
        author: String,
        shelter: String,
        row: Int,
        offset: Int
    ): Observable<Int>

    fun submitRecommendationsRxJava(
        shelter: String,
        row: String,
        actionSetSortedStringProductId: String,
        actionSetSortedString: String,
        actionSetSortedBarString: String,
        actionSetTotalString: String,
        actionSetProductIdRemove: String
    ): Observable<Int>

    fun confirmRecommendationsRxJava(
        shelter: String,
        row: String,
        actionSetSortedStringName: String,
        actionSetSortedStringProductId: String,
        actionSetSortedStringAmount: String,
        actionSetSortedStringValue: String,
        actionSetSortedStringAuthor: String,
        actionSetSortedStringStatus: String,
        actionSetSortedStringTimeOrder: String
    ): Observable<Int>

    fun listenToRequestsRxJava(
        recommendationIdFromConsumer: String,
        currentOrderLocal: String,
    ): Observable<Post>

    fun listenToRequestsRxJava(
        recommendationIdFromConsumer: String,
        currentOrderPrevious: String,
        currentOrderCurrent: String,
    ): Observable<Post>

}