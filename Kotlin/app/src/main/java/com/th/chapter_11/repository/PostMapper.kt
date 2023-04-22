package com.th.chapter_11.repository

import com.th.chapter_11.ShelterActivity
import com.th.chapter_11.api.Post
import com.th.chapter_11.api.Recommendation
import com.th.chapter_11.db.PostEntity
import com.th.chapter_11.db.RecommendationEntity

class PostMapper {

    // TH: help handle NullPointerException.
    private val recommendationEntityDummyUrl = ShelterActivity.LAYOUT_DUMMY_URL

    // TH: converts post service to post entity.
    fun serviceToEntity(post: Post): PostEntity
    {
        return PostEntity(
            post.productId,
            post.name,
            post.amount,
            post.value
        )
    }

    // TH: converts model to entity.
    fun serviceToRecommendationEntity(rec: Recommendation): RecommendationEntity {
        return RecommendationEntity(
            rec.recommendationId,
            rec.author,
            rec.row,
            rec.offset,
            rec.shelter,
            rec.productId,
            rec.url ?: recommendationEntityDummyUrl
        )
    }

    // TH: converts model to entity, as List.
    // TH: gets deployed in ShelterViewModel (i.e. Observable transformation).
    fun serviceToRecommendationEntityRxJava(inputList: List<Recommendation>): List<RecommendationEntity> {

        var outputList = ArrayList<RecommendationEntity>()

        // TH: sorts list.
        val inputListSorted = inputList.sortedWith(compareBy({ it.row }, { it.offset }))
        for(offset in inputListSorted.indices){
            outputList.add(
                offset,
                serviceToRecommendationEntity(inputListSorted[offset])
            )
        }//for offset.

        return outputList
    }

}