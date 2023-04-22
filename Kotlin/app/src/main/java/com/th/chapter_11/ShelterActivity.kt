package com.th.chapter_11

import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.th.chapter_11.adapter.RecommendationAdapter
import com.th.chapter_11.application.RepositoryApplication
import com.th.chapter_11.db.RecommendationEntity
import com.th.chapter_11.util.EspressoIdlingResource
import com.th.chapter_11.viewmodel.ShelterViewModel
import kotlinx.coroutines.*
import kotlin.system.exitProcess

class ShelterActivity: AppCompatActivity() {

    // TH: represents wait for re-try.
    private val waitTime: Long  = DateUtils.SECOND_IN_MILLIS

    // TH: represents each shelter: row, column.
    private lateinit var shelter: ArrayList<ArrayList<RecommendationEntity>>

    // TH: represents update for shelter: <productId, offset_old, offset_new>.
    private lateinit var shelterUpdate: ArrayList<ArrayList<ShelterUpdate>>

    // TH: represents update for local RecyclerView using shelterUpdate, before submit.
    private lateinit var shelterUpdateRecyclerView: ArrayList<ArrayList<RecommendationEntity>>

    // TH: holds on to cache, update for shelter: 0 as clean, 1 as dirty.
    private lateinit var shelterUpdateCache: ArrayList<Int>

    // TH: helps update local ViewModel, against back-end.
    private lateinit var shelterViewModel: ShelterViewModel

    // TH: represents RecyclerView, RecommendationAdapter per row.
    private lateinit var recyclerView: ArrayList<RecyclerView>
    private lateinit var recommendationAdapter: ArrayList<RecommendationAdapter>

    // TH: helps layout, per shelter.
    private var longest: Int = 0

    // TH: represents dummy recommendationId.
    private var sentinelValue: Int = -1

    // TH: saves selected anchor row, on shelter.
    private var anchoredRow: Int = sentinelValue

    // TH: saves selected anchor index, within selected anchor row.
    private var anchoredIndex: Int = sentinelValue

    // TH: saves anchor as row-elements pairs.
    private lateinit var anchoredRowIndex: ArrayList<ArrayList<Int>>

    // TH: represents productId anchor.
    private lateinit var anchoredProductId: String

    // TH: helps delete unique elements, if selected.
    private lateinit var deleteActionSet: MutableSet<RecommendationEntity>

    // TH: tracks target recommendationIds to be removed, per row.
    private lateinit var deleteActionSetListRemove: ArrayList<ArrayList<Long>>

    // TH: helps anchor unique elements, if selected.
    private lateinit var anchorActionSet: MutableSet<RecommendationEntity>

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // TH: helps clear ALL highlights (i.e. search, insert, delete, add ...).
    private lateinit var productIdClear:  Button

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // TH: helps insert-row mode.
    private lateinit var productIdSetupMode:  Button

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // TH: helps enable mode: highlight.
    private lateinit var productIdHighLightMode:  Button

    // TH: helps highlight, one-by-one.
    private lateinit var productIdHighLightEdit: EditText
    private lateinit var productIdHighLightButton: Button

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // TH: helps enable mode: add.
    private lateinit var productIdAddMode:  Button

    // TH: helps select anchor point for insertion.
    private lateinit var productIdAnchorEdit: EditText
    private lateinit var productIdAnchorButton: Button

    // TH: represents productId to be added (or inserted), with respect to anchor.
    private lateinit var productIdAddEdit: EditText

    // TH: helps add to left of anchor.
    private lateinit var productIdAddLeftButton: Button

    // TH: helps add to right of anchor.
    private lateinit var productIdAddRightButton: Button

    // TH: helps add new row above anchor point.
    private lateinit var productIdAddAboveButton: Button

    // TH: helps add new row below anchor point.
    private lateinit var productIdAddBelowButton: Button

    // TH: helps shift anchor point to left (i.e. inserts dummy on right of anchor).
    private lateinit var productIdShiftLeftButton: Button

    // TH: helps shift anchor point to right (i.e. inserts dummy on left of anchor).
    private lateinit var productIdShiftRightButton: Button

    // TH: helps add action.
    private lateinit var addActionButton: Button

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // TH: helps enable mode: delete.
    private lateinit var productIdDeleteMode:  Button

    // TH: represents productId to be deleted.
    private lateinit var productIdDeleteEdit: EditText

    // TH: helps select mode: single or multiple.
    private lateinit var deleteSingleButton: Button
    private lateinit var deleteMultipleButton: Button
    private lateinit var deleteActionButton: Button

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // TH: helps enable submit user interface to back-end.
    private lateinit var submitActionButton: Button

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // TH: specifies exclusive modes of operation.
    private var modeHighLight: Int = 0
    private var modeSetup: Int = 0
    private var modeAdd: Int = 0
    private var modeDelete: Int = 0

    // TH: specifies exclusive sub-modes of operation.
    private var modeDeleteSingle: Int = 0
    private var modeDeleteMultiple: Int = 0

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // TH: helps receive data from parent Activity (i.e. per shelter).
    companion object {

        const val LAYOUT_DUMMY_URL               = "https://cdn2.thecatapi.com/images/99r.jpg"
        const val LAYOUT_DUMMY_ID_PRODUCT        = "1"
        const val LAYOUT_ROWS_DEFAULT            = "30"
        const val LAYOUT_SHELTER                 = "layout_shelter"
        const val LAYOUT_INSERT_LEFT             = "layout_insert_left"
        const val LAYOUT_INSERT_RIGHT            = "layout_insert_right"
        const val LAYOUT_INTERFACE               = "layout_interface"
        const val LAYOUT_INTERFACE_DEFAULT       = 0
        const val LAYOUT_DESIGN_FOR_TEST         = "0"
        const val LAYOUT_DESIGN_FOR_TEST_DEFAULT = 0

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // TH: retrieves data from parent activity.
        val shelterForm = intent.getStringExtra(LAYOUT_SHELTER)

        if (shelterForm == null) {

            val toast = Toast.makeText(this,getString(R.string.shelter_form_error),Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER,0,0)
            toast.show()

            // TH: crashes immediately.
            exitProcess(1)

        }//if shelterForm==null.

        val interfaceLayout = intent.getIntExtra(LAYOUT_INTERFACE,LAYOUT_INTERFACE_DEFAULT)

        // TH: retrieves debug mode.
        val designForTest = intent.getIntExtra(LAYOUT_DESIGN_FOR_TEST,LAYOUT_DESIGN_FOR_TEST_DEFAULT)
        Log.i(
            "onCreate",
            "designForTest=$designForTest"
        )

        // TH: represents number of rows at run time, for insert-row feature.
        val rows = LAYOUT_ROWS_DEFAULT.toInt()

        // TH: initializes shelter.
        shelter = arrayListOf()

        // TH: initializes update for shelter.
        shelterUpdate = arrayListOf()

        // TH: initializes update for local RecyclerView.
        shelterUpdateRecyclerView = arrayListOf()

        // TH: initializes cache, update for shelter.
        shelterUpdateCache = arrayListOf()

        ////////////////////////////////////////////////////////////////////////////////////////////////

        // TH: retrieves application that owns this activity.
        val postRepository = (application as RepositoryApplication).postRepository

        // TH: retrieves an existing ViewModel or creates new ViewModel in scope.
        // (usually, a fragment or an activity), associated with this ViewModelProvider.
        shelterViewModel  = ViewModelProvider (
            this,
            object : ViewModelProvider.Factory{

                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ShelterViewModel(postRepository) as T
                }

            }
        ).get(ShelterViewModel::class.java)

        // TH: initializes ViewModel.
        shelterViewModel.initShelter()

        for (index in 0 until rows) {

            // TH: retrieves recommendations per (shelter,row) with reactive programming.
            if(designForTest==1) {

                Log.i(
                    "onCreate",
                    "Incremented Before getRecommendationsRxJava."
                )
                // TH: pauses test flow, waits for background (i.e. network request, see below).
                EspressoIdlingResource.increment()

                shelterViewModel.getRecommendationsRxJava(
                    shelterForm,
                    index.toString(),
                    designForTest
                )

                shelterViewModel.recommendationEntityListRxJavaProperty.observe(this){}

            }//if designForTest.

        }//for.

