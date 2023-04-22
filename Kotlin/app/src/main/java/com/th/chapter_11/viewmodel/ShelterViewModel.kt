package com.th.chapter_11.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.th.chapter_11.ShelterActivity.Companion.LAYOUT_INSERT_LEFT
import com.th.chapter_11.ShelterActivity.Companion.LAYOUT_INSERT_RIGHT
import com.th.chapter_11.db.RecommendationEntity
import com.th.chapter_11.repository.PostRepository
import com.th.chapter_11.util.EspressoIdlingResource
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class ShelterViewModel(
    private val postRepository: PostRepository
)
    : ViewModel()
{

    val shelterLiveData = MutableLiveData<ArrayList<ArrayList<RecommendationEntity>>>()

    var recommendationEntityList = MutableLiveData<List<RecommendationEntity>>()

    // TH: helps post LiveData.
    private val recommendationEntityListRxJava = MutableLiveData<List<RecommendationEntity>>()
    private val recommendationEntityListRxJavaError = MutableLiveData<String>()

    val recommendationEntityListRxJavaProperty: LiveData<List<RecommendationEntity>>
        get() = recommendationEntityListRxJava

    val recommendationEntityListRxJavaErrorProperty: LiveData<String>
        get() = recommendationEntityListRxJavaError

    // TH: represents each shelter: row, column.
    private lateinit var shelter: ArrayList<ArrayList<RecommendationEntity>>

    private var total = 0
    private val totalRxJava = MutableLiveData<Int>()
    private val totalRxJavaError = MutableLiveData<String>()
    val totalRxJavaProperty: LiveData<Int>
        get() = totalRxJava
    val totalRxJavaPropertyError: LiveData<String>
        get() = totalRxJavaError

    // TH: represents disposable.
    private var disposable       = CompositeDisposable()
    private var disposableInsert = CompositeDisposable()
    private var disposableRemove = CompositeDisposable()
    private var disposableSubmit = CompositeDisposable()

    init{

        shelterLiveData.postValue(null)
        totalRxJava.postValue(0)

    }//init.

    fun initShelter() {

        shelter = arrayListOf()
        total = 0
        shelterLiveData.postValue(shelter)

    }//initShelter.

    @Synchronized
    fun increaseTotal(): Int {
        total++
        return total
    }

    fun getShelterFragment(index: Int): List<RecommendationEntity> {
        return shelter[index]
    }

    // TH: returns LiveData for List<RecommendationEntity>: shelter, row.
    fun getRecommendations(shelter: String, row: String) = postRepository.getRecommendations(shelter, row)

    // TH: returns distinct number of rows from recommendations per shelter.
//  fun getRecommendationsNumRow(shelter: String) = postRepository.getRecommendationsNumRow(shelter)
    fun recommendationsNumRowVar() = postRepository.getRecommendationsNumRowVar()

    // TH: returns distinct number of columns from recommendations per row, per shelter.
    fun recommendationsNumColVar() = postRepository.getRecommendationsNumColVar()

    // TH: OBSOLETE !!
    // TH: inserts Recommendations into back-end, to either side of anchor.
    fun insertRecommendations(shelter: String, row: String, anchor: String, where: String) =
        postRepository.insertRecommendations(shelter,row,anchor,where)

    // TH: queries external end-point and pre-filters data in one-shot.
    fun getRecommendationsRxJava(
        shelter: String,
        anchor: String,
        designForTest: Int,
    ) {

        // TH: experimental.
        this.shelter[anchor.toInt()] = getShelter(anchor.toInt())

        disposable.add(
            postRepository.getRecommendationsRxJava(shelter, anchor)
                .debounce(5,TimeUnit.SECONDS)
                .timeout(1,TimeUnit.MINUTES)
                // TH: deploys IO operation on one thread.
                .subscribeOn(
                    Schedulers.io()
                )
                // TH: transforms one Observable into another.
                .map{

                    /*
                    // TH: displays debug --- IMPORTANT.
                    Log.i(
                        "submitActionButton-getRecommendationsRxJava (before)",
                        "this.shelter[${anchor.toInt()}].size=${this.shelter[anchor.toInt()].size}"
                    )
                    */

                    // TH: experimental.
                    this.shelter[anchor.toInt()] = postRepository.getPostMapper()
                        .serviceToRecommendationEntityRxJava(it.recommendations)
                            as java.util.ArrayList<RecommendationEntity>

                    /*
                    // TH: displays debug --- IMPORTANT.
                    Log.i(
                        "submitActionButton-getRecommendationsRxJava  (after)",
                        "this.shelter[${anchor.toInt()}].size=${this.shelter[anchor.toInt()].size}"
                    )
                    */

                    // TH: displays debug --- IMPORTANT.
                    for(index in 0 until this.shelter[anchor.toInt()].size){
                        Log.i(
                            "submitActionButton-getRecommendationsRxJava",
                            "this.shelter[${anchor.toInt()}][$index].productId=" +
                                    "${this.shelter[anchor.toInt()][index].productId}" +
                                    " @ ${this.shelter[anchor.toInt()][index].offset}"
                        )
                    }//for index.

                    this.shelter[anchor.toInt()]

                }
                // TH: deploys RxJava operation in another thread.
                .observeOn(
                    AndroidSchedulers.mainThread()
                )
                // TH: helps post LiveData as part of Observable, which then gets observed by Activity.
                .subscribe(
                    {
                        recommendationEntityListRxJava.postValue(it)

                        if(designForTest==1) {
                            if (!EspressoIdlingResource.getCounterIdler().isIdleNow) {
                                // TH: resumes test flow.
                                EspressoIdlingResource.decrement()
                            }
                        }//if designForTest.

                    },
                    {
                        Log.i(
                            "submitActionButton-getRecommendationsRxJava",
                            "getRecommendationsRxJava Error: ${it.printStackTrace()}"
                            //"getRecommendationsRxJava Error: ${it.message}"
                        )
                        recommendationEntityListRxJavaError.postValue(
                            "getRecommendationsRxJava Error: ${it.message}"
                        )
                    }
                )
        )

    }//getRecommendationsRxJava: shelter, row.

    fun insertRecommendationsRxJava(
        shelter: String,
        anchorRow: String,
        anchorIndex: String,
        whichSide: String,
        productId: String
    ) {

        disposableInsert.add(
            postRepository.insertRecommendationsRxJava(
                shelter,
                anchorRow,
                anchorIndex,
                (
                        when (whichSide) {
                            LAYOUT_INSERT_LEFT  -> 0
                            LAYOUT_INSERT_RIGHT -> 1
                            else -> 2
                        }
                        ),
                productId
            )
                // TH: deploys IO operation on one thread.
                .subscribeOn(
                    Schedulers.io()
                )
                // TH: transforms one Observable into another.
                .map{
                    if(it==0){
                        Log.i("ShelterViewModel:insertRecommendationsRxJava", "Succeeded.")
                    }
                    else{
                        Log.i("ShelterViewModel:insertRecommendationsRxJava", "Failed.")
                    }
                }
                // TH: deploys RxJava operation in another thread.
                .observeOn(
                    AndroidSchedulers.mainThread()
                )
                // TH: helps post LiveData as part of Observable, which then gets observed by Activity.
                .subscribe(
                    {
                        if(it==0) totalRxJava.postValue(it)
                    },
                    {
                        totalRxJavaError.postValue(
                            "insertRecommendationsRxJava Error: ${it.message}"
                        )
                    }
                )
        )

    }//insertRecommendationsRxJava ...

    // TH: deletes by recommendationId.
    fun deleteRecommendations(
        productId: Long,
        recommendationId: Long
    ) {

        disposableInsert.add(
            postRepository.deleteRecommendations(productId, recommendationId)
                // TH: deploys IO operation on one thread.
                .subscribeOn(
                    Schedulers.io()
                )
                // TH: transforms one Observable into another.
                .map{
                    if(it==0){
                        increaseTotal()
                    }
                    else{
                        Log.i("ShelterViewModel:deleteRecommendations", "Failed.")
                    }
                }
                // TH: deploys RxJava operation in another thread.
                .observeOn(
                    AndroidSchedulers.mainThread()
                )
                // TH: helps post LiveData as part of Observable, which then gets observed by Activity.
                .subscribe {
                    totalRxJava.postValue(it)
                }
        )

    }//deleteRecommendations.

    // TH: inserts Recommendations into back-end.
    fun insertRecommendations(
        productId: Long,
        recommendationId: Int,
        author: String,
        shelter: String,
        row: Int,
        offset: Int,
    ) {

        disposableInsert.add(
            postRepository.insertRecommendations(productId,recommendationId,author,shelter,row,offset)
                // TH: deploys IO operation on one thread.
                .subscribeOn(
                    Schedulers.io()
                )
                // TH: transforms one Observable into another.
                .map{
                    if(it==0){
                        increaseTotal()
                    }
                    else{
                        Log.i("ShelterViewModel:insertRecommendations", "Failed.")
                    }
                }
                // TH: deploys RxJava operation in another thread.
                .observeOn(
                    AndroidSchedulers.mainThread()
                )
                // TH: helps post LiveData as part of Observable, which then gets observed by Activity.
                .subscribe {
                    totalRxJava.postValue(it)
                }

        )

    }//insertRecommendations.

    fun getTotal() = total
    fun setTotal(total: Int) { this.total = total }

    private fun getShelterSize() = this.shelter.size

    // TH: implements getter.
    fun getShelter(row: Int): ArrayList<RecommendationEntity> {

        // TH: generates place holder only when necessary.
        while(row>=getShelterSize()){
            this.shelter.add(arrayListOf())
        }

        return this.shelter[row]

    }//getShelter: row.

    // TH: implements setter.
    fun setShelter(row: Int, it: ArrayList<RecommendationEntity>?) {
        if (it != null) {
            this.shelter[row] = it
        }
    }//setShelter.

    fun removeRecommendationsRxJava(
        shelter: String,
        row: String,
        deleteActionSetSortedString: String,
        deleteActionSetSortedBarString: String,
        deleteActionSetTotalString: String,
    ) {

        disposableRemove.add(
            postRepository.removeRecommendationsRxJava(
                shelter,
                row,
                deleteActionSetSortedString,
                deleteActionSetSortedBarString,
                deleteActionSetTotalString,
            )
                // TH: deploys IO operation on one thread.
                .subscribeOn(
                    Schedulers.io()
                )
                // TH: transforms one Observable into another.
                .map{
                    if(it==0){
                        Log.i("ShelterViewModel:removeRecommendationsRxJava", "Succeeded.")
                    }
                    else{
                        Log.i("ShelterViewModel:removeRecommendationsRxJava", "Failed.")
                    }
                }
                // TH: deploys RxJava operation in another thread.
                .observeOn(
                    AndroidSchedulers.mainThread()
                )
                // TH: helps post LiveData as part of Observable, which then gets observed by Activity.
                .subscribe(
                    {
                        // TH: should have received '0' from back-end.
                        if(it==0) totalRxJava.postValue(it)
                        else totalRxJavaError.postValue(
                            "removeRecommendationsRxJava Error (Back-End)."
                        )
                    },
                    {
                        totalRxJavaError.postValue(
                            "removeRecommendationsRxJava Error: ${it.message}"
                        )
                    }
                )

        )

    }//removeRecommendationsRxJava.

    fun submitRecommendationsRxJava(
        shelterForm: String,
        indexShelterString: String,
        submitActionSetSortedStringProductId: String,
        submitActionSetSortedString: String,
        submitActionSetSortedBarString: String,
        submitActionSetTotalString: String,
        submitActionSetProductIdRemove: String,
        designForTest: Int,
    ) {

        disposableSubmit.add(
            postRepository.submitRecommendationsRxJava(
                shelterForm,
                indexShelterString,
                submitActionSetSortedStringProductId,
                submitActionSetSortedString,
                submitActionSetSortedBarString,
                submitActionSetTotalString,
                submitActionSetProductIdRemove,
            )
                // TH: deploys IO operation on one thread.
                .subscribeOn(
                    Schedulers.io()
                )
                // TH: transforms one Observable into another.
                .map{
                    if(it==0){
                        Log.i("ShelterViewModel:submitRecommendationsRxJava", "Succeeded.")
                    }
                    else{
                        Log.i("ShelterViewModel:submitRecommendationsRxJava", "Failed.")
                    }
                }
                // TH: deploys RxJava operation in another thread.
                .observeOn(
                    AndroidSchedulers.mainThread()
                )
                // TH: helps post LiveData as part of Observable, which then gets observed by Activity.
                .subscribe(
                    {
                        // TH: should have received '0' from back-end.
                        if(it==0) {
                            totalRxJava.postValue(it)
                        }
                        else {
                            totalRxJavaError.postValue(
                                "submitRecommendationsRxJava Error (Back-End)."
                            )
                        }

                        if(designForTest==1) {
                            if (!EspressoIdlingResource.getCounterIdler().isIdleNow) {
                                // TH: resumes test flow.
                                EspressoIdlingResource.decrement()
                            }
                        }//if designForTest.

                    },
                    {
                        totalRxJavaError.postValue(
                            "submitRecommendationsRxJava Error: ${it.message}"
                        )
                    }
                )
        )

    }//submitRecommendationsRxJava.

}