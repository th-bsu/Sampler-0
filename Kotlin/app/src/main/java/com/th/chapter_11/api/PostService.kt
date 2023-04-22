package com.th.chapter_11.api

import io.reactivex.rxjava3.core.Observable
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PostService {

    // TH: @Query parameter refers to remote interface.
    // TH: input parameter refers to local interface.

    // TH: issues GET request for 'posts' data from server: shelter.
    // TH: http://192.168.0.11:8080/product-composite-by-query-shelter?shelter=22-01
    @GET("/product-composite-by-query-shelter")
    fun getPosts(
        @Query("shelter") shelter:String
    ): Call<Post>

    // TH: issues GET request for 'posts' data from server: shelter, row.
    // TH: http://192.168.0.11:8080/product-composite-by-query-shelter-row?shelter=22-01&row=0
    @GET("/product-composite-by-query-shelter-row")
    fun getPosts(
        @Query("shelter") shelter:String,
        @Query("row") row:String
    ): Call<Post>

    // TH: deploys reactive programming.
    // TH: issues GET request for 'posts' data from server: shelter, row.
    // TH: http://192.168.0.11:8080/product-composite-by-query-shelter-row?"shelter=22-01&row=0"
    @GET("/product-composite-by-query-shelter-row")
    fun getPostsRxJava(
        @Query("shelter") shelter:String,
        @Query("row") row:String
    ): Observable<Post>

    // TH: deploys reactive programming.
    // TH: issues GET request for 'posts' data from server: productId.
    // TH: http://localhost:8080/product-composite?"productId=22052"
    @GET("/product-composite")
    fun getPostsRxJava(
        @Query("productId") productId: Long,
    ): Observable<Post>

    // TH: deploys reactive programming.
    // TH: issues GET request to server, by [recommendation] insertion.
    // http://192.168.0.11:8080/product-composite-by-insert-shelter-row?shelter=22-01&row=0&index=2&side=0&productId=X
    // TH: [https://code.tutsplus.com/tutorials/sending-data-with-retrofit-2-http-client-for-android--cms-27845]
    @GET("/product-composite-by-insert-shelter-row")
    fun insertRecommendationsRxJava(
        @Query("shelter") shelter: String,
        @Query("row") anchorRow: String,
        @Query("index") anchorIndex: String,
        @Query("side") whichSide: Int,
        @Query("productId") productId: String,
    ): Observable<Int>

    // TH: deploys reactive programming.
    // TH: issues GET request to server, by [recommendation] removal.
    // http://localhost:8080/product-composite-by-remove-shelter-row/?"shelter=22-02&row=0&setSortedString=1-2-4-5&setSortedBarString=3-6-7&setTotalString=1-2-3"
    // TH: [https://code.tutsplus.com/tutorials/sending-data-with-retrofit-2-http-client-for-android--cms-27845]
    @GET("/product-composite-by-remove-shelter-row")
    fun removeRecommendationsRxJava(
        @Query("shelter") shelter: String,
        @Query("row") row: String,
        @Query("setSortedString") deleteActionSetSortedString: String,
        @Query("setSortedBarString") deleteActionSetSortedBarString: String,
        @Query("setTotalString") deleteActionSetTotalString: String,
    ): Observable<Int>

    // TH: deploys reactive programming.
    // TH: issues GET request to server, by [recommendation] submission.
    // TH: [https://code.tutsplus.com/tutorials/sending-data-with-retrofit-2-http-client-for-android--cms-27845]
    @GET("/product-composite-by-submit-shelter-row")
    fun submitRecommendationsRxJava(
        @Query("shelter") shelter: String,
        @Query("row") row: String,
        @Query("setSortedStringProductId") setSortedStringProductId: String,
        @Query("setSortedString") setSortedString: String,
        @Query("setSortedBarString") setSortedBarString: String,
        @Query("setTotalString") setTotalString: String,
        @Query("setProductIdRemove") setProductIdRemove: String,
    ): Observable<Int>

    // TH: deploys reactive programming.
    // TH: issues GET request to server, by [recommendation] submission.
    // TH: [https://code.tutsplus.com/tutorials/sending-data-with-retrofit-2-http-client-for-android--cms-27845]
    @GET("/product-composite-by-confirm-shelter-row")
    fun confirmRecommendationsRxJava(
        @Query("shelter") shelter: String,
        @Query("row") row: String,
        @Query("setSortedStringName") actionSetSortedStringName: String,
        @Query("setSortedStringProductId") actionSetSortedStringProductId: String,
        @Query("setSortedStringAmount") actionSetSortedStringAmount: String,
        @Query("setSortedStringValue") actionSetSortedStringValue: String,
        @Query("setSortedStringAuthor") actionSetSortedStringAuthor: String,
        @Query("setSortedStringStatus") actionSetSortedStringStatus: String,
        @Query("setSortedStringTimeOrder") actionSetSortedStringTimeOrder: String,
    ): Observable<Int>

    // TH: deploys reactive programming.
    // TH: issues GET request to server, by [request] submission.
    // TH: [https://code.tutsplus.com/tutorials/sending-data-with-retrofit-2-http-client-for-android--cms-27845]
    @GET("/product-composite-by-request-shelter-row")
    fun listenToRequestsRxJava(
        @Query("recommendationIdRequested") recommendationIdFromConsumer: String,
        @Query("yearMonthDate") currentOrderLocal: String,
    ): Observable<Post>

    // TH: deploys reactive programming.
    // TH: issues GET request to server, by [request] submission.
    // TH: [https://code.tutsplus.com/tutorials/sending-data-with-retrofit-2-http-client-for-android--cms-27845]
    @GET("/product-composite-by-request-shelter-row-range")
    fun listenToRequestsRxJava(
        @Query("recommendationIdRequested") recommendationIdFromConsumer: String,
        @Query("yearMonthDatePrevious") currentOrderPrevious: String,
        @Query("yearMonthDateCurrent") currentOrderCurrent: String,
    ): Observable<Post>

    @GET("/product-composite-by-delete-recommendations")
    fun deleteRecommendations(
        @Query("productId")  productId: Long,
        @Query("recommendationId")  recommendationId: Long
    ): Observable<Int>

    @GET("/product-composite-by-insert-recommendations")
    fun insertRecommendations(
        @Query("productId")  productId: Long,
        @Query("recommendationId")  recommendationId: Int,
        @Query("author")  author: String,
        @Query("shelter")  shelter: String,
        @Query("row")  row: Int,
        @Query("offset")  offset: Int
    ): Observable<Int>

}