        // TH: issues delay for initial data retrieval from back-end.
        val startTimeInitial = System.currentTimeMillis()
        var elapsedInitial   = System.currentTimeMillis() - startTimeInitial
        while(elapsedInitial<waitTime){
            elapsedInitial = System.currentTimeMillis() - startTimeInitial
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////

        for (index in 0 until rows) {

            // TH: initializes each row.
            // TH: https://stackoverflow.com/questions/33278869/how-do-i-initialize-kotlins-mutablelist-to-empty-mutablelist
            val row = shelterViewModel.getShelter(index)

            // TH: populates shelter, grouped by row.
            // TH: populates shelter.
            shelter.add(index,row)

            // TH: populates update, per row.
            val eachRow = ArrayList<ShelterUpdate>()

            // TH: populates update for local RecyclerView, per row.
            val eachRowRecyclerView = ArrayList<RecommendationEntity>()

            for(eachOffset in row.sortedWith(compareBy { it.offset }).indices) {

                // TH: populates shelterUpdate.
                val update = ShelterUpdate (
                    row[eachOffset].productId.toString(),
                    row[eachOffset].recommendationId.toString(),
                    row[eachOffset].row.toString(),
                    row[eachOffset].offset.toString(),
                    row[eachOffset].offset.toString(),
                )
                eachRow.add(eachOffset,update)

                // TH: populates shelterUpdateRecyclerView.
                val updateRecyclerView = RecommendationEntity (
                    row[eachOffset].recommendationId,
                    row[eachOffset].author,
                    row[eachOffset].row,
                    row[eachOffset].offset,
                    row[eachOffset].shelter,
                    row[eachOffset].productId,
                    row[eachOffset].url
                )
                eachRowRecyclerView.add(eachOffset,updateRecyclerView)

            }//for eachOffset.

            // TH: populates update for shelter.
            shelterUpdate.add(index,eachRow)

            // TH: populates update for local RecyclerView.
            shelterUpdateRecyclerView.add(index,eachRowRecyclerView)

        }//for.

        ////////////////////////////////////////////////////////////////////////////////////////////////

        // TH: releases memory from shelter.
        saveMemoryShelter()

        // TH: releases memory from shelterUpdateRecyclerView.
        saveMemoryShelterUpdateRecyclerView()

        // TH: releases memory from shelterUpdate.
        saveMemoryShelterUpdate()

        // TH: sets all caches as clean, initially.
        for (index in 0 until rows ) {
            shelterUpdateCache.add(index,0)
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////

        /*
        displayShelter();
        displayShelterUpdate("onCreate");
        displayShelterUpdateRecyclerView("onCreate");
        */

        ////////////////////////////////////////////////////////////////////////////////////////////////

        // TH: invokes ViewModel that manipulates LiveData.
        setContentView(R.layout.activity_shelter_split_30)

        // TH: retrieves reference to RecyclerView in layout.
        recyclerView = arrayListOf()
        recyclerView.add(0,findViewById(R.id.activity_shelter_recycler_view_0))
        recyclerView.add(1,findViewById(R.id.activity_shelter_recycler_view_1))
        recyclerView.add(2,findViewById(R.id.activity_shelter_recycler_view_2))
        recyclerView.add(3,findViewById(R.id.activity_shelter_recycler_view_3))
        recyclerView.add(4,findViewById(R.id.activity_shelter_recycler_view_4))
        recyclerView.add(5,findViewById(R.id.activity_shelter_recycler_view_5))
        recyclerView.add(6,findViewById(R.id.activity_shelter_recycler_view_6))
        recyclerView.add(7,findViewById(R.id.activity_shelter_recycler_view_7))
        recyclerView.add(8,findViewById(R.id.activity_shelter_recycler_view_8))
        recyclerView.add(9,findViewById(R.id.activity_shelter_recycler_view_9))
        recyclerView.add(10,findViewById(R.id.activity_shelter_recycler_view_10))
        recyclerView.add(11,findViewById(R.id.activity_shelter_recycler_view_11))
        recyclerView.add(12,findViewById(R.id.activity_shelter_recycler_view_12))
        recyclerView.add(13,findViewById(R.id.activity_shelter_recycler_view_13))
        recyclerView.add(14,findViewById(R.id.activity_shelter_recycler_view_14))
        recyclerView.add(15,findViewById(R.id.activity_shelter_recycler_view_15))
        recyclerView.add(16,findViewById(R.id.activity_shelter_recycler_view_16))
        recyclerView.add(17,findViewById(R.id.activity_shelter_recycler_view_17))
        recyclerView.add(18,findViewById(R.id.activity_shelter_recycler_view_18))
        recyclerView.add(19,findViewById(R.id.activity_shelter_recycler_view_19))
        recyclerView.add(20,findViewById(R.id.activity_shelter_recycler_view_20))
        recyclerView.add(21,findViewById(R.id.activity_shelter_recycler_view_21))
        recyclerView.add(22,findViewById(R.id.activity_shelter_recycler_view_22))
        recyclerView.add(23,findViewById(R.id.activity_shelter_recycler_view_23))
        recyclerView.add(24,findViewById(R.id.activity_shelter_recycler_view_24))
        recyclerView.add(25,findViewById(R.id.activity_shelter_recycler_view_25))
        recyclerView.add(26,findViewById(R.id.activity_shelter_recycler_view_26))
        recyclerView.add(27,findViewById(R.id.activity_shelter_recycler_view_27))
        recyclerView.add(28,findViewById(R.id.activity_shelter_recycler_view_28))
        recyclerView.add(29,findViewById(R.id.activity_shelter_recycler_view_29))

        // TH: retrieves Adapter (i.e. helps bind data to view).
        recommendationAdapter = arrayListOf()
        for (index in 0 until rows) {
            recommendationAdapter.add(
                index,
                RecommendationAdapter(
                    LayoutInflater.from(this), /* obtains layout from given context. */
                    interfaceLayout,                  /* ... */
                    index                             /* ... */
                )
            )
        }

        // TH: initializes sets.
        deleteActionSet = mutableSetOf()
        anchorActionSet = mutableSetOf()

        // TH: initializes ...
        deleteActionSetListRemove = arrayListOf()

        for (row in 0 until rows) {
            deleteActionSetListRemove.add(row,arrayListOf())
        }//for row.

        // TH: assigns Adapter to RecyclerView, per row.
        // TH: implements ClickListener interface, per Adapter.
        for (index in 0 until rows ) {
            recyclerView[index].adapter = recommendationAdapter[index]

            // TH: handles item click.
            // TH: receives Adapter position from ViewHolder.
            // TH: provides explicit implementation.
            // TH: deploys singleton pattern for OnClickListener (i.e. ViewHolder:View).
            recommendationAdapter[index].setOnItemClickListener(
                object: RecommendationAdapter.OnItemClickListener {
                    override fun onItemClick(row: Int, position: Int) {

                        if(modeHighLight==1 && modeAdd==0 && modeDelete==0) {

                            recommendationAdapter[index].toggleRecommendations(position)

                            /*
                            // TH: toggles selection, highlight.
                            val selection = recommendationAdapter[index].toggleRecommendations(position)
                            if(selection==1) {
                                // TH: displays debug, VERY SLOW !!
                                val toast = Toast.makeText(
                                    this@ShelterActivity,
                                    "Row $index Offset $position selected for highlight.",
                                    Toast.LENGTH_LONG
                                )
                                toast.setGravity(Gravity.CENTER, 0, 0)
                                toast.show()
                            }
                            else{
                                // TH: displays debug, VERY SLOW !!
                                val toast = Toast.makeText(
                                    this@ShelterActivity,
                                    "Row $index Offset $position de-selected for highlight.",
                                    Toast.LENGTH_LONG
                                )
                                toast.setGravity(Gravity.CENTER, 0, 0)
                                toast.show()
                            }
                            */

                        }//if modeHighLight, exclusive.

                        else if(modeHighLight==0 && modeAdd==0 && modeDelete==1) {

                            if(recommendationAdapter[index].checkMatchedDelete(position)){

                                if(modeDeleteSingle==1) {

                                    // TH: clears matchedDeleteSelected all rows, notifies data change per row.
                                    // TH: saves and restores previous state, for toggle later.
                                    for (indexShelter in 0 until recommendationAdapter.size) {
                                        recommendationAdapter[indexShelter].clearMatchedDeleteSelected(position)
                                    }

                                    // TH: removes previous delete.
                                    for (rowInternal in 0 until recommendationAdapter.size) {
                                        for(offsetInternal in 0 until recommendationAdapter[rowInternal].itemCount){
                                            recommendationAdapter[rowInternal].clearRecommendationsForDelete(offsetInternal)
                                        }//for offsetInternal.
                                    }//for rowInternal.

                                    deleteActionSet = mutableSetOf()

                                }//if modeDeleteSingle.

                                // TH: toggles selection, delete.
                                val selection = recommendationAdapter[index].toggleRecommendationsForDelete(position)

                                /*
                                // TH: displays debug, VERY SLOW !!
                                val toast = Toast.makeText(
                                    this@ShelterActivity,
                                    if(selection==1) "Row $index Offset $position selected for delete."
                                    else             "Row $index Offset $position de-selected for delete.",
                                    Toast.LENGTH_LONG
                                )
                                toast.setGravity(Gravity.CENTER, 0, 0)
                                toast.show()
                                */

                                // TH: retrieves item for delete.
                                val entityExternal = recommendationAdapter[index].getRecommendations(position)

                                // TH: retrieves deep copy (i.e. beware of reference) !!
                                val entity = RecommendationEntity (
                                    entityExternal.recommendationId,
                                    entityExternal.author,
                                    entityExternal.row,
                                    entityExternal.offset,
                                    entityExternal.shelter,
                                    entityExternal.productId,
                                    entityExternal.url
                                )

                                // TH: if delete mode.
                                if(
                                    (modeDeleteSingle==1 && modeDeleteMultiple==0) ||
                                    (modeDeleteSingle==0 && modeDeleteMultiple==1)
                                ) {

                                    // TH: collects [unique] targets to be deleted (i.e. offset).
                                    if(selection==1) {
                                        deleteActionSet.add(entity)
                                    }
                                    else {
                                        deleteActionSet.remove(entity)
                                    }

                                }// if either deleteSingle or deleteMultiple, exclusive.

                                // TH: invalid user-interface.
                                else {

                                    // TH: displays debug, VERY SLOW !!
                                    val toast0 = Toast.makeText(
                                        this@ShelterActivity,
                                        "INVALID USER-INTERFACE (1) !!", Toast.LENGTH_LONG
                                    )
                                    toast0.setGravity(Gravity.CENTER, 0, 0)
                                    toast0.show()

                                    // TH: crashes immediately.
                                    exitProcess(1)

                                }

                            }//if matchedDelete asserted.

                        }//if modeDelete, exclusive.

                        else if(modeHighLight==0 && modeAdd==1 && modeDelete==0) {

                            if(recommendationAdapter[index].checkMatchedAnchor(position)){

                                // TH: clears matchedAnchorSelected all rows, notifies data change per row.
                                // TH: saves and restores previous state, for toggle later.
                                for (indexShelter in 0 until recommendationAdapter.size) {
                                    recommendationAdapter[indexShelter].clearMatchedAnchorSelected(position)
                                }

                                // TH: removes previous anchor.
                                for (rowInternal in 0 until recommendationAdapter.size) {
                                    for(offsetInternal in 0 until recommendationAdapter[rowInternal].itemCount){
                                        recommendationAdapter[rowInternal].clearRecommendationsForAnchor(offsetInternal)
                                    }//for offsetInternal.
                                }//for rowInternal.

                                // TH: allows ONLY one anchor point per insertion.
                                anchorActionSet = mutableSetOf()

                                // TH: retrieves item for anchor.
                                val entity = recommendationAdapter[index].getRecommendations(position)

                                // TH: toggles selection, anchor.
                                val selection = recommendationAdapter[index].toggleRecommendationsForAnchor(position)

                                // TH: indicates anchor selected.
                                if(selection==1){

                                    // TH: collects [unique] anchors.
                                    anchorActionSet.add(entity)

                                    // TH: retrieves anchor row.
                                    anchoredRow   = entity.row

                                    // TH: captures locally updated offset, before submit.
                                    anchoredIndex = entity.offset

                                }
                                else{
                                    // TH: indicates NO anchor selected.
                                    anchorActionSet.remove(entity)
                                    anchoredRow   = sentinelValue
                                    anchoredIndex = sentinelValue
                                }

                                if(anchoredRow != sentinelValue){

                                    /*
                                    // TH: displays debug, VERY SLOW !!
                                    val toast = Toast.makeText(
                                        this@ShelterActivity,
                                        if(selection==1) "Row $anchoredRow Offset $anchoredIndex selected for anchor."
                                        else             "Row $anchoredRow Offset $anchoredIndex de-selected for anchor.",
                                        Toast.LENGTH_LONG
                                    )
                                    toast.setGravity(Gravity.CENTER, 0, 0)
                                    toast.show()
                                    */

                                    /*
                                    // TH: compares for equality (i.e. check-point, debug) --- IMPORTANT.
                                    if(
                                        recommendationAdapter[anchoredRow].getRecommendations()
                                        == shelterUpdateRecyclerView[anchoredRow]
                                    ){
                                        // TH: displays debug, VERY SLOW !!
                                        val toast0 = Toast.makeText(
                                            this@ShelterActivity,
                                            "In Order: Adapter versus ShelterUpdateRecyclerView.",
                                            Toast.LENGTH_LONG
                                        )
                                        toast0.setGravity(Gravity.CENTER, 0, 0)
                                        toast0.show()
                                    }//if equal.
                                    else{
                                        // TH: displays debug, VERY SLOW !!
                                        val toast1 = Toast.makeText(
                                            this@ShelterActivity,
                                            "NOT In Order: Adapter versus ShelterUpdateRecyclerView.",
                                            Toast.LENGTH_LONG
                                        )
                                        toast1.setGravity(Gravity.CENTER, 0, 0)
                                        toast1.show()
                                    }
                                    */

                                }
                                else{

                                    // TH: displays debug, VERY SLOW !!
                                    val toast = Toast.makeText(
                                        this@ShelterActivity,
                                        "NO anchor selected.",
                                        Toast.LENGTH_LONG
                                    )
                                    toast.setGravity(Gravity.CENTER, 0, 0)
                                    toast.show()

                                }

                            }//if already anchored (i.e. productId matched), but NOT selected.

                        }//if modeAdd, exclusive.

                        else {

                            // TH: displays debug, VERY SLOW !!
                            val toast = Toast.makeText(
                                this@ShelterActivity,
                                "INVALID USER-INTERFACE (0) !!", Toast.LENGTH_LONG
                            )
                            toast.setGravity(Gravity.CENTER, 0, 0)
                            toast.show()

                            // TH: crashes immediately.
                            exitProcess(1)

                        }//else: invalid user-interface.

                    }//onItemClick.
                }//OnItemClickListener.
            )//setOnItemClickListener.

        }// for index.

        // TH: retrieves data per row.
        for (index in 0 until shelter.size) {
            recommendationAdapter[index].updateRecommendations(
                shelterViewModel.getShelterFragment(index)
            )
        }

        // TH: retrieves longest row.
        longest = 0
        for (index in 0 until shelter.size) {
            longest = if(longest<shelter[index].size) shelter[index].size else longest
        }

        // TH: assigns LayoutManager to RecyclerView, per row.
        // TH: assumes uniform dimension.
        for (index in 0 until shelter.size) {
            // TH: deploys GridLayoutManager.
            recyclerView[index].layoutManager = GridLayoutManager(
                this,
                longest,
                GridLayoutManager.VERTICAL,false
            )
        }

        // TH: adds borders around RecyclerView items.
        // TH: https://syntaxbytetutorials.com/android-recyclerview-divider/
        // TH: https://stackoverflow.com/questions/51075150/how-to-set-border-for-android-recyclerview-grid-layout
        for (index in 0 until shelter.size) {
            recyclerView[index]
                .apply {
                    addItemDecoration(
                        DividerItemDecoration(
                            this.context,
                            DividerItemDecoration.HORIZONTAL
                        )
                    )
                }
                .apply {
                    addItemDecoration(
                        DividerItemDecoration(
                            this.context,
                            DividerItemDecoration.VERTICAL
                        )
                    )
                }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////

        productIdClear           = findViewById(R.id.button_clear)

        productIdSetupMode       = findViewById(R.id.button_setup)

        productIdHighLightMode   = findViewById(R.id.button_mode_highlight)
        productIdHighLightEdit   = findViewById(R.id.productIdHighLight)
        productIdHighLightButton = findViewById(R.id.button_highlight_productId)

        productIdAddMode         = findViewById(R.id.button_mode_add)
        productIdAnchorEdit      = findViewById(R.id.productIdAnchor)
        productIdAnchorButton    = findViewById(R.id.button_anchor_productId)

        productIdAddEdit          = findViewById(R.id.productIdAdd)
        productIdAddLeftButton    = findViewById(R.id.button_addLEFT_productId)
        productIdAddRightButton   = findViewById(R.id.button_addRIGHT_productId)
        productIdAddAboveButton   = findViewById(R.id.button_add_above)
        productIdAddBelowButton   = findViewById(R.id.button_add_below)
        productIdShiftLeftButton  = findViewById(R.id.button_shift_left)
        productIdShiftRightButton = findViewById(R.id.button_shift_right)
        addActionButton           = findViewById(R.id.button_add_action)

        productIdDeleteMode      = findViewById(R.id.button_mode_delete)
        productIdDeleteEdit      = findViewById(R.id.productIdDelete)
        deleteSingleButton       = findViewById(R.id.button_delete_single)
        deleteMultipleButton     = findViewById(R.id.button_delete_multiple)
        deleteActionButton       = findViewById(R.id.button_delete_action)

        submitActionButton       = findViewById(R.id.button_mode_submit)

        // TH: initializes FSM.
        initializeStateMachine()

        ////////////////////////////////////////////////////////////////////////////////////////////////

        productIdClear.setOnClickListener {

            clearAll()
            productIdSetupMode.isEnabled = true
            productIdHighLightMode.isEnabled = true
            productIdAddMode.isEnabled = true
            productIdDeleteMode.isEnabled = true

            // TH: enables 'Submit' only when necessary.
            submitActionButton.isEnabled = compareShelterUpdateAgainstShelter()

            // TH: displays mode.
            displayModeAll()

            // TH: clears highlights per row.
            for (rowInternal in 0 until recommendationAdapter.size) {
                recommendationAdapter[rowInternal].clearRecommendations()
            }//for rowInternal.

            // TH: clears out all items for delete.
            deleteActionSet = mutableSetOf()

            // TH: clears out all items for anchor.
            anchorActionSet  = mutableSetOf()
            anchoredRow      = sentinelValue
            anchoredIndex    = sentinelValue
            anchoredRowIndex = arrayListOf()

        }// clear.

        ////////////////////////////////////////////////////////////////////////////////////////////////

        productIdSetupMode.setOnClickListener{

            if(modeSetup==0){
                clearAll()
                enableSetup()
                hideModeButSetup()
                productIdSetupMode.isEnabled = true
            }
            else{
                disableSetup()
                productIdHighLightMode.isEnabled = true
                productIdAddMode.isEnabled = true
                productIdDeleteMode.isEnabled = true

                // TH: enables 'Submit' only when necessary.
                submitActionButton.isEnabled = compareShelterUpdateAgainstShelter()

                // TH: displays mode.
                displayModeAll()

            }

            if(modeSetup==1){

                val toast = Toast.makeText(
                    this,
                    "Mode Setup: $rows rows.",
                    Toast.LENGTH_LONG
                )
                toast.setGravity(Gravity.CENTER,0,0)
                toast.show()

                // TH: resets longest during setup.
                longest=0

                // --------------------------------------------------------------------

                // TH: gets ready for next round of delete multiple.
                deleteActionSet = mutableSetOf()

                Log.i(
                    "productIdSetupMode",
                    "shelter.size=${shelter.size}."
                )

                for(index in 0 until shelter.size) {

                    Log.i(
                        "productIdSetupMode",
                        "shelter[$index].size=${shelter[index].size}."
                    )
                    displayShelter()

                    for(position in 0 until shelter[index].size) {

                        // TH: retrieves item for delete.
                        val entityExternal = shelter[index][position]

                        // TH: retrieves deep copy (i.e. beware of reference) !!
                        val entity = RecommendationEntity (
                            entityExternal.recommendationId,
                            entityExternal.author,
                            entityExternal.row,
                            entityExternal.offset,
                            entityExternal.shelter,
                            entityExternal.productId,
                            entityExternal.url
                        )

                        deleteActionSet.add(entity)

                    }//for position.
                }//for index.

                // TH: initializes ...
                deleteActionSetListRemove = arrayListOf()

                for (row in 0 until rows) {
                    deleteActionSetListRemove.add(row,arrayListOf())
                }//for row.

                // TH: sorts delete set, ascending.
                val deleteActionSetList = deleteActionSet.sortedWith(compareBy({it.row},{it.offset}))

                // TH: updates items to be deleted, by row.
                updateDeleteActionSetListRemove(deleteActionSetList)

                // TH: gets ready for next round of delete multiple.
                deleteActionSet = mutableSetOf()

                // --------------------------------------------------------------------

                for (index in 0 until 1) {

                    // TH: populates update, per row.
                    val eachRow = ArrayList<ShelterUpdate>()

                    // TH: populates update for local RecyclerView, per row.
                    val eachRowRecyclerView = ArrayList<RecommendationEntity>()

                    // TH: populates shelterUpdate.
                    val update = ShelterUpdate (
                        "1",
                        sentinelValue.toString(),
                        index.toString(),
                        0.toString(),
                        0.toString(),
                    )
                    eachRow.add(0,update)

                    // TH: populates shelterUpdateRecyclerView.
                    val updateRecyclerView = RecommendationEntity (
                        sentinelValue.toLong(),
                        "Dummy-Author",
                        index,
                        0,
                        shelterForm,
                        1,
                        LAYOUT_DUMMY_URL
                    )
                    eachRowRecyclerView.add(0,updateRecyclerView)

                    // TH: populates update for shelter, per row.
                    if(shelterUpdate.size>index){
                        shelterUpdate[index]=eachRow
                    }
                    else{
                        shelterUpdate.add(index,eachRow)
                    }

                    // TH: populates update for local RecyclerView.
                    if(shelterUpdateRecyclerView.size>index){
                        shelterUpdateRecyclerView[index]=eachRowRecyclerView
                    }
                    else{
                        shelterUpdateRecyclerView.add(index,eachRowRecyclerView)
                    }

                    // TH: updates local RecyclerView.
                    recommendationAdapter[index].updateRecommendations(
                        shelterUpdateRecyclerView[index]
                    )

                    // TH: updates longest row, after local insert.
                    if(longest<shelterUpdateRecyclerView[index].size){
                        longest = shelterUpdateRecyclerView[index].size
                    }

                    // TH: marks cache as dirty.
                    shelterUpdateCache[index]=1

                }//for row 0.

                for (index in 1 until shelterUpdate.size) {

                    shelterUpdate[index]=arrayListOf()
                    shelterUpdateRecyclerView[index]=arrayListOf()

                    saveMemoryShelterViewModel(shelterViewModel,index)

                    // TH: updates local RecyclerView.
                    recommendationAdapter[index].updateRecommendations(
                        shelterUpdateRecyclerView[index]
                    )

                    // TH: updates longest row, after local insert.
                    if(longest<shelterUpdateRecyclerView[index].size){
                        longest = shelterUpdateRecyclerView[index].size
                    }

                    // TH: marks cache as dirty.
                    shelterUpdateCache[index]=1

                }//for remaining rows.

                saveMemoryShelterUpdate()
                saveMemoryShelterUpdateRecyclerView()

                // TH: assigns LayoutManager to RecyclerView, per row.
                // TH: assumes uniform dimension.
                for (index in 0 until rows) {
                    // TH: deploys GridLayoutManager.
                    recyclerView[index].layoutManager = GridLayoutManager(
                        this,
                        longest,
                        GridLayoutManager.VERTICAL,
                        false
                    )
                }//for index.

            }//if mode activated.

            // TH: clears highlights per row.
            for (rowInternal in 0 until recommendationAdapter.size) {
                recommendationAdapter[rowInternal].clearRecommendations()
            }//for rowInternal.

        }// setup.

        ////////////////////////////////////////////////////////////////////////////////////////////////

        productIdHighLightMode.setOnClickListener {
            if(modeHighLight==0) {
                clearAll()
                enableHighLight()
                hideModeButHighlight()
                productIdHighLightMode.isEnabled = true
            }
            else {

                disableHighLight()
                productIdSetupMode.isEnabled = true
                productIdAddMode.isEnabled = true
                productIdDeleteMode.isEnabled = true

                // TH: enables 'Submit' only when necessary.
                submitActionButton.isEnabled = compareShelterUpdateAgainstShelter()

                // TH: displays mode.
                displayModeAll()

                // TH: clears highlights per row.
                for (rowInternal in 0 until recommendationAdapter.size) {
                    recommendationAdapter[rowInternal].clearRecommendations()
                }//for rowInternal.

            }
        }

        productIdHighLightButton.setOnClickListener {

            val productIdForm = productIdHighLightEdit.text.toString().trim()

            if(productIdForm.isEmpty()){
                val toast = Toast.makeText(
                    this,
                    getString(R.string.productId_form_error),
                    Toast.LENGTH_LONG
                )
                toast.setGravity(Gravity.CENTER,0,0)
                toast.show()
            } else{
                // TH: searches for productId, as String.
                for (index in 0 until recommendationAdapter.size) {
                    recommendationAdapter[index].searchRecommendations(productIdForm)
                }
            }

            // TH: helps de-bounce button push.
            productIdHighLightEdit.text.clear()

        }// highlight (search).

        ////////////////////////////////////////////////////////////////////////////////////////////////

        productIdAddMode.setOnClickListener {
            if(modeAdd==0) {
                clearAll()
                enableAdd()
                hideModeButAdd()
                productIdAddMode.isEnabled = true
            }
            else {

                disableAdd()
                productIdSetupMode.isEnabled = true
                productIdHighLightMode.isEnabled = true
                productIdDeleteMode.isEnabled = true

                // TH: enables 'Submit' only when necessary.
                submitActionButton.isEnabled = compareShelterUpdateAgainstShelter()

                // TH: displays mode.
                displayModeAll()

                // TH: clears highlights per row.
                for (rowInternal in 0 until recommendationAdapter.size) {
                    recommendationAdapter[rowInternal].clearRecommendations()
                }//for rowInternal.

            }
        }

        // TH: saves selected anchor row, on shelter.
        anchoredRow = sentinelValue

        // TH: saves selected anchor index, within selected anchor row.
        anchoredIndex = sentinelValue

        // TH: saves anchor as row-elements pairs.
        anchoredRowIndex = arrayListOf()

        productIdAnchorButton.setOnClickListener {

            val productIdForm   = productIdAnchorEdit.text.toString().trim()

            if(productIdForm.isEmpty()){
                val toast = Toast.makeText(
                    this,
                    getString(R.string.productId_anchor_error_empty),
                    Toast.LENGTH_LONG
                )
                toast.setGravity(Gravity.CENTER,0,0)
                toast.show()
            }
            else{

                // TH: saves anchor as row-elements pairs.
                anchoredRowIndex = arrayListOf()

                for (index in 0 until recommendationAdapter.size) {
                    val anchoredIndexIterator: ArrayList<Int> =
                        recommendationAdapter[index].selectRecommendationsForAnchor(productIdForm)
                    if(anchoredIndexIterator.size>0) {
                        anchoredRowIndex.add(index,anchoredIndexIterator)
                    }
                    else{
                        anchoredRowIndex.add(index,arrayListOf())
                    }
                }

                for (index in 0 until anchoredRowIndex.size) {

                    if(anchoredRowIndex[index].size==0) continue
                    else{
                        // TH: latches in most recent valid anchor.
                        anchoredProductId = productIdForm
                        /*
                        val toast = Toast.makeText(
                            this,
                            "$anchoredProductId at $index:${anchoredRowIndex[index]}",
                            Toast.LENGTH_LONG
                        )
                        toast.setGravity(Gravity.CENTER,0,0)
                        toast.show()
                        */
                    }

                }//for index.

            }

            // TH: helps de-bounce button push.
            productIdAnchorEdit.text.clear()

        }// anchor.

        ////////////////////////////////////////////////////////////////////////////////////////////////

        productIdAddLeftButton.setOnClickListener {

            val productIdForm = productIdAddEdit.text.toString().trim()

            // TH: adds left (i.e. 0) or right (i.e. 1).
            addLeftOrRight(rows, shelterForm, productIdForm, 0)

            // TH: helps de-bounce button push.
            productIdAddEdit.text.clear()

        }// addLeft.

        productIdShiftRightButton.setOnClickListener {

            val productIdForm = LAYOUT_DUMMY_ID_PRODUCT

            if(anchoredRow==sentinelValue || anchoredIndex==sentinelValue){

                val toast = Toast.makeText(
                    this,
                    getString(R.string.productId_anchor_error_select),
                    Toast.LENGTH_LONG
                )
                toast.setGravity(Gravity.CENTER,0,0)
                toast.show()

                return@setOnClickListener

            }//if NO anchor.

            // TH: saves global as local.
            val anchoredRowLocal   = anchoredRow
            val anchoredIndexLocal = anchoredIndex

            // TH: adds left (i.e. 0) or right (i.e. 1).
            addLeftOrRight(rows, shelterForm, productIdForm, 0)

            // TH: restores global.
            anchoredRow   = anchoredRowLocal
            anchoredIndex = anchoredIndexLocal

            // TH: shiftLeft  == addRight at anchored row -> terminal == 0.
            // TH: shiftRight == addLeft  at anchored row -> terminal == 1.
            shiftLeftOrRightUpdateAnchor(1, rows)

            /*
            displayShelterUpdate("shiftRight")
            */

        }// shiftRight.

        ////////////////////////////////////////////////////////////////////////////////////////////////

        productIdAddRightButton.setOnClickListener {

            val productIdForm = productIdAddEdit.text.toString().trim()

            Log.i(
                "productIdAddRightButton-addLeftOrRight",
                "adding $productIdForm to right of (${anchoredRow},${anchoredIndex})."
            )

            // TH: adds left (i.e. 0) or right (i.e. 1).
            addLeftOrRight(rows, shelterForm, productIdForm, 1)

            // TH: helps de-bounce button push.
            productIdAddEdit.text.clear()

        }// addRight.

        productIdShiftLeftButton.setOnClickListener {

            val productIdForm = LAYOUT_DUMMY_ID_PRODUCT

            if(anchoredRow==sentinelValue || anchoredIndex==sentinelValue){

                val toast = Toast.makeText(
                    this,
                    getString(R.string.productId_anchor_error_select),
                    Toast.LENGTH_LONG
                )
                toast.setGravity(Gravity.CENTER,0,0)
                toast.show()

                return@setOnClickListener

            }//if NO anchor.

            // TH: saves global as local.
            val anchoredRowLocal   = anchoredRow
            val anchoredIndexLocal = anchoredIndex

            // TH: adds left (i.e. 0) or right (i.e. 1).
            addLeftOrRight(rows, shelterForm, productIdForm, 1)

            // TH: fixes global (i.e. to be restored later).
            // TH: helps add dummy at index 0, per iteration.
            anchoredIndex = 0

            for(eachRow in 0 until shelterUpdate.size){

                // TH: shifts each row to left, except for anchored row.
                anchoredRow = eachRow

                if(anchoredRow==anchoredRowLocal) continue

                // TH: adds left (i.e. 0) or right (i.e. 1).
                addLeftOrRight(rows, shelterForm, productIdForm, 0)

            }//for eachRow.

            // TH: restores global.
            anchoredRow   = anchoredRowLocal
            anchoredIndex = anchoredIndexLocal

            // TH: shiftLeft  == addRight at anchored row -> terminal == 0.
            // TH: shiftRight == addLeft  at anchored row -> terminal == 1.
            shiftLeftOrRightUpdateAnchor(0, rows)

            /*
            displayShelterUpdate("shiftLeft")
            */

        }// shiftLeft.

        ////////////////////////////////////////////////////////////////////////////////////////////////

        productIdAddAboveButton.setOnClickListener {

            if(anchoredRow==sentinelValue){

                val toast = Toast.makeText(
                    this,
                    getString(R.string.productId_anchor_error_select),
                    Toast.LENGTH_LONG
                )
                toast.setGravity(Gravity.CENTER,0,0)
                toast.show()

            }//if anchor NOT selected.
            else{

                /*
                displayShelterUpdate("productIdAddAboveButton-before")
                displayShelterUpdateRecyclerView("productIdAddAboveButton-before")
                */

                // TH: implements internal add above (0) or below (1).
                addAboveOrBelow(shelterForm, 0)

                // TH: adjusts anchor point, after insert above.
                anchoredRow  += 0
                anchoredIndex = 0

                // TH: shiftLeft  == addRight at anchored row -> terminal == 0.
                // TH: shiftRight == addLeft  at anchored row -> terminal == 1.
                // TH: helps select anchor point, after insert above.
                shiftLeftOrRightUpdateAnchor(0, rows)

                /*
                displayShelterUpdate("productIdAddAboveButton-after")
                displayShelterUpdateRecyclerView("productIdAddAboveButton-after")
                */

            }//else anchor selected.

            // TH: helps de-bounce button push.
            productIdAddEdit.text.clear()

        }// addAbove.

        ////////////////////////////////////////////////////////////////////////////////////////////////

        productIdAddBelowButton.setOnClickListener {

            if(anchoredRow==sentinelValue){

                val toast = Toast.makeText(
                    this,
                    getString(R.string.productId_anchor_error_select),
                    Toast.LENGTH_LONG
                )
                toast.setGravity(Gravity.CENTER,0,0)
                toast.show()

            }//if anchor NOT selected.
            else{

                /*
                displayShelterUpdate("productIdAddBelowButton-before")
                displayShelterUpdateRecyclerView("productIdAddBelowButton-before")
                */

                // TH: implements internal add above (0) or below (1).
                addAboveOrBelow(shelterForm, 1)

                // TH: adjusts anchor point, after insert below.
                anchoredRow  += 1
                anchoredIndex = 0

                // TH: shiftLeft  == addRight at anchored row -> terminal == 0.
                // TH: shiftRight == addLeft  at anchored row -> terminal == 1.
                // TH: helps select anchor point, after insert below.
                shiftLeftOrRightUpdateAnchor(0, rows)

                /*
                displayShelterUpdate("productIdAddBelowButton-after")
                displayShelterUpdateRecyclerView("productIdAddBelowButton-after")
                */

            }//else anchor selected.

            // TH: helps de-bounce button push.
            productIdAddEdit.text.clear()

        }// addBelow.

        ////////////////////////////////////////////////////////////////////////////////////////////////

        addActionButton.setOnClickListener {

            // TH: generates URl string parameters (i.e. target rows).
            var indexShelterString = ""

            // TH: generates URl string parameters (i.e. target productIds to be added).
            var addActionSetSortedStringProductId = ""

            // TH: generates URl string parameters (i.e. target offsets to be added).
            var addActionSetSortedString = ""

            // TH: generates URl string parameters.
            // TH: represents target recommendationIds of remaining elements.
            var addActionSetSortedBarString = ""

            // TH: generates URl string parameters.
            // TH: represents final indices of remaining elements.
            var addActionSetTotalString = ""

            var updateRequired = false

            // TH: compares user-interface (i.e. shelterUpdate) against back-end (i.e. shelter).
            // TH: handles cases where entire row recently added or deleted.
            if(shelterUpdate.size!=shelter.size) { updateRequired = true }//if size.

            for(eachRow in shelterUpdate.indices) {

                val updateRequiredEachRow = if(eachRow<shelter.size) {

                    if(compareShelterUpdateAgainstShelter(eachRow)==0) {
                        // TH: indicates existing row remains same.
                        continue
                    }
                    else {
                        // TH: indicates existing row already changed content.
                        true
                    }
                }
                else {
                    // TH: indicates additional row already inserted.
                    true
                }

                // TH: continues iteration if NO change.
                if(!updateRequiredEachRow) {
                    continue
                }
                else{

                    updateRequired = true
                    Log.i(
                        "addActionButton",
                        "Row $eachRow Needs Update (Add)."
                    )

                    // TH: represents target productIds to be added.
                    val addActionSetSortedProductId = arrayListOf<Long>()

                    // TH: represents target offsets to be added.
                    val addActionSetSorted = arrayListOf<Int>()

                    // TH: represents target recommendationIds of remaining elements.
                    val addActionSetSortedBar = arrayListOf<Long>()

                    // TH: represents final indices of remaining elements.
                    val addActionSetTotal = arrayListOf<Int>()

                    // TH: collects updates per row.
                    for(offset in shelterUpdate[eachRow].indices){

                        // TH: collects new items.
                        if(
                            shelterUpdate[eachRow][offset].getRecommendationId().toLong()
                            ==sentinelValue.toLong()
                        ){

                            addActionSetSortedProductId.add(
                                shelterUpdate[eachRow][offset].getProductId().toLong()
                            )

                            addActionSetSorted.add(
                                shelterUpdate[eachRow][offset].getOffsetNew().toInt()
                            )

                        }//if new items.

                        else{

                            // TH: skips if same position.
                            if(
                                shelterUpdate[eachRow][offset].getOffsetOld()
                                == shelterUpdate[eachRow][offset].getOffsetNew()
                            ){
                                continue
                            }//if.
                            else{

                                addActionSetSortedBar.add(
                                    shelterUpdate[eachRow][offset].getRecommendationId().toLong()
                                )

                                addActionSetTotal.add(
                                    shelterUpdate[eachRow][offset].getOffsetNew().toInt()
                                )

                            }//else new position.

                        }//else remaining items.

                    }//for offset.

                    // TH: specifies delimiter, per row.
                    indexShelterString += "AND-$eachRow-"

                    // TH: specifies delimiter, per row.
                    addActionSetSortedStringProductId+="AND-"
                    for(element in addActionSetSortedProductId){
                        addActionSetSortedStringProductId+= "$element-"
                    }

                    // TH: specifies delimiter, per row.
                    addActionSetSortedString+="AND-"
                    for(element in addActionSetSorted){
                        addActionSetSortedString+= "$element-"
                    }

                    // TH: specifies delimiter, per row.
                    addActionSetSortedBarString+="AND-"
                    for(element in addActionSetSortedBar){
                        addActionSetSortedBarString+= "$element-"
                    }

                    // TH: specifies delimiter, per row.
                    addActionSetTotalString+="AND-"
                    for(element in addActionSetTotal){
                        addActionSetTotalString+= "$element-"
                    }

                }//else.

            }//for eachRow.

            if(updateRequired){

                Log.i(
                    "addActionButton",
                    "User Interface != Back End."
                )

                Log.i("addActionButton:indexShelterString                ", indexShelterString)
                Log.i("addActionButton:addActionSetSortedStringProductId ", addActionSetSortedStringProductId)
                Log.i("addActionButton:addActionSetSortedString          ", addActionSetSortedString)
                Log.i("addActionButton:addActionSetSortedBarString       ", addActionSetSortedBarString)
                Log.i("addActionButton:addActionSetTotalString           ", addActionSetTotalString)

            }//if updateRequired.
            else{

                Log.i(
                    "addActionButton",
                    "User Interface == Back End."
                )

            }//else.

        }//addAction.

        ////////////////////////////////////////////////////////////////////////////////////////////////

        productIdDeleteMode.setOnClickListener {

            if(modeDelete==0){
                clearAll()
                enableDelete()
                hideModeButDelete()
                productIdDeleteMode.isEnabled = true
            }
            else{

                disableDelete()
                productIdSetupMode.isEnabled = true
                productIdHighLightMode.isEnabled = true
                productIdAddMode.isEnabled = true

                // TH: enables 'Submit' only when necessary.
                submitActionButton.isEnabled = compareShelterUpdateAgainstShelter()

                // TH: displays mode.
                displayModeAll()

                // TH: clears highlights per row.
                for (rowInternal in 0 until recommendationAdapter.size) {
                    recommendationAdapter[rowInternal].clearRecommendations()
                }//for rowInternal.

            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////

        deleteSingleButton.setOnClickListener {

            // TH: clears highlights per row.
            for (rowInternal in 0 until recommendationAdapter.size) {
                recommendationAdapter[rowInternal].clearRecommendations()
            }//for rowInternal.

            // TH: clears out all items for delete.
            deleteActionSet = mutableSetOf()

            // TH: toggles selection, with color change.
            if(modeDeleteSingle==0){

                modeDeleteSingle = 1
                deleteSingleButton.isEnabled  = true
                deleteSingleButton.isSelected = true

                // TH: disables other buttons.
                modeDeleteMultiple = 0
                deleteMultipleButton.isEnabled  = false
                deleteMultipleButton.isSelected = false

                // TH: enables action button.
                deleteActionButton.isEnabled  = true
                deleteActionButton.isSelected = false
                deleteActionButton.visibility = View.VISIBLE

                markForDelete(shelterForm)

            }
            else{

                modeDeleteSingle = 0
                deleteSingleButton.isEnabled  = true
                deleteSingleButton.isSelected = false

                // TH: enables other buttons.
                modeDeleteMultiple = 0
                deleteMultipleButton.isEnabled  = true
                deleteMultipleButton.isSelected = false

                // TH: disables action button.
                deleteActionButton.isEnabled  = false
                deleteActionButton.isSelected = false
                deleteActionButton.visibility = View.GONE

            }

        }//deleteSingle.

        ////////////////////////////////////////////////////////////////////////////////////////////////

        deleteMultipleButton.setOnClickListener {

            // TH: clears highlights per row.
            for (rowInternal in 0 until recommendationAdapter.size) {
                recommendationAdapter[rowInternal].clearRecommendations()
            }//for rowInternal.

            // TH: clears out all items for delete.
            deleteActionSet = mutableSetOf()

            // TH: toggles selection, with color change.
            if(modeDeleteMultiple==0){

                modeDeleteMultiple = 1
                deleteMultipleButton.isEnabled  = true
                deleteMultipleButton.isSelected = true

                // TH: disables other buttons.
                modeDeleteSingle = 0
                deleteSingleButton.isEnabled  = false
                deleteSingleButton.isSelected = false

                // TH: enables action button.
                deleteActionButton.isEnabled  = true
                deleteActionButton.isSelected = false
                deleteActionButton.visibility = View.VISIBLE

                markForDelete(shelterForm)

            }
            else{

                modeDeleteMultiple = 0
                deleteMultipleButton.isEnabled  = true
                deleteMultipleButton.isSelected = false

                // TH: enables other buttons.
                modeDeleteSingle = 0
                deleteSingleButton.isEnabled  = true
                deleteSingleButton.isSelected = false

                // TH: disables action button.
                deleteActionButton.isEnabled  = false
                deleteActionButton.isSelected = false
                deleteActionButton.visibility = View.GONE

            }

        }//deleteMultiple.

        ////////////////////////////////////////////////////////////////////////////////////////////////

        deleteActionButton.setOnClickListener {

            if(designForTest==1){
                Log.i(
                    "deleteActionButton",
                    "Started."
                )
            }//if designForTest.

            /*
            displayDeleteActionSetListRemove("deleteActionButton-before")
            */

            // TH: generates URl string parameters (i.e. target rows).
            var indexShelterString = ""

            // TH: generates URl string parameters (i.e. target productIds to be added).
            var deleteActionSetSortedStringProductId = ""

            // TH: generates URl string parameters (i.e. target offsets to be added).
            var deleteActionSetSortedString = ""

            // TH: generates URl string parameters.
            // TH: represents target recommendationIds of remaining elements.
            var deleteActionSetSortedBarString = ""

            // TH: generates URl string parameters.
            // TH: represents final indices of remaining elements.
            var deleteActionSetTotalString = ""

            // TH: helps track debug statements.
            var deleteActionSetEmpty = false

            if(deleteActionSet.size==0){

                deleteActionSetEmpty = true

                val toast = Toast.makeText(
                    this,
                    getString(R.string.productId_remove_actionSet_empty),
                    Toast.LENGTH_LONG
                )
                toast.setGravity(Gravity.CENTER,0,0)
                toast.show()

                if(designForTest==1){

                    Log.i(
                        "deleteActionButton",
                        "Ended (Error, 0)."
                    )

                    /*
                    // TH: crashes immediately.
                    exitProcess(1)
                    */

                }//if designForTest.

            }
            else{

                // TH: updates running index (i.e. gets incremented after every shift).
                val itemOffsetMap: MutableMap<Int,Int> = mutableMapOf()
                for (item in deleteActionSet){
                    itemOffsetMap[item.row] = 0
                }

                Log.i(
                    "deleteActionButton",
                    "HERE_0."
                )

                /*
                displayShelterUpdate("deleteActionButton-before")
                displayShelterUpdateRecyclerView("deleteActionButton-before")
                */

                // TH: sorts delete set, ascending.
                val deleteActionSetList =
                    deleteActionSet.sortedWith(compareBy({it.row},{it.offset})).toMutableList()

                // TH: updates items to be deleted, by row.
                updateDeleteActionSetListRemove(deleteActionSetList)

                /*
                displayDeleteActionSetListRemove("deleteActionButton-before-0")
                */

                // TH: generates URl string parameters (i.e. target recommendationIds to be removed).
                var deleteActionSetProductIdRemove = ""

                // TH: specifies delimiter, per row.
                for(eachRow in 0 until deleteActionSetListRemove.size){

                    // TH: skips if empty.
                    if(deleteActionSetListRemove[eachRow].size==0) continue

                    deleteActionSetProductIdRemove +="AND-"
                    for(element in deleteActionSetListRemove[eachRow]){
                        deleteActionSetProductIdRemove+= "$element-"
                    }//for element.

                }//for eachRow.

                // TH: updates local copy, based on delete set.
                // TH: https://www.geeksforgeeks.org/kotlin-mutablesetof-method/

                // TH: helps update by reference (i.e. due to shift-up).
                for (itemIndex in 0 until deleteActionSetList.size){

                    val item = deleteActionSetList[itemIndex]

                    Log.i(
                        "deleteActionButton",
                        "productId=${item.productId}, row=${item.row}, offset=${item.offset}."
                    )

                    Log.i(
                        "deleteActionButton",
                        "HERE_1A."
                    )

                    // TH: marks cache as dirty.
                    shelterUpdateCache[item.row]=1

                    Log.i(
                        "deleteActionButton",
                        "HERE_1B."
                    )

                    // TH: enables increment for running index.
                    var itemOffsetMapBoolean = false

                    // TH: holds indices to be removed.
                    val offsetRemoved: ArrayList<Int> = arrayListOf()

                    // TH: tracks running offset, per row.
                    // TH: handles NullPointerException (i.e. continues if null).
                    val itemOffsetAdjusted = itemOffsetMap[item.row] ?: continue

                    Log.i(
                        "deleteActionButton",
                        "HERE_1C."
                    )

                    Log.i(
                        "deleteActionButton (before)",
                        "shelterUpdate[${item.row}].size=${shelterUpdate[item.row].size}"
                    )

                    // TH: updates indices locally, before submit.
                    for(offset in 0 until shelterUpdate[item.row].size) {

                        // TH: marks indices for removal.
                        if(
                            shelterUpdate[item.row][offset].getOffsetNew().toInt()
                            ==item.offset-itemOffsetAdjusted
                        ){
                            offsetRemoved.add(offset)
                            itemOffsetMapBoolean = true
                        }

                        // TH: shifts left, if greater than target index.
                        else if(
                            shelterUpdate[item.row][offset].getOffsetNew().toInt()
                            >item.offset-itemOffsetAdjusted
                        ){

                            // TH: updates local indices of remaining elements (i.e. shifts left).
                            shelterUpdate[item.row][offset].setOffsetNew(
                                shelterUpdate[item.row][offset].getOffsetNew().toInt()-1
                            )

                            // TH: updates local indices of RecyclerView (i.e. recently updated offset).
                            shelterUpdateRecyclerView[item.row][offset].setOffsetNew(
                                shelterUpdate[item.row][offset].getOffsetNew().toInt()
                            )

                        }

                        // TH: remains same, if less than target index.
                        else{
                            continue
                        }

                    }//for offset.

                    // TH: removes targets.
                    for(offset in offsetRemoved.indices) {

                        // TH: removes from update for shelter.
                        shelterUpdate[item.row].removeAt(
                            offsetRemoved[offset]
                        )

                        // TH: removes from update for RecyclerView.
                        shelterUpdateRecyclerView[item.row].removeAt(
                            offsetRemoved[offset]
                        )

                    }//for offset.

                    Log.i(
                        "deleteActionButton  (after)",
                        "shelterUpdate[${item.row}].size=${shelterUpdate[item.row].size}"
                    )

                    // TH: if target row recently emptied.
                    if(shelterUpdate[item.row].size==0){

                        for(offset in item.row until shelterUpdate.size){

                            // TH: gets content of next row, if still within bound.
                            if(offset+1<shelterUpdate.size){

                                // TH: populates shelterUpdate, per next row.
                                val rowNext = ArrayList<ShelterUpdate>()

                                // TH: populates update for local RecyclerView, per next row.
                                val rowNextRecyclerView = ArrayList<RecommendationEntity>()

                                // TH: populates ..., per next row.
                                for(eachOffset in shelterUpdate[offset+1].indices){

                                    // TH: marks for addition in back-end.
                                    // TH: shifts up by one row, takes 'sentinel' as recommendationId.
                                    val update = ShelterUpdate(
                                        shelterUpdate[offset+1][eachOffset].getProductId(),
                                        sentinelValue.toString(),
                                        ((shelterUpdate[offset+1][eachOffset].getRow()).toInt()-1).toString(),
                                        shelterUpdate[offset+1][eachOffset].getOffsetOld(),
                                        shelterUpdate[offset+1][eachOffset].getOffsetNew()
                                    )
                                    rowNext.add(eachOffset,update)

                                    // TH: shifts up by one row.
                                    // TH: takes 'row-1' attribute.
                                    val updateRecyclerView = RecommendationEntity (
                                        shelterUpdateRecyclerView[offset+1][eachOffset].recommendationId,
                                        shelterUpdateRecyclerView[offset+1][eachOffset].author,
                                        shelterUpdateRecyclerView[offset+1][eachOffset].row-1,
                                        shelterUpdateRecyclerView[offset+1][eachOffset].offset,
                                        shelterUpdateRecyclerView[offset+1][eachOffset].shelter,
                                        shelterUpdateRecyclerView[offset+1][eachOffset].productId,
                                        shelterUpdateRecyclerView[offset+1][eachOffset].url
                                    )
                                    rowNextRecyclerView.add(eachOffset,updateRecyclerView)

                                    // TH: marks for deletion in back-end.
                                    // TH: takes 'row' attribute, instead of 'row-1' attribute.
                                    // TH: retrieves deep copy (i.e. beware of reference) !!
                                    val entityDelete = RecommendationEntity (
                                        shelterUpdateRecyclerView[offset+1][eachOffset].recommendationId,
                                        shelterUpdateRecyclerView[offset+1][eachOffset].author,
                                        shelterUpdateRecyclerView[offset+1][eachOffset].row,
                                        shelterUpdateRecyclerView[offset+1][eachOffset].offset,
                                        shelterUpdateRecyclerView[offset+1][eachOffset].shelter,
                                        shelterUpdateRecyclerView[offset+1][eachOffset].productId,
                                        shelterUpdateRecyclerView[offset+1][eachOffset].url
                                    )
                                    deleteActionSet.add(entityDelete)

                                }//for eachOffset.

                                // TH: shifts every element up, by one row.
                                shelterUpdate[offset] = rowNext
                                shelterUpdateRecyclerView[offset] = rowNextRecyclerView

                                for (itemIndexAhead in itemIndex+1 until deleteActionSetList.size){

                                    // TH: helps update by reference (i.e. due to shift-up).
                                    // TH: patches failure due to pattern (i.e. deletes 1's, multiple):
                                    // TH: 1-2-3
                                    // TH: 1
                                    // TH: 1-4-5
                                    var itemAhead = deleteActionSetList[itemIndexAhead]

                                    if(itemAhead.row>item.row){
                                        val itemAheadUpdated=RecommendationEntity(
                                            itemAhead.recommendationId,
                                            itemAhead.author,
                                            itemAhead.row-1,
                                            itemAhead.offset,
                                            itemAhead.shelter,
                                            itemAhead.productId,
                                            itemAhead.url,

                                            )
                                        deleteActionSetList[itemIndexAhead]=itemAheadUpdated
                                    }

                                }//for itemIndexAhead.

                                // TH: sorts delete set, ascending.
                                val deleteActionSetListInternal = deleteActionSet.sortedWith(compareBy({it.row},{it.offset}))

                                // TH: updates items to be deleted, by row.
                                updateDeleteActionSetListRemove(deleteActionSetListInternal)

                                /*
                                displayDeleteActionSetListRemove("deleteActionButton-before-1")
                                */

                                // TH: shifts content of itemOffsetMap up, by one row.
                                val tempOffset = itemOffsetMap[offset+1]
                                if (tempOffset != null) {
                                    itemOffsetMap[offset] = tempOffset
                                }

                            }//if next row still within bound.
                            else{

                                // TH: clears out current content.
                                shelterUpdate[offset] = arrayListOf()
                                shelterUpdateRecyclerView[offset] = arrayListOf()

                                // TH: clears out local ViewModel.
                                saveMemoryShelterViewModel(shelterViewModel,offset)

                                itemOffsetMap[offset] = 0

                            }//else next row already out of bound.

                            // TH: marks cache as dirty.
                            shelterUpdateCache[offset]=1

                            // TH: updates local RecyclerView.
                            recommendationAdapter[offset].updateRecommendations(
                                shelterUpdateRecyclerView[offset]
                            )

                            saveMemoryShelterUpdate()
                            saveMemoryShelterUpdateRecyclerView()

                        }//for offset.

                    }//if target row recently emptied.
                    else{

                        // TH: updates local RecyclerView.
                        recommendationAdapter[item.row].updateRecommendations(
                            shelterUpdateRecyclerView[item.row]
                        )

                        if(offsetRemoved.size>0){
                            if(itemOffsetMapBoolean){
                                // TH: helps track indices of remaining elements after each shift.
                                val tempOffset = itemOffsetMap[item.row]
                                if (tempOffset != null) {
                                    itemOffsetMap[item.row]= tempOffset + 1
                                }
                            }//if itemOffsetMapBoolean.
                        }

                    }//else target row NOT yet emptied.

                }//for item.

                Log.i(
                    "deleteActionButton",
                    "HERE_2."
                )

                /*
                displayShelterUpdate("deleteActionButton-after")
                displayShelterUpdateRecyclerView("deleteActionButton-after")
                */

                // TH: removes previous highlight(s).
                for (rowInternal in 0 until recommendationAdapter.size) {
                    recommendationAdapter[rowInternal].clearRecommendations()
                }//for rowInternal.

                longest = 0
                for(rowInternal in 0 until shelterUpdateRecyclerView.size){
                    // TH: updates longest row, after local insert.
                    if(longest<shelterUpdateRecyclerView[rowInternal].size){
                        longest = shelterUpdateRecyclerView[rowInternal].size
                    }
                }//for rowInternal.

                // TH: assigns LayoutManager to RecyclerView, per row.
                // TH: assumes uniform dimension.
                for (index in 0 until shelterUpdateRecyclerView.size) {
                    // TH: deploys GridLayoutManager.
                    recyclerView[index].layoutManager = GridLayoutManager(
                        this,
                        longest,
                        GridLayoutManager.VERTICAL,
                        false
                    )
                }

                var updateRequired = false

                // TH: compares user-interface (i.e. shelterUpdate) against back-end (i.e. shelter).
                // TH: handles cases where entire row recently added or deleted.
                if(shelterUpdate.size!=shelter.size) { updateRequired = true }//if size.

                for(eachRow in shelterUpdate.indices) {

                    val updateRequiredEachRow = if(eachRow<shelter.size) {

                        if(compareShelterUpdateAgainstShelter(eachRow)==0) {
                            // TH: indicates existing row remains same.
                            continue
                        }
                        else {
                            // TH: indicates existing row already changed content.
                            true
                        }
                    }
                    else {
                        // TH: indicates additional row already inserted.
                        true
                    }

                    // TH: continues iteration if NO change.
                    if(!updateRequiredEachRow) {
                        continue
                    }
                    else{

                        updateRequired = true
                        Log.i(
                            "deleteActionButton",
                            "Row $eachRow Needs Update (Delete)."
                        )

                        // TH: represents target productIds to be added.
                        val deleteActionSetSortedProductId = arrayListOf<Long>()

                        // TH: represents target offsets to be added.
                        val deleteActionSetSorted = arrayListOf<Int>()

                        // TH: represents target recommendationIds of remaining elements.
                        val deleteActionSetSortedBar = arrayListOf<Long>()

                        // TH: represents final indices of remaining elements.
                        val deleteActionSetTotal = arrayListOf<Int>()

                        // TH: collects updates per row.
                        for(offset in shelterUpdate[eachRow].indices){

                            // TH: collects new items.
                            if(
                                shelterUpdate[eachRow][offset].getRecommendationId().toLong()
                                ==sentinelValue.toLong()
                            ){

                                deleteActionSetSortedProductId.add(
                                    shelterUpdate[eachRow][offset].getProductId().toLong()
                                )

                                deleteActionSetSorted.add(
                                    shelterUpdate[eachRow][offset].getOffsetNew().toInt()
                                )

                            }//if new items.

                            else{

                                // TH: skips if same position.
                                if(
                                    shelterUpdate[eachRow][offset].getOffsetOld()
                                    == shelterUpdate[eachRow][offset].getOffsetNew()
                                ){
                                    continue
                                }//if.
                                else{

                                    deleteActionSetSortedBar.add(
                                        shelterUpdate[eachRow][offset].getRecommendationId().toLong()
                                    )

                                    deleteActionSetTotal.add(
                                        shelterUpdate[eachRow][offset].getOffsetNew().toInt()
                                    )

                                }//else new position.

                            }//else remaining items.

                        }//for offset.

                        // TH: specifies delimiter, per row.
                        indexShelterString += "AND-$eachRow-"

                        // TH: specifies delimiter, per row.
                        deleteActionSetSortedStringProductId+="AND-"
                        for(element in deleteActionSetSortedProductId){
                            deleteActionSetSortedStringProductId+= "$element-"
                        }

                        // TH: specifies delimiter, per row.
                        deleteActionSetSortedString+="AND-"
                        for(element in deleteActionSetSorted){
                            deleteActionSetSortedString+= "$element-"
                        }

                        // TH: specifies delimiter, per row.
                        deleteActionSetSortedBarString+="AND-"
                        for(element in deleteActionSetSortedBar){
                            deleteActionSetSortedBarString+= "$element-"
                        }

                        // TH: specifies delimiter, per row.
                        deleteActionSetTotalString+="AND-"
                        for(element in deleteActionSetTotal){
                            deleteActionSetTotalString+= "$element-"
                        }

                    }//else.

                }//for eachRow.

                if(updateRequired){

                    Log.i(
                        "deleteActionButton",
                        "User Interface != Back End."
                    )

                    Log.i("deleteActionButton:indexShelterString                   ", indexShelterString)
                    Log.i("deleteActionButton:deleteActionSetSortedStringProductId ", deleteActionSetSortedStringProductId)
                    Log.i("deleteActionButton:deleteActionSetSortedString          ", deleteActionSetSortedString)
                    Log.i("deleteActionButton:deleteActionSetSortedBarString       ", deleteActionSetSortedBarString)
                    Log.i("deleteActionButton:deleteActionSetTotalString           ", deleteActionSetTotalString)
                    Log.i("deleteActionButton:deleteActionSetProductIdRemove       ", deleteActionSetProductIdRemove)

                }//if updateRequired.
                else{

                    Log.i(
                        "deleteActionButton",
                        "User Interface == Back End."
                    )

                }//else.

            }//else deleteActionSet.size!=0.

            modeDeleteMultiple = 0
            deleteMultipleButton.isEnabled  = true
            deleteMultipleButton.isSelected = false

            // TH: enables other buttons.
            modeDeleteSingle = 0
            deleteSingleButton.isEnabled  = true
            deleteSingleButton.isSelected = false

            // TH: disables action button.
            deleteActionButton.isEnabled  = false
            deleteActionButton.isSelected = false
            deleteActionButton.visibility = View.GONE

            // TH: gets ready for next round of delete multiple.
            deleteActionSet = mutableSetOf()

            /*
            displayDeleteActionSetListRemove("deleteActionButton-after")
            */

            if(designForTest==1){
                if(!deleteActionSetEmpty) {
                    Log.i(
                        "deleteActionButton",
                        "Ended."
                    )
                }
            }//if designForTest.

        }//deleteAction.

        ////////////////////////////////////////////////////////////////////////////////////////////////

        submitActionButton.setOnClickListener{

            // TH: helps de-bounce, maybe.
            submitActionButton.isEnabled = false

            clearAll()

            /*
            // TH: displays debug --- IMPORTANT.
            displayStatistics("before")
            displayShelterUpdate("submitActionButton-before")
            displayShelterUpdateRecyclerView("submitActionButton-before")
            */

            ////////////////////////////////////////////////////////////////////////////////////////////////

            var updateRequired = false

            // TH: compares user-interface (i.e. shelterUpdate) against back-end (i.e. shelter).
            // TH: handles cases where entire row recently added or deleted.
            if(shelterUpdate.size!=shelter.size) { updateRequired = true }//if size.

            // TH: issues burst request, instead of bulk request.
            for(eachRow in 0 until deleteActionSetListRemove.size){

                // TH: skips if empty.
                if(deleteActionSetListRemove[eachRow].size==0) continue

                // TH: handles cases where items per given row need to be deleted.
                updateRequired = true

                // TH: generates URl string parameters (i.e. target recommendationIds to be removed).
                var submitActionSetProductIdRemove = ""
                submitActionSetProductIdRemove +="AND-"

                for(element in deleteActionSetListRemove[eachRow]){
                    submitActionSetProductIdRemove+= "$element-"
                }//for element.

                if(designForTest==1) {
                    Log.i(
                        "submitActionButton (Remove)",
                        "Incremented Before submitRecommendationsRxJava @ eachRow=${eachRow}."
                    )
                    Log.i("submitActionButton::submitActionSetProductIdRemove       ", submitActionSetProductIdRemove)
                    // TH: pauses test flow, waits for background (i.e. network request, see below).
                    EspressoIdlingResource.increment()
                }//if ==1.

                // TH: calls back-end, for submit.
                CoroutineScope(Dispatchers.IO).launch {
                    shelterViewModel.submitRecommendationsRxJava(
                        shelterForm.trim(),
                        eachRow.toString().trim(),
                        "AND-".trim(),
                        "AND-".trim(),
                        "AND-".trim(),
                        "AND-".trim(),
                        submitActionSetProductIdRemove.trim(),
                        designForTest
                    )
                    withContext(Dispatchers.Main){
                        delay(1)
                    }
                }//CoroutineScope.

            }//for eachRow.

            // TH: issues burst request, instead of bulk request.
            for(eachRow in shelterUpdate.indices) {

                val updateRequiredEachRow = if(eachRow<shelter.size) {

                    if(compareShelterUpdateAgainstShelter(eachRow)==0) {
                        // TH: indicates existing row remains same.
                        continue
                    }
                    else {
                        // TH: indicates existing row already changed content.
                        true
                    }
                }
                else {
                    // TH: indicates additional row already inserted.
                    true
                }

                // TH: continues iteration if NO change.
                if(!updateRequiredEachRow) {
                    continue
                }
                else{

                    // TH: generates URl string parameters (i.e. target recommendationIds to be removed).
                    var submitActionSetProductIdRemove = ""
                    submitActionSetProductIdRemove +="AND-"

                    // TH: generates URl string parameters (i.e. target rows).
                    var indexShelterString = ""

                    // TH: generates URl string parameters (i.e. target productIds to be added).
                    var submitActionSetSortedStringProductId = ""

                    // TH: generates URl string parameters (i.e. target offsets to be added).
                    var submitActionSetSortedString = ""

                    // TH: generates URl string parameters.
                    // TH: represents target recommendationIds of remaining elements.
                    var submitActionSetSortedBarString = ""

                    // TH: generates URl string parameters.
                    // TH: represents final indices of remaining elements.
                    var submitActionSetTotalString = ""

                    updateRequired = true

                    // TH: represents target productIds to be added.
                    val submitActionSetSortedProductId = arrayListOf<Long>()

                    // TH: represents target offsets to be added.
                    val submitActionSetSorted = arrayListOf<Int>()

                    // TH: represents target recommendationIds of remaining elements.
                    val submitActionSetSortedBar = arrayListOf<Long>()

                    // TH: represents final indices of remaining elements.
                    val submitActionSetTotal = arrayListOf<Int>()

                    // TH: collects updates per row.
                    for(offset in shelterUpdate[eachRow].indices){

                        // TH: collects new items.
                        if(
                            shelterUpdate[eachRow][offset].getRecommendationId().toLong()
                            ==sentinelValue.toLong()
                        ){

                            submitActionSetSortedProductId.add(
                                shelterUpdate[eachRow][offset].getProductId().toLong()
                            )

                            submitActionSetSorted.add(
                                shelterUpdate[eachRow][offset].getOffsetNew().toInt()
                            )

                        }//if new items.

                        else{

                            // TH: skips if same position.
                            if(
                                shelterUpdate[eachRow][offset].getOffsetOld()
                                == shelterUpdate[eachRow][offset].getOffsetNew()
                            ){
                                continue
                            }//if.
                            else{

                                submitActionSetSortedBar.add(
                                    shelterUpdate[eachRow][offset].getRecommendationId().toLong()
                                )

                                submitActionSetTotal.add(
                                    shelterUpdate[eachRow][offset].getOffsetNew().toInt()
                                )

                            }//else new position.

                        }//else remaining items.

                    }//for offset.

                    // TH: specifies delimiter, per row.
                    indexShelterString += "AND-$eachRow-"

                    // TH: specifies delimiter, per row.
                    submitActionSetSortedStringProductId+="AND-"
                    for(element in submitActionSetSortedProductId){
                        submitActionSetSortedStringProductId+= "$element-"
                    }

                    // TH: specifies delimiter, per row.
                    submitActionSetSortedString+="AND-"
                    for(element in submitActionSetSorted){
                        submitActionSetSortedString+= "$element-"
                    }

                    // TH: specifies delimiter, per row.
                    submitActionSetSortedBarString+="AND-"
                    for(element in submitActionSetSortedBar){
                        submitActionSetSortedBarString+= "$element-"
                    }

                    // TH: specifies delimiter, per row.
                    submitActionSetTotalString+="AND-"
                    for(element in submitActionSetTotal){
                        submitActionSetTotalString+= "$element-"
                    }

                    // TH: indexShelterString:                   represents target rows.
                    // TH: submitActionSetSortedStringProductId: represents target productIds to be added.
                    // TH: submitActionSetSortedString:          represents target offsets to be added.
                    // TH: submitActionSetSortedBarString:       represents target recommendationIds of remaining elements.
                    // TH: submitActionSetTotalString:           represents final indices of remaining elements.
                    // TH: submitActionSetProductIdRemove:       represents target recommendationIds to be removed.

                    Log.i("submitActionButton::indexShelterString                   ", indexShelterString)
                    Log.i("submitActionButton::submitActionSetSortedStringProductId ", submitActionSetSortedStringProductId)
                    Log.i("submitActionButton::submitActionSetSortedString          ", submitActionSetSortedString)
                    Log.i("submitActionButton::submitActionSetSortedBarString       ", submitActionSetSortedBarString)
                    Log.i("submitActionButton::submitActionSetTotalString           ", submitActionSetTotalString)
                    Log.i("submitActionButton::submitActionSetProductIdRemove       ", submitActionSetProductIdRemove)

                    if(designForTest==1) {
                        Log.i(
                            "submitActionButton (Add)",
                            "Incremented Before submitRecommendationsRxJava @ eachRow=${eachRow}."
                        )
                        // TH: pauses test flow, waits for background (i.e. network request, see below).
                        EspressoIdlingResource.increment()
                    }//if ==1.

                    // TH: calls back-end, for submit.
                    CoroutineScope(Dispatchers.IO).launch {
                        shelterViewModel.submitRecommendationsRxJava(
                            shelterForm.trim(),
                            indexShelterString.trim(),
                            submitActionSetSortedStringProductId.trim(),
                            submitActionSetSortedString.trim(),
                            submitActionSetSortedBarString.trim(),
                            submitActionSetTotalString.trim(),
                            submitActionSetProductIdRemove.trim(),
                            designForTest
                        )
                        withContext(Dispatchers.Main){
                            delay(1)
                        }
                    }//CoroutineScope.

                }//else.

            }//for eachRow.

            if(updateRequired){

                Log.i(
                    "submitActionButton",
                    "User Interface != Back End."
                )

                // TH: waits for and checks one row at a time.
                for (indexShelter in 0 until shelterUpdate.size) {

                    var firstTime=true

                    // TH: waits until fully updated (i.e. checks certain attributes, element-by-element).
                    while(
                        !checkShelterViewModel(
                            shelterViewModel.getShelter(indexShelter),
                            shelterUpdate[indexShelter],
                            indexShelter
                        )
                    ){

                        // TH: retrieves recommendations per (shelter,row) with reactive programming.
                        if(designForTest==1) {

                            if(firstTime){ firstTime=false }//if firstTime.
                            else{

                                val startTime = System.currentTimeMillis()
                                var elapsed   = System.currentTimeMillis() - startTime

                                while(elapsed<waitTime){
                                    elapsed = System.currentTimeMillis() - startTime
                                }

                            }//else.

                            Log.i(
                                "submitActionButton",
                                "Incremented Before getRecommendationsRxJava."
                            )
                            // TH: pauses test flow, waits for background (i.e. network request, see below).
                            EspressoIdlingResource.increment()

                        }//if designForTest==1.

                        shelterViewModel.getRecommendationsRxJava(
                            shelterForm,
                            indexShelter.toString(),
                            designForTest
                        )

                        shelterViewModel.recommendationEntityListRxJavaProperty.observe(this){

                        }

                    }//while.

                }//for.

                for (indexShelter in 0 until shelterUpdate.size) {
                    recommendationAdapter[indexShelter].updateRecommendations(
                        shelterViewModel.getShelter(
                            indexShelter
                        )
                    )
                }//for.

                if(shelterUpdate.size<shelter.size){

                    for (indexShelter in 0 until shelterUpdate.size){

                        // TH: caches shelter[indexShelter], in Activity.
                        shelter[indexShelter] = shelterViewModel.getShelter(indexShelter)

                        // TH: retrieves deep copy (i.e. beware of reference) !!
                        retrieveDeepCopyShelterUpdateRecyclerView(shelterViewModel,indexShelter)

                    }//for.

                    for (indexShelter in shelterUpdate.size until shelter.size){
                        // TH: caches shelter[indexShelter], in Activity.
                        shelter[indexShelter] = arrayListOf()
                    }//for.
                    saveMemoryShelter()

                }//if <.

                else if(shelterUpdate.size==shelter.size){

                    for (indexShelter in 0 until shelterUpdate.size){

                        // TH: caches shelter[indexShelter], in Activity.
                        shelter[indexShelter] = shelterViewModel.getShelter(indexShelter)

                        // TH: retrieves deep copy (i.e. beware of reference) !!
                        retrieveDeepCopyShelterUpdateRecyclerView(shelterViewModel,indexShelter)

                    }//for.

                }//else if ==.

                else{

                    for (indexShelter in 0 until shelter.size){

                        // TH: caches shelter[indexShelter], in Activity.
                        shelter[indexShelter] = shelterViewModel.getShelter(indexShelter)

                        // TH: retrieves deep copy (i.e. beware of reference) !!
                        retrieveDeepCopyShelterUpdateRecyclerView(shelterViewModel,indexShelter)

                    }//for.

                    for (indexShelter in shelter.size until shelterUpdate.size){

                        // TH: if next row does NOT exist yet -> creates new row.
                        shelter.add(
                            shelterViewModel.getShelter(indexShelter)
                        )

                        // TH: retrieves deep copy (i.e. beware of reference) !!
                        retrieveDeepCopyShelterUpdateRecyclerView(shelterViewModel,indexShelter)

                    }//for.

                }//else >.

                longest = 0
                for (indexShelter in 0 until shelter.size)
                {
                    // TH: updates longest row, after insert.
                    if(longest<shelter[indexShelter].size) {
                        longest = shelter[indexShelter].size
                    }
                }//for.

                // TH: assigns LayoutManager to RecyclerView, per row.
                // TH: assumes uniform dimension.
                for (indexLayout in 0 until shelter.size) {
                    // TH: deploys GridLayoutManager.
                    recyclerView[indexLayout].layoutManager = GridLayoutManager(
                        this@ShelterActivity,
                        longest,
                        GridLayoutManager.VERTICAL,
                        false
                    )
                }

            }//if updateRequired.
            else{

                Log.i(
                    "submitActionButton",
                    "User Interface == Back End."
                )

            }//else.

            ////////////////////////////////////////////////////////////////////////////////////////////////

            // TH: sets all caches as clean, after submit.
            for (index in 0 until rows ) {
                shelterUpdateCache[index]=0
            }

            // TH: initializes ...
            deleteActionSetListRemove = arrayListOf()

            for (row in 0 until rows) {
                deleteActionSetListRemove.add(row,arrayListOf())
            }//for row.

            productIdSetupMode.isEnabled = true
            productIdHighLightMode.isEnabled = true
            productIdAddMode.isEnabled = true
            productIdDeleteMode.isEnabled = true

            // TH: enables 'Submit' only when necessary.
            submitActionButton.isEnabled = compareShelterUpdateAgainstShelter()

            // TH: clears highlights per row.
            for (rowInternal in 0 until recommendationAdapter.size) {
                recommendationAdapter[rowInternal].clearRecommendations()
            }//for rowInternal.

            // TH: clears out all items for delete.
            deleteActionSet = mutableSetOf()

            // TH: clears out all items for anchor.
            anchorActionSet = mutableSetOf()

            // TH: displays debug, VERY SLOW !!
            val toast = Toast.makeText(
                this@ShelterActivity,
                "Submission Succeeded.",
                Toast.LENGTH_LONG
            )
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()

            /*
            // TH: displays debug --- IMPORTANT.
            displayStatistics("after")
            displayShelterUpdate("submitActionButton-after")
            displayShelterUpdateRecyclerView("submitActionButton-after")
            */

        }//submit.

        ////////////////////////////////////////////////////////////////////////////////////////////////

    }//onCreate.

    private fun hideModeButDelete() {
        productIdSetupMode.visibility      = View.GONE
        productIdHighLightMode.visibility  = View.GONE
        productIdAddMode.visibility        = View.GONE
        submitActionButton.visibility      = View.GONE
    }//hideModeButDelete.

    private fun hideModeButAdd() {
        productIdSetupMode.visibility      = View.GONE
        productIdHighLightMode.visibility  = View.GONE
        productIdDeleteMode.visibility     = View.GONE
        submitActionButton.visibility      = View.GONE
    }//hideModeButAdd.

    private fun hideModeButHighlight() {
        productIdSetupMode.visibility      = View.GONE
        productIdAddMode.visibility        = View.GONE
        productIdDeleteMode.visibility     = View.GONE
        submitActionButton.visibility      = View.GONE
    }//hideModeButHighlight.

    private fun hideModeButSetup() {
        productIdHighLightMode.visibility  = View.GONE
        productIdAddMode.visibility        = View.GONE
        productIdDeleteMode.visibility     = View.GONE
        submitActionButton.visibility      = View.GONE
    }//hideModeButSetup.

    private fun displayModeAll(){
        productIdSetupMode.visibility      = View.VISIBLE
        productIdHighLightMode.visibility  = View.VISIBLE
        productIdAddMode.visibility        = View.VISIBLE
        productIdDeleteMode.visibility     = View.VISIBLE
        submitActionButton.visibility      = View.VISIBLE
    }//displayModeAll.

    private fun compareShelterUpdateAgainstShelter(): Boolean {

        // TH: compares user-interface (i.e. shelterUpdate) against back-end (i.e. shelter).
        // TH: handles cases where entire row recently added or deleted.
        if(shelterUpdate.size!=shelter.size) { return true }//if size.

        for(eachRow in 0 until deleteActionSetListRemove.size){

            // TH: skips if empty.
            if(deleteActionSetListRemove[eachRow].size==0) continue

            // TH: handles cases where items per given row need to be deleted.
            return true

        }//for eachRow.

        // TH: handles cases where content per any given row recently changed.
        for(eachRow in shelterUpdate.indices) {

            return if(eachRow<shelter.size) {

                if(compareShelterUpdateAgainstShelter(eachRow)==0) {
                    // TH: indicates existing row remains same.
                    continue
                } else {
                    // TH: indicates existing row already changed content.
                    true
                }
            } else {
                // TH: indicates additional row already inserted.
                true
            }

        }//for eachRow.

        return false

    }//checkShelterUpdateAgainstShelter.

    private fun retrieveDeepCopyShelterUpdateRecyclerView(
        shelterViewModel: ShelterViewModel,
        indexShelter: Int
    ) {

        val shelterViewModelSorted = shelterViewModel.getShelter(indexShelter).sortedWith(compareBy({it.row},{it.offset}))

        // TH: retrieves deep copy (i.e. beware of reference) !!
        val indexShelterRecyclerView = ArrayList<RecommendationEntity>()

        for(eachOffset in shelterViewModelSorted.indices){
            val entity = RecommendationEntity (
                shelterViewModelSorted[eachOffset].recommendationId,
                shelterViewModelSorted[eachOffset].author,
                shelterViewModelSorted[eachOffset].row,
                shelterViewModelSorted[eachOffset].offset,
                shelterViewModelSorted[eachOffset].shelter,
                shelterViewModelSorted[eachOffset].productId,
                shelterViewModelSorted[eachOffset].url
            )
            indexShelterRecyclerView.add(eachOffset,entity)
        }//for eachOffset.

        shelterUpdateRecyclerView[indexShelter] = indexShelterRecyclerView

    }//retrieveDeepCopyShelterUpdateRecyclerView.

    private fun displayDeleteActionSet(atWhen: String) {

        val deleteActionSetList = deleteActionSet.sortedWith(compareBy({it.row},{it.offset}))

        for(index in deleteActionSetList.indices){

            Log.i(
                "displayDeleteActionSet-$atWhen",
                "sortedDeleteActionSet[$index].row=${deleteActionSetList[index].row}"
            )
            Log.i(
                "displayDeleteActionSet-$atWhen",
                "sortedDeleteActionSet[$index].productId=${deleteActionSetList[index].productId}"
            )
            Log.i(
                "displayDeleteActionSet-$atWhen",
                "sortedDeleteActionSet[$index].offset=${deleteActionSetList[index].offset}"
            )
            Log.i(
                "displayDeleteActionSet-$atWhen",
                "sortedDeleteActionSet[$index].recommendationId=${deleteActionSetList[index].recommendationId}"
            )
            Log.i(
                "displayDeleteActionSet-$atWhen",
                "--------------------------------------------------------"
            )
        }//for index.

    }//displayDeleteActionSet.

    private fun saveMemoryShelterViewModel(shelterViewModel: ShelterViewModel, offset: Int) {
        shelterViewModel.setShelter(offset,arrayListOf())
    }//saveMemoryShelterViewModel.

    private fun saveMemoryShelter() {
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
    }//saveMemoryShelter.

    private fun saveMemoryShelterUpdateRecyclerView() {
        for (index in 0 until shelterUpdateRecyclerView.size) {
            // TH: removes empty list(s) from shelterUpdateRecyclerView.
            shelterUpdateRecyclerView
                .filter {
                    it.size == 0
                }
                .map {
                    shelterUpdateRecyclerView.remove(it)
                }
        }
    }//saveMemoryShelterUpdateRecyclerView.

    private fun saveMemoryShelterUpdate() {
        for (index in 0 until shelterUpdate.size) {
            // TH: removes empty list(s) from shelterUpdate.
            shelterUpdate
                .filter {
                    it.size == 0
                }
                .map {
                    shelterUpdate.remove(it)
                }
        }
    }//saveMemoryShelterUpdate.

    private fun updateDeleteActionSetListRemove(deleteActionSetList: List<RecommendationEntity>) {

        for (index in 0 until deleteActionSetListRemove.size) {
            updateDeleteActionSetListRemoveInternal(
                deleteActionSetList,
                index
            )
        }//for index.

        for (row in 0 until deleteActionSetListRemove.size) {
            deleteActionSetListRemove[row]=
                deleteActionSetListRemove[row].toHashSet().toMutableList()
                        as java.util.ArrayList<Long>
        }//for row.

    }//updateDeleteActionSetListRemove.

    private fun updateDeleteActionSetListRemoveInternal(
        deleteActionSetList: List<RecommendationEntity>,
        index: Int
    ) {

        // TH: initializes each row.
        // TH: https://stackoverflow.com/questions/33278869/how-do-i-initialize-kotlins-mutablelist-to-empty-mutablelist
        val row = ArrayList<Long>()

        // TH: retrieves existing contents.
        val previousList = deleteActionSetListRemove[index]

        deleteActionSetList
            .filter{
                it.row == index && it.recommendationId != sentinelValue.toLong()
            }
            .map{
                row.add(it.recommendationId)
            }
            .apply{
                // TH: appends new contents to existing contents (i.e. between sessions).
                previousList.addAll(row)
                deleteActionSetListRemove[index]=previousList
            }

    }//updateDeleteActionSetListRemoveInternal.

    private fun displayShelterUpdateRecyclerView(atWhen: String) {

        for (row in 0 until shelterUpdateRecyclerView.size) {
            Log.i(
                "displayShelterUpdateRecyclerView-$atWhen",
                "row=${row}"
            )
            for(offset in 0 until shelterUpdateRecyclerView[row].size) {
                Log.i(
                    "displayShelterUpdateRecyclerView-$atWhen",
                    "productId: " + shelterUpdateRecyclerView[row][offset].productId.toString()
                )
                Log.i(
                    "displayShelterUpdateRecyclerView-$atWhen",
                    "offset:    " + shelterUpdateRecyclerView[row][offset].offset.toString()
                )
                Log.i(
                    "displayShelterUpdateRecyclerView-$atWhen",
                    "recommendationId:       " + shelterUpdateRecyclerView[row][offset].recommendationId
                )
            }//for offset.
            Log.i("displayShelterUpdateRecyclerView-$atWhen", "--------------------------------------------------------")
        }//for row.

    }//displayShelterUpdateRecyclerView.

    private fun displayShelter() {

        for (row in 0 until shelter.size) {
            Log.i("displayShelter:shelter:row", row.toString())
            Log.i("displayShelter:shelter:shelter[$row].size", shelter[row].size.toString())
            for(offset in 0 until shelter[row].size) {
                Log.i("displayShelter:RecommendationEntity", "productId: " + shelter[row][offset].productId.toString())
                Log.i("displayShelter:RecommendationEntity", "shelter:   " + shelter[row][offset].shelter)
                Log.i("displayShelter:RecommendationEntity", "row:       " + shelter[row][offset].row.toString())
                Log.i("displayShelter:RecommendationEntity", "offset:    " + shelter[row][offset].offset.toString())
                Log.i("displayShelter:RecommendationEntity", "url:       " + shelter[row][offset].url)
            }//for offset.
            Log.i("displayShelter:", "--------------------------------------------------------")
        }//for row.

    }//displayShelter.

    private fun displayStatistics(atWhen: String) {

        Log.i(
            "displayStatistics-$atWhen",
            "shelter.size=${shelter.size}."
        )
        Log.i(
            "displayStatistics-$atWhen",
            "shelterUpdate.size=${shelterUpdate.size}."
        )
        Log.i(
            "displayStatistics-$atWhen",
            "shelterUpdateRecyclerView.size=${shelterUpdateRecyclerView.size}."
        )
        Log.i(
            "displayStatistics-$atWhen",
            "shelterUpdateCache.size=${shelterUpdateCache.size}."
        )
        Log.i(
            "displayStatistics-$atWhen",
            "deleteActionSet.size=${deleteActionSet.size}."
        )
        Log.i(
            "displayStatistics-$atWhen",
            "deleteActionSetListRemove.size=${deleteActionSetListRemove.size}."
        )
        Log.i(
            "displayStatistics-$atWhen",
            "anchorActionSet.size=${anchorActionSet.size}."
        )

        displayShelter()
        displayShelterUpdate("statistics-$atWhen")
        displayShelterUpdateRecyclerView("statistics-$atWhen")
        displayShelterUpdateCache()

    }//displayStatistics.

    private fun displayShelterUpdateCache() {

        for(index in 0 until shelterUpdateCache.size){
            Log.i(
                "displayShelterUpdateCache",
                "shelterUpdateCache[$index]=${shelterUpdateCache[index]}."
            )
        }//for index.

    }//displayShelterUpdateCache.

    private fun checkShelterViewModel(
        shelterViewModel: ArrayList<RecommendationEntity>,
        shelterUpdate: ArrayList<ShelterUpdate>,
        indexShelter: Int
    ): Boolean {

        Log.i(
            "checkShelterViewModel",
            "indexShelter=${indexShelter}."
        )
        Log.i(
            "checkShelterViewModel",
            "shelterViewModel.size=${shelterViewModel.size}."
        )
        Log.i(
            "checkShelterViewModel",
            "shelterUpdate.size=${shelterUpdate.size}."
        )

        // TH: checks for size.
        if(shelterViewModel.size!=shelterUpdate.size) return false

        // TH: checks for certain attributes.
        for(index in 0 until shelterViewModel.size){

            if(
                shelterViewModel[index].productId.toString()!=
                shelterUpdate[index].getProductId()
            ) {
                /*
                Log.i(
                    "checkShelterViewModel",
                    "shelterViewModel[$index].productId=${shelterViewModel[index].productId}"
                )
                Log.i(
                    "checkShelterViewModel",
                    "shelterUpdate[$index].productId=${shelterUpdate[index].getProductId()}"
                )
                */
                return false
            }

            if(
                shelterViewModel[index].offset.toString()!=
                shelterUpdate[index].getOffsetNew()
            ){
                /*
                Log.i(
                    "checkShelterViewModel",
                    "shelterViewModel[$index].offset=${shelterViewModel[index].offset}"
                )
                Log.i(
                    "checkShelterViewModel",
                    "shelterUpdate[$index].offset=${shelterUpdate[index].getOffsetNew()}"
                )
                */
                return false
            }

            if(
                shelterViewModel[index].row.toString()!=
                shelterUpdate[index].getRow()
            ){
                /*
                Log.i(
                    "checkShelterViewModel",
                    "shelterViewModel[$index].row=${shelterViewModel[index].row}"
                )
                Log.i(
                    "checkShelterViewModel",
                    "shelterUpdate[$index].row=${shelterUpdate[index].getRow()}"
                )
                */
                return false
            }

            for(row in 0 until deleteActionSetListRemove.size){
                if(deleteActionSetListRemove[row].contains(shelterViewModel[index].recommendationId)){
                    Log.i(
                        "checkShelterViewModel",
                        "pending update deleteActionSetListRemove @ row=$row."
                    )
                    /*
                    for(column in 0 until deleteActionSetListRemove[row].size){
                        Log.i(
                            "checkShelterViewModel",
                            "deleteActionSetListRemove[$row][$column]=${deleteActionSetListRemove[row][column]}"
                        )
                    }//for column.
                    Log.i(
                        "checkShelterViewModel",
                        "--------------------------------------------------------"
                    )
                    */
                    return false
                }//if contains.
            }//for row.

            // TH: takes on new recommendationId, after back-end.
            shelterUpdate[index].setRecommendationIdNew(
                shelterViewModel[index].recommendationId
            )

            // TH: updates stale value, after back-end.
            shelterUpdate[index].setOffsetOld(
                shelterUpdate[index].getOffsetNew()
            )

        }//for index.

        return true

    }//checkShelterViewModel.

    private fun displayShelterUpdate(atWhen: String) {

        for (row in 0 until shelterUpdate.size) {
            for(offset in 0 until shelterUpdate[row].size) {
                Log.i("displayShelterUpdate-$atWhen", "row:              " + shelterUpdate[row][offset].getRow())
                Log.i("displayShelterUpdate-$atWhen", "productId:        " + shelterUpdate[row][offset].getProductId())
                Log.i("displayShelterUpdate-$atWhen", "recommendationId: " + shelterUpdate[row][offset].getRecommendationId())
                Log.i("displayShelterUpdate-$atWhen", "offset_old:       " + shelterUpdate[row][offset].getOffsetOld())
                Log.i("displayShelterUpdate-$atWhen", "offset_new:       " + shelterUpdate[row][offset].getOffsetNew())
            }//for offset.
            Log.i("displayShelterUpdate-$atWhen:", "--------------------------------------------------------")
        }//for row.

    }//displayShelterUpdate.

    private fun shiftLeftOrRightUpdateAnchor(terminal: Int, rows: Int) {

        // TH: IMPORTANT !!
        // TH: shiftLeft  == addRight at anchored row -> terminal == 0.
        // TH: shiftRight == addLeft  at anchored row -> terminal == 1.

        // TH: allows ONLY one anchor point per insertion.
        anchorActionSet = mutableSetOf()

        // TH: retrieves item for anchor.
        val entity = recommendationAdapter[anchoredRow].getRecommendations(anchoredIndex+terminal)

        // TH: moves anchor point to left (i.e. head end) or to right (i.e. tail end).
        for (index in 0 until recommendationAdapter.size) {
            recommendationAdapter[index].selectRecommendationsForAnchor(
                entity.productId.toString()
            )
        }//for index.

        // TH: toggles selection, anchor.
        val selection = recommendationAdapter[anchoredRow].toggleRecommendationsForAnchor(anchoredIndex+terminal)

        // TH: indicates anchor selected.
        if(selection==1){

            // TH: collects [unique] anchors.
            anchorActionSet.add(entity)

            // TH: retrieves anchor row.
            anchoredRow   = entity.row

            // TH: captures locally updated offset, before submit.
            anchoredIndex = entity.offset

        }
        else{
            // TH: indicates NO anchor selected.
            anchorActionSet.remove(entity)
            anchoredRow   = sentinelValue
            anchoredIndex = sentinelValue
        }

        for(rowInternal in 0 until shelterUpdateRecyclerView.size){
            // TH: updates longest row, after local insert.
            if(longest<shelterUpdateRecyclerView[rowInternal].size){
                longest = shelterUpdateRecyclerView[rowInternal].size
            }
        }//for rowInternal.

        // TH: assigns LayoutManager to RecyclerView, per row.
        // TH: assumes uniform dimension.
        for (index in 0 until rows) {
            // TH: deploys GridLayoutManager.
            recyclerView[index].layoutManager = GridLayoutManager(
                this,
                longest,
                GridLayoutManager.VERTICAL,
                false
            )
        }

    }//shiftLeftOrRightUpdateAnchor.

    private fun addLeftOrRight(
        rows: Int,
        shelterForm: String?,
        productIdForm: String,
        terminal: Int
    ) {

        if(productIdForm.isEmpty()){
            val toast = Toast.makeText(
                this,
                getString(R.string.productId_add_error_empty),
                Toast.LENGTH_LONG
            )
            toast.setGravity(Gravity.CENTER,0,0)
            toast.show()
        }
        else{

            if(anchoredIndex==sentinelValue){
                val toast = Toast.makeText(
                    this,
                    getString(R.string.productId_anchor_error_select),
                    Toast.LENGTH_LONG
                )
                toast.setGravity(Gravity.CENTER,0,0)
                toast.show()
            }
            else{

                if (shelterForm == null) {
                    val toast = Toast.makeText(
                        this,
                        getString(R.string.shelter_error_empty),
                        Toast.LENGTH_LONG
                    )
                    toast.setGravity(Gravity.CENTER,0,0)
                    toast.show()
                }
                // TH: implements insert-left (i.e. 0) or insert-right (i.e. 1) to anchor.
                else {

                    // TH: marks cache as dirty.
                    shelterUpdateCache[anchoredRow] = 1

                    // TH: updates indices locally, before submit.
                    for(offset in 0 until shelterUpdate[anchoredRow].size) {

                        // TH: updates local indices of remaining elements (i.e. NO change).
                        if(
                            shelterUpdate[anchoredRow][offset].getOffsetNew().toInt()<anchoredIndex+terminal
                        ) {
                            continue
                        }

                        // TH: updates local indices of remaining elements (i.e. shifts right).
                        shelterUpdate[anchoredRow][offset].setOffsetNew(
                            shelterUpdate[anchoredRow][offset].getOffsetNew().toInt()+1
                        )

                        // TH: updates local indices of RecyclerView.
                        shelterUpdateRecyclerView[anchoredRow][offset].setOffsetNew(
                            shelterUpdate[anchoredRow][offset].getOffsetNew().toInt()
                        )

                    }//for offset.

                    // TH: creates new item for shelter update, at given offset.
                    val updateNew = ShelterUpdate(
                        productIdForm,
                        sentinelValue.toString(),
                        anchoredRow.toString(),
                        (anchoredIndex+terminal).toString(), // old.
                        (anchoredIndex+terminal).toString(), // new.
                    )

                    // TH: appends to update for shelter.
                    shelterUpdate[anchoredRow].add(anchoredIndex+terminal,updateNew)

                    // TH: creates new item for local RecyclerView update, at given offset.
                    val updateNewView = RecommendationEntity(
                        sentinelValue.toLong(),
                        "Dummy-Author",
                        anchoredRow,
                        anchoredIndex+terminal,
                        shelterForm,
                        productIdForm.toLong(),
                        "Dummy-Url"
                    )

                    // TH: appends to update for RecyclerView.
                    shelterUpdateRecyclerView[anchoredRow].add(anchoredIndex+terminal,updateNewView)

                    // TH: updates local RecyclerView.
                    recommendationAdapter[anchoredRow].updateRecommendations(
                        shelterUpdateRecyclerView[anchoredRow]
                    )

                    // TH: saves anchor as row-elements pairs.
                    anchoredRowIndex = arrayListOf()

                    // TH: moves anchor point to left (i.e. head end) or to right (i.e. tail end).
                    for (index in 0 until recommendationAdapter.size) {

                        val anchoredIndexIterator: ArrayList<Int> =
                            recommendationAdapter[index].selectRecommendationsForAnchor(productIdForm)

                        if(anchoredIndexIterator.size>0) {
                            anchoredRowIndex.add(index,anchoredIndexIterator)
                        }
                        else{
                            anchoredRowIndex.add(index,arrayListOf())
                        }

                    }//for index.

                    for (index in 0 until anchoredRowIndex.size) {
                        if(anchoredRowIndex[index].size==0) continue
                        else{
                            // TH: latches in most recent valid anchor.
                            anchoredProductId = productIdForm
                            Log.i(
                                "addLeftOrRight (index=${index})",
                                "$anchoredProductId at $index:${anchoredRowIndex[index]}"
                            )
                        }
                    }//for index.

                    Log.i(
                        "addLeftOrRight (before)",
                        "anchoredRow=${anchoredRow}, anchoredIndex=${anchoredIndex}"
                    )

                    if(recommendationAdapter[anchoredRow].checkMatchedAnchor(anchoredIndex+terminal)){

                        // TH: clears matchedAnchorSelected all rows, notifies data change per row.
                        // TH: saves and restores previous state, for toggle later.
                        for (indexShelter in 0 until recommendationAdapter.size) {
                            recommendationAdapter[indexShelter].clearMatchedAnchorSelected(anchoredIndex+terminal)
                        }

                        // TH: removes previous anchor.
                        for (rowInternal in 0 until recommendationAdapter.size) {
                            for(offsetInternal in 0 until recommendationAdapter[rowInternal].itemCount){
                                recommendationAdapter[rowInternal].clearRecommendationsForAnchor(offsetInternal)
                            }//for offsetInternal.
                        }//for rowInternal.

                        // TH: allows ONLY one anchor point per insertion.
                        anchorActionSet = mutableSetOf()

                        // TH: retrieves item for anchor.
                        val entity = recommendationAdapter[anchoredRow].getRecommendations(anchoredIndex+terminal)

                        // TH: toggles selection, anchor.
                        val selection = recommendationAdapter[anchoredRow].toggleRecommendationsForAnchor(anchoredIndex+terminal)

                        // TH: indicates anchor selected.
                        if(selection==1){

                            // TH: collects [unique] anchors.
                            anchorActionSet.add(entity)

                            // TH: retrieves anchor row.
                            anchoredRow   = entity.row

                            // TH: captures locally updated offset, before submit.
                            anchoredIndex = entity.offset

                        }
                        else{
                            // TH: indicates NO anchor selected.
                            anchorActionSet.remove(entity)
                            anchoredRow   = sentinelValue
                            anchoredIndex = sentinelValue
                        }

                        if(anchoredRow != sentinelValue){
                            /*
                            // TH: displays debug, VERY SLOW !!
                            val toast = Toast.makeText(
                                this@ShelterActivity,
                                if(selection==1) "Row $anchoredRow Offset $anchoredIndex selected for anchor."
                                else             "Row $anchoredRow Offset $anchoredIndex de-selected for anchor.",
                                Toast.LENGTH_LONG
                            )
                            toast.setGravity(Gravity.CENTER, 0, 0)
                            toast.show()
                            */
                        }
                        else{

                            // TH: displays debug, VERY SLOW !!
                            val toast = Toast.makeText(
                                this@ShelterActivity,
                                "NO anchor selected.",
                                Toast.LENGTH_LONG
                            )
                            toast.setGravity(Gravity.CENTER, 0, 0)
                            toast.show()

                        }

                    }//if already anchored (i.e. productId matched), but NOT selected.

                    Log.i(
                        "addLeftOrRight (after)",
                        "anchoredRow=${anchoredRow}, anchoredIndex=${anchoredIndex}"
                    )

                    // TH: updates longest row, after local insert.
                    if(longest<shelterUpdateRecyclerView[anchoredRow].size){
                        longest = shelterUpdateRecyclerView[anchoredRow].size
                    }

                    // TH: assigns LayoutManager to RecyclerView, per row.
                    // TH: assumes uniform dimension.
                    for (index in 0 until rows) {
                        // TH: deploys GridLayoutManager.
                        recyclerView[index].layoutManager = GridLayoutManager(
                            this,
                            longest,
                            GridLayoutManager.VERTICAL,
                            false
                        )
                    }

                    // TH: updates anchor index, for successive insertion (i.e. to head end or to tail end).
                    anchoredIndex += 0

                }//if shelterForm != null.

            }//if anchoredIndex!=sentinelValue.

        }//if productIdForm != empty.

    }//addLeftOrRight.

    private fun addAboveOrBelow(shelterForm: String, terminal: Int) {

        /*
        displayDeleteActionSet("addAboveOrBelow-before")
        displayDeleteActionSetListRemove("addAboveOrBelow-before")
        */

        for(index in shelterUpdate.size-1 downTo (anchoredRow+terminal) )
        {

            // TH: populates shelterUpdate, per previous row.
            val rowPrevious = ArrayList<ShelterUpdate>()

            // TH: populates update for local RecyclerView, per previous row.
            val rowPreviousRecyclerView = ArrayList<RecommendationEntity>()

            for(eachIndex in shelterUpdate[index].indices){

                // TH: removes all items at given row (i.e. to be shifted down).
                val deleteEntity = RecommendationEntity(
                    shelterUpdateRecyclerView[index][eachIndex].recommendationId,
                    shelterUpdateRecyclerView[index][eachIndex].author,
                    shelterUpdateRecyclerView[index][eachIndex].row,
                    shelterUpdateRecyclerView[index][eachIndex].offset,
                    shelterUpdateRecyclerView[index][eachIndex].shelter,
                    shelterUpdateRecyclerView[index][eachIndex].productId,
                    shelterUpdateRecyclerView[index][eachIndex].url
                )
                deleteActionSet.add(deleteEntity)

                // TH: shifts down by one row.
                val update = ShelterUpdate(
                    shelterUpdate[index][eachIndex].getProductId(),
                    sentinelValue.toString(),
                    (shelterUpdate[index][eachIndex].getRow().toInt()+1).toString(),
                    shelterUpdate[index][eachIndex].getOffsetOld(),
                    shelterUpdate[index][eachIndex].getOffsetNew()
                )
                rowPrevious.add(eachIndex,update)

                // TH: shifts down by one row.
                val updateRecyclerView = RecommendationEntity (
                    shelterUpdateRecyclerView[index][eachIndex].recommendationId,
                    shelterUpdateRecyclerView[index][eachIndex].author,
                    shelterUpdateRecyclerView[index][eachIndex].row+1,
                    shelterUpdateRecyclerView[index][eachIndex].offset,
                    shelterUpdateRecyclerView[index][eachIndex].shelter,
                    shelterUpdateRecyclerView[index][eachIndex].productId,
                    shelterUpdateRecyclerView[index][eachIndex].url
                )
                rowPreviousRecyclerView.add(eachIndex,updateRecyclerView)

            }//for eachIndex.

            // TH: sorts delete set, ascending.
            val deleteActionSetList = deleteActionSet.sortedWith(compareBy({it.row},{it.offset}))

            // TH: updates items to be deleted, by row.
            updateDeleteActionSetListRemoveInternal(deleteActionSetList,index)

            /*
            displayShelterUpdate("addAboveOrBelow-before")
            */

            // TH: if next row does NOT exist yet -> creates new row.
            if(index + 1 > shelterUpdate.size-1){
                shelterUpdate.add(rowPrevious)
                shelterUpdateRecyclerView.add(rowPreviousRecyclerView)
            }//if next row out-of-bound.
            else{
                shelterUpdate[index+1]=rowPrevious
                shelterUpdateRecyclerView[index+1]=rowPreviousRecyclerView
            }//else next row within bound.

            /*
            displayShelterUpdate("addAboveOrBelow-after")
            */

            // TH: creates new cache row.
            if(shelterUpdateCache.size<=index+1){
                shelterUpdateCache.add(1)
            }
            // TH: updates existing cache (next) row as dirty.
            shelterUpdateCache[index+1]=1

            // TH: updates local RecyclerView.
            recommendationAdapter[index+1].updateRecommendations(
                shelterUpdateRecyclerView[index+1]
            )

        }//for index.

        // TH: populates shelterUpdate, per anchored row.
        val rowAnchored = ArrayList<ShelterUpdate>()

        // TH: populates update for local RecyclerView, per anchored row.
        val rowAnchoredRecyclerView = ArrayList<RecommendationEntity>()

        // TH: populates shelterUpdate.
        val update = ShelterUpdate(
            "1",
            sentinelValue.toString(),
            (anchoredRow+terminal).toString(),
            0.toString(),
            0.toString(),
        )
        rowAnchored.add(0,update)

        // TH: populates shelterUpdateRecyclerView.
        val updateRecyclerView = RecommendationEntity (
            sentinelValue.toLong(),
            "Dummy-Author",
            (anchoredRow+terminal),
            0,
            shelterForm,
            1,
            LAYOUT_DUMMY_URL
        )

        rowAnchoredRecyclerView.add(0,updateRecyclerView)

        // TH: marks cache as dirty, at anchored row (i.e. add above), or below it (i.e. add below).
        shelterUpdateCache[anchoredRow+terminal] = 1

        /*
        displayShelterUpdate("addAboveOrBelow-before")
        */

        // TH: if next row does NOT exist yet -> creates new row.
        if(anchoredRow+terminal>=shelterUpdate.size){
            shelterUpdate.add(rowAnchored)
        }
        else{
            // TH: populates update for shelter, at anchored row (i.e. add above), or below it (i.e. add below).
            shelterUpdate[anchoredRow+terminal]=rowAnchored
        }

        // TH: if next row does NOT exist yet -> creates new row.
        if(anchoredRow+terminal>=shelterUpdateRecyclerView.size){
            shelterUpdateRecyclerView.add(rowAnchoredRecyclerView)
        }
        else{
            // TH: populates update for local RecyclerView, at anchored row (i.e. add above), or below it (i.e. add below).
            shelterUpdateRecyclerView[anchoredRow+terminal]=rowAnchoredRecyclerView
        }

        /*
        displayShelterUpdate("addAboveOrBelow-after")
        */

        // TH: updates local RecyclerView.
        recommendationAdapter[anchoredRow+terminal].updateRecommendations(
            shelterUpdateRecyclerView[anchoredRow+terminal]
        )

        // TH: removes previous highlight(s).
        for (rowInternal in 0 until recommendationAdapter.size) {
            recommendationAdapter[rowInternal].clearRecommendations()
        }//for rowInternal.

        for(rowInternal in 0 until shelterUpdateRecyclerView.size){
            // TH: updates longest row, after local insert.
            if(longest<shelterUpdateRecyclerView[rowInternal].size){
                longest = shelterUpdateRecyclerView[rowInternal].size
            }
        }//for rowInternal.

        // TH: assigns LayoutManager to RecyclerView, per row.
        // TH: assumes uniform dimension.
        for (index in 0 until shelterUpdateRecyclerView.size) {
            // TH: deploys GridLayoutManager.
            recyclerView[index].layoutManager = GridLayoutManager(
                this,
                longest,
                GridLayoutManager.VERTICAL,
                false
            )
        }

        /*
        displayDeleteActionSet("addAboveOrBelow-after")
        displayDeleteActionSetListRemove("addAboveOrBelow-after")
        */

    }//addAboveOrBelow.

    private fun displayDeleteActionSetListRemove(atWhen: String) {
        Log.i(
            "displayDeleteActionSetListRemove-${atWhen}",
            "--------------------------------------------------------"
        )
        for(row in 0 until deleteActionSetListRemove.size){
            for(column in 0 until deleteActionSetListRemove[row].size){
                Log.i(
                    "displayDeleteActionSetListRemove",
                    "deleteActionSetListRemove[$row][$column]=${deleteActionSetListRemove[row][column]}"
                )
            }//for column.
        }//for row.
    }//displayDeleteActionSetListRemove.

    private fun compareShelterUpdateAgainstShelter(row: Int): Int {

        // TH: returns 1 if additional item(s) already added in user interface.
        if(shelterUpdate[row].size!=shelter[row].size) {
            /*
            Log.i(
                "compareShelterUpdateAgainstShelter",
                "returns 1 @ row=${row}."
            )
            */
            return 1
        }

        val shelterUpdateSorted = shelterUpdate[row]
            .sortedWith(
                compareBy { it.getOffsetNew().toInt() }
            )

        val shelterSorted = shelter[row]
            .sortedWith(
                compareBy { it.offset }
            )

        // TH: returns 1 if at least one item already shuffled around.
        for(offset in shelterUpdateSorted.indices) {

            if(
                shelterUpdateSorted[offset].getRecommendationId().toInt()==sentinelValue
            ) {
                /*
                Log.i(
                    "compareShelterUpdateAgainstShelter",
                    "returns 1 @ row=${row}."
                )
                */
                return 1
            }

            if(
                shelterUpdateSorted[offset].getProductId().toLong()
                !=shelterSorted[offset].productId
            ) return 1

            if(
                shelterUpdateSorted[offset].getOffsetNew().toInt()
                !=shelterSorted[offset].offset
            ) return 1

        }//for offset.
        /*
        Log.i(
            "compareShelterUpdateAgainstShelter",
            "returns 0 @ row=${row}."
        )
        */
        return 0

    }//compareShelterUpdateAgainstShelter.

    private fun disableSetup() {
        modeSetup = 0
    }//disableInsertRow.

    private fun enableSetup() {
        modeSetup = 1
    }//enableInsertRow.

    // TH: marks targets for delete.
    private fun markForDelete(shelterForm: String?) {

        val productIdForm = productIdDeleteEdit.text.toString().trim()

        if(productIdForm.isEmpty()){
            val toast = Toast.makeText(
                this,
                getString(R.string.productId_remove_error_empty),
                Toast.LENGTH_LONG
            )
            toast.setGravity(Gravity.CENTER,0,0)
            toast.show()
        }
        else{

            // TH: helps capture productId locations, per row.
            val deleteIndexIterator: ArrayList<ArrayList<Int>> = arrayListOf()

            for (index in 0 until recommendationAdapter.size) {
                deleteIndexIterator.add(
                    index,
                    recommendationAdapter[index].searchRecommendationsForDelete(productIdForm)
                )
            }//for index.

            for (index in 0 until deleteIndexIterator.size) {
                // TH: removes empty list(s).
                deleteIndexIterator
                    .filter {
                        it.size == 0
                    }
                    .map {
                        deleteIndexIterator.remove(it)
                    }
            }

            when {
                shelterForm == null -> {
                    val toast = Toast.makeText(
                        this,
                        getString(R.string.shelter_error_empty),
                        Toast.LENGTH_LONG
                    )
                    toast.setGravity(Gravity.CENTER,0,0)
                    toast.show()
                }
                deleteIndexIterator.size==0 -> {
                    val toast = Toast.makeText(
                        this,
                        "$productIdForm NOT FOUND !!",
                        Toast.LENGTH_LONG
                    )
                    toast.setGravity(Gravity.CENTER,0,0)
                    toast.show()
                }
                else -> {
                    /*
                    val toast = Toast.makeText(
                        this,
                        "$productIdForm FOUND !!",
                        Toast.LENGTH_LONG
                    )
                    toast.setGravity(Gravity.CENTER,0,0)
                    toast.show()
                    */
                }
            }//when.

        }//else.

        // TH: helps de-bounce button push.
        productIdDeleteEdit.text.clear()

    }//markForDelete.

    private fun disableHighLight() {

        // TH: disables highlight.
        modeHighLight = 0

        productIdHighLightEdit.isEnabled = false
        productIdHighLightEdit.text.clear()
        productIdHighLightEdit.visibility = View.GONE

        productIdHighLightButton.isEnabled  = false
        productIdHighLightButton.isSelected = false
        productIdHighLightButton.visibility = View.GONE

    }//disableHighLight.

    private fun enableHighLight() {

        // TH: enables highlight.
        modeHighLight = 1

        productIdHighLightEdit.isEnabled = true
        productIdHighLightEdit.text.clear()
        productIdHighLightEdit.visibility = View.VISIBLE

        productIdHighLightButton.isEnabled  = true
        productIdHighLightButton.isSelected = false
        productIdHighLightButton.visibility = View.VISIBLE

    }//enableHighLight.

    private fun initializeStateMachine() {

        // TH: disables all buttons.
        clearAll()

        // TH: always enables CLEAR mode.
        productIdClear.isEnabled = true

        // TH: enables mode during start-up.
        productIdSetupMode.isEnabled = true

        // TH: enables mode during start-up.
        productIdHighLightMode.isEnabled = true

        // TH: enables mode during start-up.
        productIdAddMode.isEnabled = true

        // TH: enables mode during start-up.
        productIdDeleteMode.isEnabled = true

        // TH: enables 'Submit' only when necessary.
        submitActionButton.isEnabled = compareShelterUpdateAgainstShelter()

    }//initializeStateMachine.

    private fun clearAll() {

        // TH: disables mode.
        disableSetup()
        productIdSetupMode.isEnabled = false

        // TH: disables mode.
        disableHighLight()
        productIdHighLightMode.isEnabled = false

        // TH: disables mode.
        disableAdd()
        productIdAddMode.isEnabled = false

        // TH: disables mode.
        disableDelete()
        productIdDeleteMode.isEnabled = false

        // TH: disables mode.
        disableSubmit()
        submitActionButton.isEnabled = false

    }//clearAll.

    private fun disableSubmit() {

        submitActionButton.isEnabled  = false
        submitActionButton.isSelected = false

    }//disableSubmit.

    private fun disableDelete() {

        // TH: disables delete.
        modeDelete = 0

        productIdDeleteEdit.isEnabled = false
        productIdDeleteEdit.text.clear()
        productIdDeleteEdit.visibility = View.GONE

        modeDeleteSingle = 0
        deleteSingleButton.isEnabled  = false
        deleteSingleButton.isSelected = false
        deleteSingleButton.visibility = View.GONE

        modeDeleteMultiple = 0
        deleteMultipleButton.isEnabled  = false
        deleteMultipleButton.isSelected = false
        deleteMultipleButton.visibility = View.GONE

        deleteActionButton.isEnabled  = false
        deleteActionButton.isSelected = false
        deleteActionButton.visibility = View.GONE

    }//disableDelete.

    private fun enableDelete() {

        // TH: enables delete.
        modeDelete = 1

        productIdDeleteEdit.isEnabled = true
        productIdDeleteEdit.text.clear()
        productIdDeleteEdit.visibility = View.VISIBLE

        modeDeleteSingle = 0
        deleteSingleButton.isEnabled  = true
        deleteSingleButton.isSelected = false
        deleteSingleButton.visibility = View.VISIBLE

        modeDeleteMultiple = 0
        deleteMultipleButton.isEnabled  = true
        deleteMultipleButton.isSelected = false
        deleteMultipleButton.visibility = View.VISIBLE

    }//enableDelete.

    private fun disableAdd() {

        // TH: disables add.
        modeAdd = 0

        productIdAnchorEdit.isEnabled = false
        productIdAnchorEdit.text.clear()
        productIdAnchorEdit.visibility = View.GONE

        productIdAnchorButton.isEnabled  = false
        productIdAnchorButton.isSelected = false
        productIdAnchorButton.visibility = View.GONE

        productIdAddEdit.isEnabled = false
        productIdAddEdit.text.clear()
        productIdAddEdit.visibility = View.GONE

        productIdAddLeftButton.isEnabled  = false
        productIdAddLeftButton.isSelected = false
        productIdAddLeftButton.visibility = View.GONE

        productIdAddRightButton.isEnabled  = false
        productIdAddRightButton.isSelected = false
        productIdAddRightButton.visibility = View.GONE

        productIdAddAboveButton.isEnabled  = false
        productIdAddAboveButton.isSelected = false
        productIdAddAboveButton.visibility = View.GONE

        productIdAddBelowButton.isEnabled  = false
        productIdAddBelowButton.isSelected = false
        productIdAddBelowButton.visibility = View.GONE

        productIdShiftLeftButton.isEnabled  = false
        productIdShiftLeftButton.isSelected = false
        productIdShiftLeftButton.visibility = View.GONE

        productIdShiftRightButton.isEnabled  = false
        productIdShiftRightButton.isSelected = false
        productIdShiftRightButton.visibility = View.GONE

        addActionButton.isEnabled  = false
        addActionButton.isSelected = false
        addActionButton.visibility = View.GONE

        // TH: clears out all items for anchor.
        anchorActionSet = mutableSetOf()
        anchoredRow      = sentinelValue
        anchoredIndex    = sentinelValue
        anchoredRowIndex = arrayListOf()

    }//disableAdd.

    private fun enableAdd() {

        // TH: enables add.
        modeAdd = 1

        productIdAnchorEdit.isEnabled = true
        productIdAnchorEdit.text.clear()
        productIdAnchorEdit.visibility = View.VISIBLE

        productIdAnchorButton.isEnabled  = true
        productIdAnchorButton.isSelected = false
        productIdAnchorButton.visibility = View.VISIBLE

        productIdAddEdit.isEnabled = true
        productIdAddEdit.text.clear()
        productIdAddEdit.visibility = View.VISIBLE

        productIdAddLeftButton.isEnabled  = true
        productIdAddLeftButton.isSelected = false
        productIdAddLeftButton.visibility = View.VISIBLE

        productIdAddRightButton.isEnabled  = true
        productIdAddRightButton.isSelected = false
        productIdAddRightButton.visibility = View.VISIBLE

        productIdAddAboveButton.isEnabled  = true
        productIdAddAboveButton.isSelected = false
        productIdAddAboveButton.visibility = View.VISIBLE

        productIdAddBelowButton.isEnabled  = true
        productIdAddBelowButton.isSelected = false
        productIdAddBelowButton.visibility = View.VISIBLE

        productIdShiftLeftButton.isEnabled  = true
        productIdShiftLeftButton.isSelected = false
        productIdShiftLeftButton.visibility = View.VISIBLE

        productIdShiftRightButton.isEnabled  = true
        productIdShiftRightButton.isSelected = false
        productIdShiftRightButton.visibility = View.VISIBLE

        addActionButton.isEnabled  = true
        addActionButton.isSelected = false
        addActionButton.visibility = View.VISIBLE

    }//enableAdd.

}//ShelterActivity.

// TH: represents local copy of shelter (i.e. helps submission to back-end).
class ShelterUpdate(
    private var productId:  String,
    private var recommendationId: String,
    private var row: String,
    private var offset_old: String,
    private var offset_new: String
) {//ShelterUpdate.

    fun getProductId()        = productId
    fun getRecommendationId() = recommendationId
    fun getRow()              = row
    fun getOffsetOld()        = offset_old
    fun getOffsetNew()        = offset_new

    fun setOffsetNew(input: Int)            { offset_new       = input.toString() }
    fun setOffsetOld(input: String)         { offset_old       = input }
    fun setRecommendationIdNew(input: Long) { recommendationId = input.toString() }

}