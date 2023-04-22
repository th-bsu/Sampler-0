package com.th.chapter_11.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.th.chapter_11.api.Post
import com.th.chapter_11.api.PostService
import com.th.chapter_11.db.PostDao
import com.th.chapter_11.db.PostEntity
import com.th.chapter_11.db.RecommendationDao
import com.th.chapter_11.db.RecommendationEntity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

class PostRepositoryImpl
(
    private val postService: PostService,
    private val postDao: PostDao,
    private val recommendationDao: RecommendationDao,
    private val postMapper: PostMapper,

    // TH: helps execute submitted executable tasks.
    private val executor: Executor,
    private val executor_rec: Executor
)
    : PostRepository
{

    // TH: represents non-const, versus val.
    private var recommendationsNumRow = 0
    private var recommendationsNumCol = 0

    override fun getRecommendationsNumRow(shelter: String): Int {
        return recommendationDao.loadRecommendationsNumRow(shelter)
    }

    override fun getRecommendationsNumRowVar(): Int {
        return recommendationsNumRow
    }

    override fun getRecommendationsNumColVar(): Int {
        return recommendationsNumCol
    }

    // TH: issues service async request and updates database.
    // TH: gets observed by custom ViewModel.
    override fun getPosts(shelter: String): LiveData<List<PostEntity>> {

        // TH: issues request asynchronously and notifies observer of response.
        postService.getPosts(shelter).enqueue (

            object : Callback<Post> {

                // TH: gets invoked when request succeeded.
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    response.body()?.let{
                        executePosts(executor,postDao,postMapper, it)
                        executeRecommendations(executor_rec,recommendationDao,it,postMapper,shelter)
                    }
                }

                override fun onFailure(call: Call<Post>, t: Throwable) {
                    Log.i("PostRepositoryImpl:getPosts", "onFailure: getPosts.")
                    t.message?.let { Log.i("PostRepositoryImpl:getPosts", it) }
                }

            }
        )

        // TH: returns posts.
        return postDao.loadPosts()

    }//getPosts.

    // TH: updates database (i.e. asynchronously), by executor: shelter.
    private fun executeRecommendations(
        executor: Executor,
        recommendationDao: RecommendationDao,
        it: Post,
        postMapper: PostMapper,
        shelter: String
    ) {

        executor.execute {

            recommendationDao.clearRecommendations(shelter)

            recommendationDao.insertRecommendations (
                it.recommendations.map {
                    postMapper.serviceToRecommendationEntity(it)
                }
            )

            recommendationsNumRow = recommendationDao.loadRecommendationsNumRow(shelter)
            recommendationsNumCol = recommendationDao.loadRecommendationsNumCol(shelter,0)

        }

    }//executeRecommendations: shelter.

    // TH: updates database (i.e. asynchronously), by executor: shelter, row.
    private fun executeRecommendations(
        executor: Executor,
        recommendationDao: RecommendationDao,
        it: Post,
        postMapper: PostMapper,
        shelter: String,
        row: String
    ) {

        executor.execute {

            recommendationDao.insertRecommendations (
                it.recommendations.map {
                    postMapper.serviceToRecommendationEntity(it)
                }
            )

            recommendationsNumRow = recommendationDao.loadRecommendationsNumRow(shelter)
            recommendationsNumCol = recommendationDao.loadRecommendationsNumCol(shelter,Integer.parseInt(row))

        }

    }//executeRecommendations: shelter, row.

    // TH: updates database (i.e. asynchronously), by executor.
    private fun executePosts(
        executor: Executor,
        postDao: PostDao,
        postMapper: PostMapper,
        it: Post
    ) {
        executor.execute {
            postDao.insertPosts (
                postMapper.serviceToEntity(it)
            )
        }
    }


    override fun getRecommendations(shelter: String): LiveData<List<RecommendationEntity>> {

        // TH: issues request asynchronously and notifies observer of response.
        postService.getPosts(shelter).enqueue (

            object : Callback<Post> {

                // TH: gets invoked when request succeeded.
                override fun onResponse(call: Call<Post>, response: Response<Post>) {

                    response.body()?.let{

                        executePosts(executor,postDao,postMapper, it)
                        executeRecommendations(executor_rec,recommendationDao,it,postMapper,shelter)

                    }

                }

                override fun onFailure(call: Call<Post>, t: Throwable) {
                    Log.i("PostRepositoryImpl:getRecommendations", "onFailure: getPosts($shelter).")
                    t.message?.let { Log.i("PostRepositoryImpl:getRecommendations", it) }
                }

            }
        )

        // TH: returns recommendations.
        return recommendationDao.loadRecommendations(shelter)

    }//getRecommendations: shelter.

    override fun getRecommendations(shelter: String, row: String): LiveData<List<RecommendationEntity>> {

        // TH: issues request asynchronously and notifies observer of response.
        postService.getPosts(shelter,row).enqueue (
            object : Callback<Post> {

                // TH: gets invoked when request succeeded.
                override fun onResponse(call: Call<Post>, response: Response<Post>) {

                    response.body()?.let{
                        executePosts(executor,postDao,postMapper, it)
                        executeRecommendations(executor_rec,recommendationDao,it,postMapper,shelter,row)
                    }

                }

                override fun onFailure(call: Call<Post>, t: Throwable) {
                    Log.i("PostRepositoryImpl:getRecommendations", "onFailure: getPosts($shelter,$row).")
                    t.message?.let { Log.i("PostRepositoryImpl:getRecommendations", it) }
                }

            }
        )

        // TH: returns recommendations.
        return recommendationDao.loadRecommendations(shelter,row)

    }//getRecommendations: shelter, row.

    override fun getPostsRxJava(productId: Long) = postService.getPostsRxJava(productId)


    override fun getRecommendationsRxJava(
        shelter: String,
        row: String,
    ) = postService.getPostsRxJava(
        shelter,
        row
    )

    override fun insertRecommendationsRxJava(
        shelter: String,
        anchorRow: String,
        anchorIndex: String,
        whichSide: Int,
        productId: String,
    ) = postService.insertRecommendationsRxJava(
        shelter,
        anchorRow,
        anchorIndex,
        whichSide,
        productId,
    )

    override fun removeRecommendationsRxJava(
        shelter: String,
        row: String,
        deleteActionSetSortedString: String,
        deleteActionSetSortedBarString: String,
        deleteActionSetTotalString: String,
    ) = postService.removeRecommendationsRxJava(
        shelter,
        row,
        deleteActionSetSortedString,
        deleteActionSetSortedBarString,
        deleteActionSetTotalString,
    )

    // TH: OBSOLETE !!
    override fun insertRecommendations(
        shelter: String,
        row: String,
        anchor: String,
        where: String
    ): LiveData<List<RecommendationEntity>> {
        // TH: returns recommendations.
        return recommendationDao.loadRecommendations(shelter,row)
    }

    override fun getPostMapper() = postMapper

    override fun deleteRecommendations(
        productId: Long,
        recommendationId: Long
    ) = postService.deleteRecommendations(
        productId,
        recommendationId
    )

    override fun insertRecommendations(
        productId: Long,
        recommendationId: Int,
        author: String,
        shelter: String,
        row: Int,
        offset: Int
    ) = postService.insertRecommendations(
        productId,
        recommendationId,
        author,
        shelter,
        row,
        offset
    )

    override fun submitRecommendationsRxJava(
        shelter: String,
        row: String,
        actionSetSortedStringProductId: String,
        actionSetSortedString: String,
        actionSetSortedBarString: String,
        actionSetTotalString: String,
        actionSetProductIdRemove: String
    ) = postService.submitRecommendationsRxJava(
        shelter,
        row,
        actionSetSortedStringProductId,
        actionSetSortedString,
        actionSetSortedBarString,
        actionSetTotalString,
        actionSetProductIdRemove,
    )

    override fun confirmRecommendationsRxJava(
        shelter: String,
        row: String,
        actionSetSortedStringName: String,
        actionSetSortedStringProductId: String,
        actionSetSortedStringAmount: String,
        actionSetSortedStringValue: String,
        actionSetSortedStringAuthor: String,
        actionSetSortedStringStatus: String,
        actionSetSortedStringTimeOrder: String
    ) = postService.confirmRecommendationsRxJava(
        shelter,
        row,
        actionSetSortedStringName,
        actionSetSortedStringProductId,
        actionSetSortedStringAmount,
        actionSetSortedStringValue,
        actionSetSortedStringAuthor,
        actionSetSortedStringStatus,
        actionSetSortedStringTimeOrder
    )

    override fun listenToRequestsRxJava(
        recommendationIdFromConsumer: String,
        currentOrderLocal: String,
    ) = postService.listenToRequestsRxJava(
        recommendationIdFromConsumer,
        currentOrderLocal
    )

    override fun listenToRequestsRxJava(
        recommendationIdFromConsumer: String,
        currentOrderPrevious: String,
        currentOrderCurrent: String,
    ) = postService.listenToRequestsRxJava(
        recommendationIdFromConsumer,
        currentOrderPrevious,
        currentOrderCurrent,
    )

}