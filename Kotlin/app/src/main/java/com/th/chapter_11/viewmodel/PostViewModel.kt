package com.th.chapter_11.viewmodel

import androidx.lifecycle.ViewModel
import com.th.chapter_11.repository.PostRepository

class PostViewModel(
    private val postRepository: PostRepository
)
    : ViewModel()
{

    /*
    // TH: helps observe changes to database.
    // TH: returns LiveData for List<PostEntity>.
    fun getPosts() = postRepository.getPosts()
    */

    // TH: returns LiveData for List<RecommendationEntity>: shelter.
    fun getRecommendations(shelter: String) = postRepository.getRecommendations(shelter)

    // TH: returns LiveData for List<RecommendationEntity>: shelter, row.
    fun getRecommendations(shelter: String, row: String) = postRepository.getRecommendations(shelter, row)

    // TH: inserts Recommendations into back-end, to either side of anchor.
    fun insertRecommendations(shelter: String, row: String, anchor: String, where: String) =
        postRepository.insertRecommendations(shelter,row,anchor,where)

    // TH: returns distinct number of rows from recommendations per shelter.
//  fun getRecommendationsNumRow(shelter: String) = postRepository.getRecommendationsNumRow(shelter)
    fun recommendationsNumRowVar() = postRepository.getRecommendationsNumRowVar()

    // TH: returns distinct number of columns from recommendations per row, per shelter.
    fun recommendationsNumColVar() = postRepository.getRecommendationsNumColVar()

}