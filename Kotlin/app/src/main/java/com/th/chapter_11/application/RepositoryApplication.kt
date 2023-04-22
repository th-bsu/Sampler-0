package com.th.chapter_11.application

import android.app.Application
import androidx.room.Room
import com.th.chapter_11.api.PostService
import com.th.chapter_11.db.PostDatabase
import com.th.chapter_11.repository.PostMapper
import com.th.chapter_11.repository.PostRepository
import com.th.chapter_11.repository.PostRepositoryImpl
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executors

// TH: initializes all services for repository, creates new repo.
class RepositoryApplication: Application() {

    lateinit var postRepository: PostRepository

    override fun onCreate(){
        super.onCreate()

        // TH: helps generate implementation for interface that would make HTTP requests to remote server.
        val retrofit = Retrofit.Builder()
            .baseUrl("http://XXX.XXX.XXX.XXX:ABCD")                    /* sets base URL for API server. */
            .addConverterFactory(GsonConverterFactory.create())        /* deploys Gson for JSON. */
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create()) /* helps with Observable. */
            .build()

        // TH: creates implementation of the API endpoints defined by the service interface.
        val postService = retrofit.create(PostService::class.java)

        // TH: builds persistent database.
        // TH: returns single, global Application object of the current process.
        val postDatabase =
            Room.databaseBuilder(applicationContext, PostDatabase::class.java, "post-db")
                .build()

        // TH: implements matching interface.
        postRepository = PostRepositoryImpl (

            postService,
            postDatabase.postDao(),
            postDatabase.recommendationDao(),
            PostMapper(),

            // TH: deploys single thread to operate on unbounded queue, with fail-over.
            Executors.newSingleThreadExecutor(), // TH: post.
            Executors.newSingleThreadExecutor()  // TH: recommendation.
        )

    }

}