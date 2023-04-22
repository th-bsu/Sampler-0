package com.th.chapter_11.repository

import androidx.lifecycle.MutableLiveData
import com.th.chapter_11.db.RecommendationEntity
import java.util.concurrent.Executor

class ShelterRepositoryImpl(

    // TH: helps execute submitted executable tasks.
    private val executor: Executor

)
    : ShelterRepository
{

    // TH: represents each shelter: row, column.
    private lateinit var shelter: ArrayList<ArrayList<RecommendationEntity>>

    override fun getShelter(
        rows: Int,
        entities: List<RecommendationEntity>,
        shelterLiveData: MutableLiveData<ArrayList<ArrayList<RecommendationEntity>>>
    ) {

        shelter = arrayListOf(arrayListOf())

        for (index in 0 until rows) {

            // TH: initializes each row.
            // TH: https://stackoverflow.com/questions/33278869/how-do-i-initialize-kotlins-mutablelist-to-empty-mutablelist
            val row = ArrayList<RecommendationEntity>()

            // TH: populates shelter.
            entities
                .filter {
                    it.row == index
                }
                .map {
                    row.add(it)
                }
                .apply {
                    shelter.add(index,row)
                }

        }

        for (index in 0 until shelter.size) {
            // TH: removes empty list(s) from shelter.
            shelter
                .filter {
                    it.size == 0
                }
                .map {
                    shelter.remove(it)
                }
        }

        shelterLiveData.postValue(shelter)

    }

}
