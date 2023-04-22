package com.th.chapter_11.repository

import androidx.lifecycle.MutableLiveData
import com.th.chapter_11.db.RecommendationEntity

interface ShelterRepository {

    abstract fun getShelter(
        rows: Int,
        entities: List<RecommendationEntity>,
        shelterLiveData: MutableLiveData<ArrayList<ArrayList<RecommendationEntity>>>
    )

}
