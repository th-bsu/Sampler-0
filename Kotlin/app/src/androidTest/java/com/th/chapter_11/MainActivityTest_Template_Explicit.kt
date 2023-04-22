package com.th.chapter_11

import android.content.Intent
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.th.chapter_11.adapter.RecommendationAdapter
import com.th.chapter_11.util.EspressoIdlingResource
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

// TH: https://www.youtube.com/watch?v=06E4hxpfkME
// TH: https://medium.com/stepstone-tech/better-tests-with-androidxs-activityscenario-in-kotlin-part-1-6a6376b713ea

class MainActivityTest_Template_Explicit {

    private var skipTest=false

    // TH: succeeded with horizontal orientation: 40.
    private val delayThreadMain = 40 // 10, 20, 40.

    private val intent = Intent(
        ApplicationProvider.getApplicationContext(),
        MainActivity::class.java
    )

    // TH: helps launch given Activity before test starts, and
    //     helps close same Activity after test ends.
    @get:Rule
    val mainActivityRule = ActivityScenarioRule<MainActivity>(intent)

    @Before
    fun setUp() {

        val espressoTimeOut = 1

        IdlingPolicies.setMasterPolicyTimeout(
            espressoTimeOut.toLong(),
            TimeUnit.HOURS
        )
        IdlingPolicies.setIdlingResourceTimeout(
            espressoTimeOut.toLong(),
            TimeUnit.HOURS
        )

        IdlingRegistry.getInstance().register(
            EspressoIdlingResource.getCounterIdler()
        )

    }//setUp.

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(
            EspressoIdlingResource.getCounterIdler()
        )
    }//tearDown.

    private fun insertHorizontal(
        maxLimit: Int,
        lastAnchorIndexRow: Int
    ) {

        Log.i(
            "MainActivityTest (Started, Horizontal)",
            "..."
        )

        // TH: represents overall string.
        val overallString: ArrayList<Int> = arrayListOf()

        // TH: represents initial string, right after setup.
        overallString.add(1)
        Log.i(
            "MainActivityTest (initial, horizontal)",
            "overallString=$overallString"
        )

        // TH: tracks running anchor letter, after each insert.
        var lastAnchorLetter = "1"

        // TH: tracks running anchor index, after each insert.
        var lastAnchorIndex = 0

        Log.i(
            "MainActivityTest (horizontal)",
            "lastAnchorLetter=${lastAnchorLetter} @ $lastAnchorIndex"
        )

        // TH: generates hashMap of alphabet.
        // TH: represents how many available indices for insertion.
        // TH: A - (0,1)
        // TH: B - (0,1,2)
        // TH: C - (0,1,2,3)
        // TH: D - (0,1,2,3,4)
        // TH: E - (0,1,2,3,4,5)
        // TH: F - (0,1,2,3,4,5,6)
        val map = hashMapOf<Int,ArrayList<Int>>()

        // TH: remembers last letter inserted.
        val productIdStringList: ArrayList<Int> = arrayListOf()

        // TH: remembers last insertion index per letter.
        val mapLastInsert = hashMapOf<Int,Int>()

        // TH: helps during backtrack.
        val mapRemoveAgain = hashMapOf<Int,Boolean>()

        var limit = 1
        val alphabetStart = 1
        val alphabetEnd   = alphabetStart + limit

        for(eachLimit in alphabetStart until alphabetEnd){

            // represents how many available indices for given letter.
            val listSizeLoop: ArrayList<Int> = arrayListOf()

            for(eachSize in 0 until eachLimit+1){
                listSizeLoop.add(eachSize)
            }//for eachSize.

            map[eachLimit] = listSizeLoop
            mapRemoveAgain[eachLimit] = false

        }//for eachLimit.

        // TH: sorts by key.
        map.toSortedMap().toMap(map)
        mapRemoveAgain.toSortedMap().toMap(mapRemoveAgain)

        // TH: tracks number of steps taken.
        var steps = 0

        while(map.size>0){

            // TH: removes first list from stack.
            val listGenerate = map.remove(limit)

            // TH: removes first item from list.
            val itemGenerate = listGenerate?.removeAt(0)

            // TH: if list!=empty, loads back on stack.
            if(listGenerate?.size!! >0){
                map[limit] = listGenerate
            }
            else{
                mapRemoveAgain[limit]=true
            }

            // TH: generates productId for insertion.
            val repeatLimit = 2
            var productIdString = ""

            // TH: experimental.
            Espresso
                .onView(
                    allOf(
                        withId(
                            R.id.button_mode_add
                        ),
                        withText(
                            "@string/button_mode_add_text"
                        ),
                        isDisplayed()
                    )
                )

            Espresso
                .onView(
                    allOf(
                        withId(
                            R.id.button_mode_add
                        )
                    )
                )
                .perform(
                    scrollTo(),
                    click()
                )

            // TH: inserts given letter at given index.
            if (itemGenerate != null) {

                for(repeat in 0 until repeatLimit){
                    productIdString += limit.toString()
                }//for repeat.

                if(itemGenerate<=lastAnchorIndex){

                    // TH: deals with running anchor index.
                    if(itemGenerate==lastAnchorIndex){

                        // TH: recommends anchor items in RecyclerView (i.e. EditText).
                        Espresso
                            .onView(
                                allOf(
                                    withId(
                                        R.id.productIdAnchor
                                    ),
                                    withHint(
                                        "@string/productIdAnchor_hint"
                                    ),
                                    isDisplayed()
                                )
                            )

                        Espresso
                            .onView(
                                allOf(
                                    withId(
                                        R.id.productIdAnchor
                                    )
                                )
                            )
                            .perform(
                                scrollTo(),
                                click(),
                                typeText(
                                    lastAnchorLetter
                                ),
                                closeSoftKeyboard()
                            )

                        // TH: recommends anchor items in RecyclerView (i.e. Button).
                        Espresso
                            .onView(
                                allOf(
                                    withId(
                                        R.id.button_anchor_productId
                                    ),
                                    withText(
                                        "@string/button_anchor_text"
                                    ),
                                    isDisplayed()
                                )
                            )

                        Espresso
                            .onView(
                                allOf(
                                    withId(
                                        R.id.button_anchor_productId
                                    )
                                )
                            )
                            .perform(
                                scrollTo(),
                                click()
                            )

                        // TH: selects RecyclerView item at running anchor index.
                        selectRecyclerViewMatrix(lastAnchorIndexRow,lastAnchorIndex)

                        // TH: puts EditText in focus.
                        Espresso
                            .onView(
                                allOf(
                                    withId(
                                        R.id.productIdAdd
                                    ),
                                    withHint(
                                        "@string/productIdAdd_hint"
                                    ),
                                    isDisplayed()
                                )
                            )

                        // TH: types text in EditText.
                        Espresso
                            .onView(
                                allOf(
                                    withId(
                                        R.id.productIdAdd
                                    )
                                )
                            )
                            .perform(
                                scrollTo(),
                                click(),
                                typeText(
                                    productIdString
                                ),
                                closeSoftKeyboard()
                            )

                        // TH: issues insert-left to running anchor index.
                        Espresso
                            .onView(
                                allOf(
                                    withId(
                                        R.id.button_addLEFT_productId
                                    ),
                                    withText(
                                        "@string/button_addLEFT_text"
                                    ),
                                    isDisplayed()
                                )
                            )

                        Espresso
                            .onView(
                                allOf(
                                    withId(
                                        R.id.button_addLEFT_productId
                                    )
                                )
                            )
                            .perform(
                                scrollTo(),
                                click()
                            )

                    }//if itemGenerate==lastAnchorIndex.

                    // TH: deals with itemGenerate (itemGenerate<lastAnchorIndex).
                    else{

                        // TH: determines which productId currently located at index 'itemGenerate'.
                        val overallStringAtItemGenerate = overallString[itemGenerate]

                        // TH: recommends anchor items in RecyclerView (i.e. EditText).
                        Espresso
                            .onView(
                                allOf(
                                    withId(
                                        R.id.productIdAnchor
                                    ),
                                    withHint(
                                        "@string/productIdAnchor_hint"
                                    ),
                                    isDisplayed()
                                )
                            )

                        Espresso
                            .onView(
                                allOf(
                                    withId(
                                        R.id.productIdAnchor
                                    )
                                )
                            )
                            .perform(
                                scrollTo(),
                                click(),
                                typeText(
                                    overallStringAtItemGenerate.toString()
                                ),
                                closeSoftKeyboard()
                            )

                        // TH: recommends anchor items in RecyclerView (i.e. Button).
                        Espresso
                            .onView(
                                allOf(
                                    withId(
                                        R.id.button_anchor_productId
                                    ),
                                    withText(
                                        "@string/button_anchor_text"
                                    ),
                                    isDisplayed()
                                )
                            )

                        Espresso
                            .onView(
                                allOf(
                                    withId(
                                        R.id.button_anchor_productId
                                    )
                                )
                            )
                            .perform(
                                scrollTo(),
                                click()
                            )

                        // TH: selects RecyclerView item at itemGenerate.
                        selectRecyclerViewMatrix(lastAnchorIndexRow,itemGenerate)

                        // TH: puts EditText in focus.
                        Espresso
                            .onView(
                                allOf(
                                    withId(
                                        R.id.productIdAdd
                                    ),
                                    withHint(
                                        "@string/productIdAdd_hint"
                                    ),
                                    isDisplayed()
                                )
                            )

                        // TH: types text in EditText.
                        Espresso
                            .onView(
                                allOf(
                                    withId(
                                        R.id.productIdAdd
                                    )
                                )
                            )
                            .perform(
                                scrollTo(),
                                click(),
                                typeText(
                                    productIdString
                                ),
                                closeSoftKeyboard()
                            )

                        // TH: issues insert-left to that productId, see above.
                        Espresso
                            .onView(
                                allOf(
                                    withId(
                                        R.id.button_addLEFT_productId
                                    ),
                                    withText(
                                        "@string/button_addLEFT_text"
                                    ),
                                    isDisplayed()
                                )
                            )

                        Espresso
                            .onView(
                                allOf(
                                    withId(
                                        R.id.button_addLEFT_productId
                                    )
                                )
                            )
                            .perform(
                                scrollTo(),
                                click()
                            )

                    }//else itemGenerate<lastAnchorIndex.

                }//if itemGenerate<=lastAnchorIndex.
                else{ /*itemGenerate>lastAnchorIndex*/

                    // TH: determines which productId currently located at index 'lastAnchorIndex'.
                    val overallStringAtLastAnchorIndex = overallString[lastAnchorIndex]

                    // TH: recommends anchor items in RecyclerView (i.e. EditText).
                    Espresso
                        .onView(
                            allOf(
                                withId(
                                    R.id.productIdAnchor
                                ),
                                withHint(
                                    "@string/productIdAnchor_hint"
                                ),
                                isDisplayed()
                            )
                        )

                    Espresso
                        .onView(
                            allOf(
                                withId(
                                    R.id.productIdAnchor
                                )
                            )
                        )
                        .perform(
                            scrollTo(),
                            click(),
                            typeText(
                                overallStringAtLastAnchorIndex.toString()
                            ),
                            closeSoftKeyboard()
                        )

                    // TH: recommends anchor items in RecyclerView (i.e. Button).
                    Espresso
                        .onView(
                            allOf(
                                withId(
                                    R.id.button_anchor_productId
                                ),
                                withText(
                                    "@string/button_anchor_text"
                                ),
                                isDisplayed()
                            )
                        )

                    Espresso
                        .onView(
                            allOf(
                                withId(
                                    R.id.button_anchor_productId
                                )
                            )
                        )
                        .perform(
                            scrollTo(),
                            click()
                        )

                    // TH: selects RecyclerView item at lastAnchorIndex.
                    selectRecyclerViewMatrix(lastAnchorIndexRow,lastAnchorIndex)

                    // TH: puts EditText in focus.
                    Espresso
                        .onView(
                            allOf(
                                withId(
                                    R.id.productIdAdd
                                ),
                                withHint(
                                    "@string/productIdAdd_hint"
                                ),
                                isDisplayed()
                            )
                        )

                    // TH: types text in EditText.
                    Espresso
                        .onView(
                            allOf(
                                withId(
                                    R.id.productIdAdd
                                )
                            )
                        )
                        .perform(
                            scrollTo(),
                            click(),
                            typeText(
                                productIdString
                            ),
                            closeSoftKeyboard()
                        )

                    // TH: issues insert-right to that productId, see above.
                    Espresso
                        .onView(
                            allOf(
                                withId(
                                    R.id.button_addRIGHT_productId
                                ),
                                withText(
                                    "@string/button_addRIGHT_text"
                                ),
                                isDisplayed()
                            )
                        )

                    Espresso
                        .onView(
                            allOf(
                                withId(
                                    R.id.button_addRIGHT_productId
                                )
                            )
                        )
                        .perform(
                            scrollTo(),
                            click()
                        )

                }//else itemGenerate>lastAnchorIndex.

                // TH: original.
                clearAllAndSubmit()

                Log.i(
                    "MainActivityTest (horizontal)",
                    "steps=${++steps}"
                )


                // TH: inserts given letter at available index.
                overallString.add(itemGenerate,productIdString.toInt())
                Log.i(
                    "MainActivityTest (insert, horizontal)",
                    "overallString=$overallString"
                )

                // TH: updates running anchor index.
                lastAnchorIndex = itemGenerate

                // TH: updates running anchor letter.
                lastAnchorLetter = productIdString

                Log.i(
                    "MainActivityTest (horizontal)",
                    "lastAnchorLetter=${lastAnchorLetter} @ $lastAnchorIndex"
                )

                // TH: remembers last insertion index per letter.
                mapLastInsert[productIdString.toInt()]=itemGenerate

                // TH: sorts by key.
                mapLastInsert.toSortedMap().toMap(mapLastInsert)

                // TH: remembers last letter inserted.
                // TH: avoids duplicates.
                if(!productIdStringList.contains(productIdString.toInt())){
                    // TH: remembers last letter inserted.
                    productIdStringList.add(productIdString.toInt())
                }

            }//if itemGenerate != null.

            // TH: initiates backtrack.
            if(overallString.size==maxLimit){

                // TH: removes letter from last insertion point.
                if (itemGenerate != null) {
                    mapLastInsert[
                            productIdString.toInt()
                    ]?.let {
                        lastAnchorIndex  = it
                        lastAnchorLetter = overallString.removeAt(it).toString()
                    }
                }

                Log.i(
                    "MainActivityTest (remove horizontal,  after)",
                    "overallString=$overallString"
                )

                Log.i(
                    "MainActivityTest (horizontal)",
                    "lastAnchorLetter=${lastAnchorLetter} @ $lastAnchorIndex"
                )

                // TH: enables DELETE mode.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.button_mode_delete
                            ),
                            withText(
                                "@string/button_mode_delete_text"
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.button_mode_delete
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        click()
                    )

                issueDeleteSingleMatrix(lastAnchorIndexRow,lastAnchorIndex,lastAnchorLetter)

                // TH: original.
                clearAllAndSubmit()

                Log.i(
                    "MainActivityTest (horizontal)",
                    "steps=${++steps}"
                )

                while(limit>=1){

                    // TH: indicates NO more available index for higher-indexed letter.
                    if(mapRemoveAgain[limit]==true){

                        limit-=1

                        productIdStringList.removeAt(
                            limit
                        )

                        // TH: marks completion of test sequence.
                        if(limit<1) {
                            Log.i(
                                "MainActivityTest (Ended, Horizontal)",
                                "overallString=$overallString"
                            )
                            break
                        }

                        // TH: peaks into lower-indexed letter.
                        val productIdStringListRemove = productIdStringList[limit-1]

                        mapLastInsert[
                                productIdStringListRemove
                        ]?.let {
                            lastAnchorIndex  = it
                            lastAnchorLetter = overallString.removeAt(it).toString()
                        }

                        Log.i(
                            "MainActivityTest (backtrack horizontal,  remove)",
                            "overallString=$overallString"
                        )

                        Log.i(
                            "MainActivityTest (horizontal)",
                            "lastAnchorLetter=${lastAnchorLetter} @ $lastAnchorIndex"
                        )

                        // TH: experimental.
                        // TH: enables DELETE mode.
                        Espresso
                            .onView(
                                allOf(
                                    withId(
                                        R.id.button_mode_delete
                                    ),
                                    withText(
                                        "@string/button_mode_delete_text"
                                    ),
                                    isDisplayed()
                                )
                            )

                        Espresso
                            .onView(
                                allOf(
                                    withId(
                                        R.id.button_mode_delete
                                    )
                                )
                            )
                            .perform(
                                scrollTo(),
                                click()
                            )

                        issueDeleteMultipleMatrix(lastAnchorIndexRow,lastAnchorIndex,lastAnchorLetter)

                        // TH: original.
                        clearAllAndSubmit()

                        Log.i(
                            "MainActivityTest (horizontal)",
                            "steps=${++steps}"
                        )

                    }//if mapRemoveAgain[limit]==true.

                    else{
                        break
                    }//else mapRemoveAgain[limit]!=true.

                }//while >=1.

            }//if overallString.size==maxLimit.
            else{

                limit = overallString.size

                // represents how many available indices for given letter.
                val listSizeLoop: ArrayList<Int> = arrayListOf()

                for(index in 0 until limit+1){
                    listSizeLoop.add(index)
                }//for index.

                map[limit] = listSizeLoop
                mapRemoveAgain[limit] = false

            }//else.

            // TH: experimental.
            Espresso
                .onView(
                    allOf(
                        withId(
                            R.id.button_clear
                        ),
                        withText(
                            "@string/button_clear_text"
                        ),
                        isDisplayed()
                    )
                )

            Espresso
                .onView(
                    allOf(
                        withId(
                            R.id.button_clear
                        )
                    )
                )
                .perform(
                    scrollTo(),
                    click()
                )

        }//while map.size > 0.

    }//insertHorizontal.

    private fun issueDeleteMultipleMatrix(
        lastAnchorIndexRow: Int,
        lastAnchorIndexColumn: Int,
        lastAnchorLetter: String
    ) {

        // TH: recommends delete items in RecyclerView (i.e. EditText).
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.productIdDelete
                    ),
                    withHint(
                        "@string/productIdDelete_hint"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.productIdDelete
                    )
                )
            )
            .perform(
                scrollTo(),
                click(),
                typeText(
                    lastAnchorLetter
                ),
                closeSoftKeyboard()
            )

        // TH: issues delete-multiple.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_delete_multiple
                    ),
                    withText(
                        "@string/button_delete_multiple_text"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_delete_multiple
                    )
                )
            )
            .perform(
                scrollTo(),
                click()
            )

        selectRecyclerViewMatrix(lastAnchorIndexRow,lastAnchorIndexColumn)

        // TH: submits local delete.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_delete_action
                    ),
                    withText(
                        "@string/button_delete_action_text"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_delete_action
                    )
                )
            )
            .perform(
                scrollTo(),
                click()
            )

    }//issueDeleteMultipleMatrix.

    private fun issueDeleteSingleMatrix(
        lastAnchorIndexRow: Int,
        lastAnchorIndexColumn: Int,
        lastAnchorLetter: String
    ) {

        // TH: recommends delete items in RecyclerView (i.e. EditText).
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.productIdDelete
                    ),
                    withHint(
                        "@string/productIdDelete_hint"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.productIdDelete
                    )
                )
            )
            .perform(
                scrollTo(),
                click(),
                typeText(
                    lastAnchorLetter
                ),
                closeSoftKeyboard()
            )

        // TH: issues delete-single.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_delete_single
                    ),
                    withText(
                        "@string/button_delete_single_text"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_delete_single
                    )
                )
            )
            .perform(
                scrollTo(),
                click()
            )

        selectRecyclerViewMatrix(lastAnchorIndexRow,lastAnchorIndexColumn)

        // TH: represents wait for re-try.
        delayMainThread(delayThreadMain.toLong())

        // TH: submits local delete.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_delete_action
                    ),
                    withText(
                        "@string/button_delete_action_text"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_delete_action
                    )
                )
            )
            .perform(
                scrollTo(),
                click()
            )

    }//issueDeleteSingleMatrix.

    private fun selectRecyclerViewMatrix(
        lastAnchorIndexRow: Int,
        lastAnchorIndexColumn: Int
    ) {

        // TH: represents wait for re-try.
        delayMainThread(delayThreadMain.toLong())

        // TH: selects particular item inside matrix.
        when (lastAnchorIndexRow) {

            0 -> {

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_0
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_0
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==0.

            1 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_1
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_1
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==1.

            2 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_2
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_2
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==2.

            3 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_3
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_3
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==3.

            4 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_4
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_4
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==4.

            5 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_5
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_5
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==5.

            6 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_6
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_6
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==6.

            7 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_7
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_7
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==7.

            8 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_8
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_8
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==8.

            9 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_9
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_9
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==9.

            10 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_10
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_10
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==10.

            11 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_11
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_11
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==11.

            12 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_12
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_12
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==12.

            13 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_13
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_13
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==13.

            14 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_14
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_14
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==14.

            15 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_15
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_15
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==15.

            16 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_16
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_16
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==16.

            17 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_17
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_17
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==17.

            18 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_18
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_18
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==18.

            19 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_19
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_19
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==19.

            20 -> {

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_20
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_20
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==20.

            21 -> {

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_21
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_21
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==21.

            22 -> {

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_22
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_22
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==22.

            23 -> {

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_23
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_23
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==23.

            24 -> {

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_24
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_24
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==24.

            25 -> {

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_25
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_25
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==25.

            26 -> {

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_26
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_26
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==26.

            27 -> {

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_27
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_27
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==27.

            28 -> {

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_28
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_28
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==28.

            29 -> {

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_29
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_29
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            lastAnchorIndexColumn,
                            click()
                        )
                    )

            }//if ==29.

        }//when.

    }//selectRecyclerViewMatrix.

    private fun issueDeleteMultipleVertical(lastAnchorIndex: Int) {

        // TH: recommends delete items in RecyclerView (i.e. EditText).
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.productIdDelete
                    ),
                    withHint(
                        "@string/productIdDelete_hint"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.productIdDelete
                    )
                )
            )
            .perform(
                scrollTo(),
                click(),
                typeText(
                    "1" /*lastAnchorLetter*/
                ),
                closeSoftKeyboard()
            )

        // TH: issues delete-multiple.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_delete_multiple
                    ),
                    withText(
                        "@string/button_delete_multiple_text"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_delete_multiple
                    )
                )
            )
            .perform(
                scrollTo(),
                click()
            )

        // TH: selects RecyclerView item at lastAnchorIndex.
        selectRecyclerViewVertical(lastAnchorIndex)

        // TH: submits local delete.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_delete_action
                    ),
                    withText(
                        "@string/button_delete_action_text"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_delete_action
                    )
                )
            )
            .perform(
                scrollTo(),
                click()
            )

    }//issueDeleteMultipleVertical.

    private fun issueDeleteSingleVertical(lastAnchorIndex: Int) {

        // TH: recommends delete items in RecyclerView (i.e. EditText).
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.productIdDelete
                    ),
                    withHint(
                        "@string/productIdDelete_hint"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.productIdDelete
                    )
                )
            )
            .perform(
                scrollTo(),
                click(),
                typeText(
                    "1" /*lastAnchorLetter*/
                ),
                closeSoftKeyboard()
            )

        // TH: issues delete-single.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_delete_single
                    ),
                    withText(
                        "@string/button_delete_single_text"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_delete_single
                    )
                )
            )
            .perform(
                scrollTo(),
                click()
            )

        // TH: selects RecyclerView item at lastAnchorIndex.
        selectRecyclerViewVertical(lastAnchorIndex)

        // TH: submits local delete.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_delete_action
                    ),
                    withText(
                        "@string/button_delete_action_text"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_delete_action
                    )
                )
            )
            .perform(
                scrollTo(),
                click()
            )

    }//issueDeleteSingleVertical.

    private fun selectRecyclerViewVertical(lastAnchorIndex: Int) {

        when (lastAnchorIndex) {

            0 -> {

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_0
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_0
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==0.

            1 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_1
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_1
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==1.

            2 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_2
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_2
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==2.

            3 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_3
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_3
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==3.

            4 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_4
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_4
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==4.

            5 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_5
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_5
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==5.

            6 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_6
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_6
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==6.

            7 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_7
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_7
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==7.

            8 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_8
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_8
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==8.

            9 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_9
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_9
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==9.

            10 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_10
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_10
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==10.

            11 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_11
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_11
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==11.

            12 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_12
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_12
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==12.

            13 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_13
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_13
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==13.

            14 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_14
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_14
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==14.

            15 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_15
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_15
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==15.

            16 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_16
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_16
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==16.

            17 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_17
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_17
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==17.

            18 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_18
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_18
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==18.

            19 -> {

                // TH: selects RecyclerView item at running anchor index.
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_19
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_19
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==19.

            20 -> {

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_20
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_20
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==20.

            21 -> {

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_21
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_21
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==21.

            22 -> {

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_22
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_22
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==22.

            23 -> {

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_23
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_23
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==23.

            24 -> {

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_24
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_24
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==24.

            25 -> {

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_25
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_25
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==25.

            26 -> {

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_26
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_26
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==26.

            27 -> {

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_27
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_27
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==27.

            28 -> {

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_28
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_28
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==28.

            29 -> {

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_29
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.activity_shelter_recycler_view_29
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        RecyclerViewActions.actionOnItemAtPosition
                        <RecommendationAdapter.PostViewHolder>(
                            0,
                            click()
                        )
                    )

            }//if ==29.

        }//when.

    }//selectRecyclerViewVertical.

    private fun clearAllAndSubmit() {

        clearAll()

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_mode_submit
                    ),
                    withText(
                        "@string/button_mode_submit_text"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_mode_submit
                    )
                )
            )
            .perform(
                scrollTo(),
                click()
            )

    }//clearAllAndSubmit.

    private fun issueDeleteMultiple(lastAnchorIndex: Int, lastAnchorLetter: String) {

        // TH: recommends delete items in RecyclerView (i.e. EditText).
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.productIdDelete
                    ),
                    withHint(
                        "@string/productIdDelete_hint"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.productIdDelete
                    )
                )
            )
            .perform(
                scrollTo(),
                click(),
                typeText(
                    lastAnchorLetter
                ),
                closeSoftKeyboard()
            )

        // TH: issues delete-multiple.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_delete_multiple
                    ),
                    withText(
                        "@string/button_delete_multiple_text"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_delete_multiple
                    )
                )
            )
            .perform(
                scrollTo(),
                click()
            )

        // TH: selects RecyclerView item at lastAnchorIndex.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.activity_shelter_recycler_view_0
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.activity_shelter_recycler_view_0
                    )
                )
            )
            .perform(
                scrollTo(),
                RecyclerViewActions.actionOnItemAtPosition
                <RecommendationAdapter.PostViewHolder>(
                    lastAnchorIndex,
                    click()
                )
            )

        // TH: submits local delete.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_delete_action
                    ),
                    withText(
                        "@string/button_delete_action_text"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_delete_action
                    )
                )
            )
            .perform(
                scrollTo(),
                click()
            )

    }//issueDeleteMultiple.

    private fun issueDeleteSingle(lastAnchorIndex: Int, lastAnchorLetter: String) {

        // TH: recommends delete items in RecyclerView (i.e. EditText).
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.productIdDelete
                    ),
                    withHint(
                        "@string/productIdDelete_hint"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.productIdDelete
                    )
                )
            )
            .perform(
                scrollTo(),
                click(),
                typeText(
                    lastAnchorLetter
                ),
                closeSoftKeyboard()
            )

        // TH: issues delete-single.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_delete_single
                    ),
                    withText(
                        "@string/button_delete_single_text"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_delete_single
                    )
                )
            )
            .perform(
                scrollTo(),
                click()
            )

        // TH: selects RecyclerView item at lastAnchorIndex.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.activity_shelter_recycler_view_0
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.activity_shelter_recycler_view_0
                    )
                )
            )
            .perform(
                scrollTo(),
                RecyclerViewActions.actionOnItemAtPosition
                <RecommendationAdapter.PostViewHolder>(
                    lastAnchorIndex,
                    click()
                )
            )

        // TH: submits local delete.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_delete_action
                    ),
                    withText(
                        "@string/button_delete_action_text"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_delete_action
                    )
                )
            )
            .perform(
                scrollTo(),
                click()
            )

    }//issueDeleteSingle.

    //----------------------------------------------------------------------------------------------------------------

    @Test
    fun test_UI_matrix_5_by_8_case_x_Slow(){

        if(skipTest) return

        Log.i(
            "test_UI_matrix_5_by_8_case_x_Slow",
            "Started."
        )

        // TH: generates holes in matrix.
        val dummies: ArrayList<ArrayList<Int>> = arrayListOf()
        dummies.add(arrayListOf(0,0) )
        dummies.add(arrayListOf(0,2) )
        dummies.add(arrayListOf(1,3) )
        dummies.add(arrayListOf(3,0) )
        dummies.add(arrayListOf(3,4) )
        dummies.add(arrayListOf(4,2) )
        dummies.add(arrayListOf(4,3) )

        // TH: sorts arrayList.
        val sortedDummies= dummies.sortedWith(compareBy({ it[0] }, { it[1] }))

        for(index in sortedDummies.indices){
            Log.i(
                "test_UI_matrix_5_by_8_case_x_Slow",
                "sortedDummies[$index]=${sortedDummies[index]}"
            )
        }//for index.

        test_UI_matrix_Internal_Experimental(5,8,sortedDummies,100)

        Log.i(
            "test_UI_matrix_5_by_8_case_x_Slow",
            "Ended."
        )

    }//test_UI_matrix_5_by_8_case_x_Slow.

    @Test
    fun test_UI_matrix_5_by_8_case_x_Fast(){

        if(skipTest) return

        Log.i(
            "test_UI_matrix_5_by_8_case_x_Fast",
            "Started."
        )

        // TH: generates holes in matrix.
        val dummies: ArrayList<ArrayList<Int>> = arrayListOf()
        dummies.add(arrayListOf(0,0) )
        dummies.add(arrayListOf(0,2) )
        dummies.add(arrayListOf(1,3) )
        dummies.add(arrayListOf(3,0) )
        dummies.add(arrayListOf(3,4) )
        dummies.add(arrayListOf(4,2) )
        dummies.add(arrayListOf(4,3) )

        // TH: sorts arrayList.
        val sortedDummies= dummies.sortedWith(compareBy({ it[0] }, { it[1] }))

        for(index in sortedDummies.indices){
            Log.i(
                "test_UI_matrix_5_by_8_case_x_Fast",
                "sortedDummies[$index]=${sortedDummies[index]}"
            )
        }//for index.

        test_UI_matrix_Internal_Optimized(5,8,sortedDummies,100)

        Log.i(
            "test_UI_matrix_5_by_8_case_x_Fast",
            "Ended."
        )

    }//test_UI_matrix_5_by_8_case_x_Fast.

    @Test
    fun test_UI_matrix_5_by_8_case_x_Twice(){

        if(skipTest) return

        Log.i(
            "test_UI_matrix_5_by_8_case_x_Twice",
            "Started."
        )

        // TH: generates holes in matrix.
        val dummies: ArrayList<ArrayList<Int>> = arrayListOf()
        dummies.add(arrayListOf(0,0) )
        dummies.add(arrayListOf(0,2) )
        dummies.add(arrayListOf(1,3) )
        dummies.add(arrayListOf(3,0) )
        dummies.add(arrayListOf(3,4) )
        dummies.add(arrayListOf(4,2) )
        dummies.add(arrayListOf(4,3) )

        // TH: sorts arrayList.
        val sortedDummies= dummies.sortedWith(compareBy({ it[0] }, { it[1] }))

        for(index in sortedDummies.indices){
            Log.i(
                "test_UI_matrix_5_by_8_case_x_Twice",
                "sortedDummies[$index]=${sortedDummies[index]}"
            )
        }//for index.

        test_UI_matrix_Internal_Optimized_Setup_Twice(5,8,sortedDummies,100)

        Log.i(
            "test_UI_matrix_5_by_8_case_x_Twice",
            "Ended."
        )

    }//test_UI_matrix_5_by_8_case_x_Twice.

    //----------------------------------------------------------------------------------------------------------------

    private fun test_UI_matrix_Internal_Experimental(
        numRows: Int,
        numCols: Int,
        sortedDummies: List<ArrayList<Int>>,
        shelterOrderOfMagnitude: Int
    ) {

        Log.i(
            "test_UI_matrix_Internal_Experimental",
            "Started."
        )

        // TH: specifies matrix dimensions.
        val shelterBaseNumber  = numRows
        val rowTotal           = numRows
        val columnTotal        = numCols
        val matrix_2_by_2_dim  = rowTotal * columnTotal

        // TH: represents exact location based on coordinate.
        // TH: for 2-by-2 matrix (i.e. see formula below):
        // TH: 0 - (0,0) ; 1 - (0,1)
        // TH: 2 - (1,0) ; 3 - (1,1)
        val mapCoordinate = hashMapOf< Int, ArrayList<Int> >()

        for(rowIndex in 0 until rowTotal){

            for(columnIndex in 0 until columnTotal){

                // TH: maps coordinate to location.
                val coordinate=columnTotal*rowIndex+columnIndex
                val location  : ArrayList<Int> = arrayListOf()
                location.add(rowIndex)
                location.add(columnIndex)

                Log.i(
                    "test_UI_matrix_Internal_Experimental",
                    "coordinate=${coordinate} @ location=${location}"
                )

                // TH: initializes coordinates.
                mapCoordinate[coordinate]=location

            }//for columnIndex.

        }//for rowIndex.

        Log.i(
            "test_UI_matrix_Internal_Experimental",
            "--------------------------------------------------------"
        )

        Log.i(
            "test_UI_matrix_Internal_Experimental",
            "mapCoordinate.size=${mapCoordinate.size}"
        )

        for((key,value) in mapCoordinate){
            Log.i(
                "test_UI_matrix_Internal_Experimental-mapCoordinate",
                "key=${key} @ value=${value}"
            )
        }//for map.

        Log.i(
            "test_UI_matrix_Internal_Experimental",
            "--------------------------------------------------------"
        )

        // TH: populates data for matrix.
        val matrix_2_by_2_data : ArrayList<Int> = arrayListOf()
        for(index in 0 until matrix_2_by_2_dim){
            matrix_2_by_2_data.add(shelterBaseNumber*shelterOrderOfMagnitude+index)
        }//for index.

        for(index in 0 until matrix_2_by_2_data.size){
            Log.i(
                "test_UI_matrix_Internal_Experimental",
                "matrix_2_by_2_data[$index]=${matrix_2_by_2_data[index]}"
            )
        }//for index.

        // TH: represents what coordinates would go into matrix (i.e. per case).
        val mapCoordinateMatrix = hashMapOf< Int, ArrayList<ArrayList<Int>> >()

        // TH: represents what data would go into matrix (i.e. per case).
        val mapCoordinateMatrixData = hashMapOf< Int, ArrayList<Int> >()

        var totalBinaryString = ""
        for(rowIndex in 0 until numRows){
            var binaryPerRow = ""
            for(columnIndex in 0 until numCols){
                binaryPerRow += if(sortedDummies.contains(arrayListOf(rowIndex,columnIndex))){
                    "0"
                } else{
                    ShelterActivity.LAYOUT_DUMMY_ID_PRODUCT
                }
            }//for columnIndex.
            Log.i(
                "test_UI_matrix_Internal_Experimental",
                "binaryPerRow=${binaryPerRow}"
            )
            totalBinaryString+=binaryPerRow
        }//for rowIndex.

        Log.i(
            "test_UI_matrix_Internal_Experimental",
            "totalBinaryString.length=${totalBinaryString.length}"
        )

        Log.i(
            "test_UI_matrix_Internal_Experimental",
            "totalBinaryString=${totalBinaryString}"
        )

        val binaryArrayListForData: ArrayList<Int> = arrayListOf()
        val binaryArrayListForPosition: ArrayList<ArrayList<Int>> = arrayListOf()

        val eachBinaryLength = if(totalBinaryString.length==matrix_2_by_2_dim) matrix_2_by_2_dim else totalBinaryString.length

        for(eachBinaryIndex in 0 until eachBinaryLength){

            if(totalBinaryString[eachBinaryIndex].toString()==ShelterActivity.LAYOUT_DUMMY_ID_PRODUCT){

                /*
                Log.i(
                    "test_UI_matrix_Internal_Experimental",
                    "enabled ${matrix_2_by_2_data[eachBinaryIndex]} @ location=${mapCoordinate[eachBinaryIndex]}"
                )
                */

                binaryArrayListForData.add(matrix_2_by_2_data[eachBinaryIndex])
                mapCoordinate[eachBinaryIndex]?.let { binaryArrayListForPosition.add(it) }

            }//if '1' @ eachBinaryIndex.

        }//for eachBinaryIndex.

        mapCoordinateMatrixData[eachBinaryLength]=binaryArrayListForData
        mapCoordinateMatrix[eachBinaryLength]=binaryArrayListForPosition

        if(mapCoordinateMatrixData.size!=mapCoordinateMatrix.size){
            Log.i(
                "test_UI_matrix_Internal_Experimental",
                "Ended (Error, 0)."
            )
            exitProcess(1)
        }

        /*
        for((key,value) in mapCoordinateMatrix){
            val shelter = "$shelterBaseNumber-$key"
            Log.i(
                "test_UI_matrix_Internal_Experimental-mapCoordinateMatrix",
                "key=${key}: shelter=${shelter}, data=${mapCoordinateMatrixData[key]}, location=${value}"
            )
        }//for map.
        */

        // TH: specifies target key.
        val targetKey = eachBinaryLength

        // TH: helps setup template (i.e. 1st iteration ONLY).
        val firstTime = true

        // TH: initiates matrix construction.
        // TH: mapCoordinateMatrix: key==case, value==locations per case.
        // TH: mapCoordinateMatrixData:               points per case.
        for((key,value) in mapCoordinateMatrix){

            // TH: builds particular shelter, based on target key.
            if(key!=targetKey) continue

            val shelter = "$shelterBaseNumber-$key"
            Log.i(
                "test_UI_matrix_Internal_Experimental-mapCoordinateMatrix",
                "key=${key}: shelter=${shelter}"
            )

            // TH: setups template (i.e. 1st iteration ONLY).
            if(firstTime){

                Espresso
                    .onView(
                        withId(
                            R.id.shelter
                        )
                    )
                    .perform(
                        typeText(
                            shelter
                        )
                    )
                    .check(
                        matches(
                            withText(
                                shelter
                            )
                        )
                    )

                Espresso
                    .onView(
                        withId(
                            R.id.button_submit
                        )
                    )
                    .perform(
                        click()
                    )

                Espresso
                    .onView(
                        withId(
                            R.id.activity_shelter_split_30
                        )
                    )
                    .check(
                        matches(
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.button_setup
                            ),
                            withText(
                                "@string/button_setup_text"
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        withId(
                            R.id.button_setup
                        )
                    )
                    .perform(
                        scrollTo(),
                        click()
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.button_setup
                            ),
                            withText(
                                "@string/button_setup_text"
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        withId(
                            R.id.button_setup
                        )
                    )
                    .perform(
                        scrollTo(),
                        click()
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.button_mode_add
                            ),
                            withText(
                                "@string/button_mode_add_text"
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.button_mode_add
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        click()
                    )

                // TH: recommends anchor items in RecyclerView (i.e. EditText).
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.productIdAnchor
                            ),
                            withHint(
                                "@string/productIdAnchor_hint"
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.productIdAnchor
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        click(),
                        typeText(
                            ShelterActivity.LAYOUT_DUMMY_ID_PRODUCT
                        ),
                        closeSoftKeyboard()
                    )

                // TH: recommends anchor items in RecyclerView (i.e. Button).
                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.button_anchor_productId
                            ),
                            withText(
                                "@string/button_anchor_text"
                            ),
                            isDisplayed()
                        )
                    )

                Espresso
                    .onView(
                        allOf(
                            withId(
                                R.id.button_anchor_productId
                            )
                        )
                    )
                    .perform(
                        scrollTo(),
                        click()
                    )

                selectRecyclerViewMatrix(0,0)

                for(index in 0 until columnTotal-1){

                    // TH: represents wait for re-try.
                    delayMainThread(delayThreadMain.toLong())

                    Espresso
                        .onView(
                            allOf(
                                withId(
                                    R.id.button_shift_left
                                ),
                                withText(
                                    "@string/button_shift_left_text"
                                ),
                                isDisplayed()
                            )
                        )

                    Espresso
                        .onView(
                            allOf(
                                withId(
                                    R.id.button_shift_left
                                )
                            )
                        )
                        .perform(
                            scrollTo(),
                            click()
                        )

                }//for index.

                for(indexRow in 0 until rowTotal-1){

                    Espresso
                        .onView(
                            allOf(
                                withId(
                                    R.id.button_add_below
                                ),
                                withText(
                                    "@string/button_add_below_text"
                                ),
                                isDisplayed()
                            )
                        )

                    Espresso
                        .onView(
                            allOf(
                                withId(
                                    R.id.button_add_below
                                )
                            )
                        )
                        .perform(
                            scrollTo(),
                            click()
                        )

                    Log.i(
                        "test_UI_matrix_Internal_Experimental",
                        "button_add_below clicked."
                    )

                    for(index in 0 until columnTotal-1){

                        // TH: represents wait for re-try.
                        delayMainThread(delayThreadMain.toLong())

                        Espresso
                            .onView(
                                allOf(
                                    withId(
                                        R.id.button_shift_right
                                    ),
                                    withText(
                                        "@string/button_shift_right_text"
                                    ),
                                    isDisplayed()
                                )
                            )

                        Espresso
                            .onView(
                                allOf(
                                    withId(
                                        R.id.button_shift_right
                                    )
                                )
                            )
                            .perform(
                                scrollTo(),
                                click()
                            )

                    }//for index.

                }//for indexRow.

            }//if firstTime.

            for(index in 0 until value.size){
                Log.i(
                    "test_UI_matrix_Internal_Experimental",
                    "mapCoordinateMatrixData[$key][$index]=${mapCoordinateMatrixData[key]?.get(index)} @ ${value[index]}."
                )
            }//for index.

            Log.i(
                "test_UI_matrix_Internal_Experimental",
                "Case=${key} Active."
            )

            for(index in 0 until mapCoordinateMatrixData[key]?.size!!){

                // TH: selects particular anchor inside matrix.
                addProductAtGivenLocationInsideMatrix(
                    mapCoordinateMatrixData[key]?.get(index),
                    ShelterActivity.LAYOUT_DUMMY_ID_PRODUCT,
                    value[index][0],
                    value[index][1]
                )

            }//for index.

            // TH: handles ONLY one particular case.
            // TH: submits shelter for construction.
            clearAllAndSubmit()
            break

        }//for map.

        Log.i(
            "test_UI_matrix_Internal_Experimental",
            "Ended."
        )

    }//test_UI_matrix_Internal_Experimental.

    private fun test_UI_matrix_Internal_Optimized(
        numRows: Int,
        numCols: Int,
        sortedDummies: List<ArrayList<Int>>,
        shelterOrderOfMagnitude: Int
    ) {

        Log.i(
            "test_UI_matrix_Internal_Optimized",
            "Started."
        )

        // TH: specifies matrix dimensions.
        val shelterBaseNumber  = numRows
        val rowTotal           = numRows
        val columnTotal        = numCols
        val matrix_2_by_2_dim  = rowTotal * columnTotal

        // TH: represents exact location based on coordinate.
        // TH: for 2-by-2 matrix (i.e. see formula below):
        // TH: 0 - (0,0) ; 1 - (0,1)
        // TH: 2 - (1,0) ; 3 - (1,1)
        val mapCoordinate = hashMapOf< Int, ArrayList<Int> >()

        for(rowIndex in 0 until rowTotal){

            for(columnIndex in 0 until columnTotal){

                // TH: maps coordinate to location.
                val coordinate=columnTotal*rowIndex+columnIndex
                val location  : ArrayList<Int> = arrayListOf()
                location.add(rowIndex)
                location.add(columnIndex)

                Log.i(
                    "test_UI_matrix_Internal_Optimized",
                    "coordinate=${coordinate} @ location=${location}"
                )

                // TH: initializes coordinates.
                mapCoordinate[coordinate]=location

            }//for columnIndex.

        }//for rowIndex.

        Log.i(
            "test_UI_matrix_Internal_Optimized",
            "--------------------------------------------------------"
        )

        Log.i(
            "test_UI_matrix_Internal_Optimized",
            "mapCoordinate.size=${mapCoordinate.size}"
        )

        for((key,value) in mapCoordinate){
            Log.i(
                "test_UI_matrix_Internal_Optimized-mapCoordinate",
                "key=${key} @ value=${value}"
            )
        }//for map.

        Log.i(
            "test_UI_matrix_Internal_Optimized",
            "--------------------------------------------------------"
        )

        // TH: populates data for matrix.
        val matrix_2_by_2_data : ArrayList<Int> = arrayListOf()
        for(index in 0 until matrix_2_by_2_dim){
            matrix_2_by_2_data.add(shelterBaseNumber*shelterOrderOfMagnitude+index)
        }//for index.

        for(index in 0 until matrix_2_by_2_data.size){
            Log.i(
                "test_UI_matrix_Internal_Optimized",
                "matrix_2_by_2_data[$index]=${matrix_2_by_2_data[index]}"
            )
        }//for index.

        // TH: represents what coordinates would go into matrix (i.e. per case).
        val mapCoordinateMatrix = hashMapOf< Int, ArrayList<ArrayList<Int>> >()

        // TH: represents what data would go into matrix (i.e. per case).
        val mapCoordinateMatrixData = hashMapOf< Int, ArrayList<Int> >()

        var totalBinaryString = ""
        for(rowIndex in 0 until numRows){
            var binaryPerRow = ""
            for(columnIndex in 0 until numCols){
                binaryPerRow += if(sortedDummies.contains(arrayListOf(rowIndex,columnIndex))){
                    "0"
                } else{
                    ShelterActivity.LAYOUT_DUMMY_ID_PRODUCT
                }
            }//for columnIndex.
            Log.i(
                "test_UI_matrix_Internal_Optimized",
                "binaryPerRow=${binaryPerRow}"
            )
            totalBinaryString+=binaryPerRow
        }//for rowIndex.

        Log.i(
            "test_UI_matrix_Internal_Optimized",
            "totalBinaryString.length=${totalBinaryString.length}"
        )

        Log.i(
            "test_UI_matrix_Internal_Optimized",
            "totalBinaryString=${totalBinaryString}"
        )

        val binaryArrayListForData: ArrayList<Int> = arrayListOf()
        val binaryArrayListForPosition: ArrayList<ArrayList<Int>> = arrayListOf()

        val eachBinaryLength = if(totalBinaryString.length==matrix_2_by_2_dim) matrix_2_by_2_dim else totalBinaryString.length

        for(eachBinaryIndex in 0 until eachBinaryLength){

            if(totalBinaryString[eachBinaryIndex].toString()==ShelterActivity.LAYOUT_DUMMY_ID_PRODUCT){

                /*
                Log.i(
                    "test_UI_matrix_Internal_Optimized",
                    "enabled ${matrix_2_by_2_data[eachBinaryIndex]} @ location=${mapCoordinate[eachBinaryIndex]}"
                )
                */

                binaryArrayListForData.add(matrix_2_by_2_data[eachBinaryIndex])
                mapCoordinate[eachBinaryIndex]?.let { binaryArrayListForPosition.add(it) }

            }//if '1' @ eachBinaryIndex.

        }//for eachBinaryIndex.

        mapCoordinateMatrixData[eachBinaryLength]=binaryArrayListForData
        mapCoordinateMatrix[eachBinaryLength]=binaryArrayListForPosition

        if(mapCoordinateMatrixData.size!=mapCoordinateMatrix.size){
            Log.i(
                "test_UI_matrix_Internal_Optimized",
                "Ended (Error, 0)."
            )
            exitProcess(1)
        }

        /*
        for((key,value) in mapCoordinateMatrix){
            val shelter = "$shelterBaseNumber-$key"
            Log.i(
                "test_UI_matrix_Internal_Optimized-mapCoordinateMatrix",
                "key=${key}: shelter=${shelter}, data=${mapCoordinateMatrixData[key]}, location=${value}"
            )
        }//for map.
        */

        // TH: initiates matrix construction.
        // TH: mapCoordinateMatrix: key==case, value==locations per case.
        // TH: mapCoordinateMatrixData:               points per case.
        for((key,value) in mapCoordinateMatrix){

            // TH: builds particular shelter, based on target key.
            if (key != eachBinaryLength) continue

            val shelter = "$shelterBaseNumber-$key"
            Log.i(
                "test_UI_matrix_Internal_Optimized-mapCoordinateMatrix",
                "key=${key}: shelter=${shelter}"
            )

            for(index in 0 until value.size){
                Log.i(
                    "test_UI_matrix_Internal_Optimized",
                    "mapCoordinateMatrixData[$key][$index]=${mapCoordinateMatrixData[key]?.get(index)} @ ${value[index]}."
                )
            }//for index.

            Log.i(
                "test_UI_matrix_Internal_Optimized",
                "Case=${key} Active."
            )

            Espresso
                .onView(
                    withId(
                        R.id.shelter
                    )
                )
                .perform(
                    typeText(
                        shelter
                    )
                )
                .check(
                    matches(
                        withText(
                            shelter
                        )
                    )
                )

            Espresso
                .onView(
                    withId(
                        R.id.button_submit
                    )
                )
                .perform(
                    click()
                )

            Espresso
                .onView(
                    withId(
                        R.id.activity_shelter_split_30
                    )
                )
                .check(
                    matches(
                        isDisplayed()
                    )
                )

            Espresso
                .onView(
                    allOf(
                        withId(
                            R.id.button_setup
                        ),
                        withText(
                            "@string/button_setup_text"
                        ),
                        isDisplayed()
                    )
                )

            Espresso
                .onView(
                    withId(
                        R.id.button_setup
                    )
                )
                .perform(
                    scrollTo(),
                    click()
                )

            Espresso
                .onView(
                    allOf(
                        withId(
                            R.id.button_setup
                        ),
                        withText(
                            "@string/button_setup_text"
                        ),
                        isDisplayed()
                    )
                )

            Espresso
                .onView(
                    withId(
                        R.id.button_setup
                    )
                )
                .perform(
                    scrollTo(),
                    click()
                )

            // TH: toggles ADD mode.
            Espresso
                .onView(
                    allOf(
                        withId(
                            R.id.button_mode_add
                        ),
                        withText(
                            "@string/button_mode_add_text"
                        ),
                        isDisplayed()
                    )
                )

            Espresso
                .onView(
                    allOf(
                        withId(
                            R.id.button_mode_add
                        )
                    )
                )
                .perform(
                    scrollTo(),
                    click()
                )

            var initialAnchorRow=0
            var initialAnchorCol=0
            selectRecyclerViewMatrixAnchor(ShelterActivity.LAYOUT_DUMMY_ID_PRODUCT,initialAnchorRow,initialAnchorCol)

            // TH: sorts arrayList.
            val valueSorted= value.sortedWith(compareBy({ it[0] }, { it[1] }))

            // TH: tracks matrix data.
            var indexMatrixData=0

            for (indexRow in 0 until rowTotal) {

                for(indexColumn in 0 until columnTotal){

                    val coordinate = arrayListOf(indexRow,indexColumn)

                    // TH: indicates real value or dummy, per coordinate.
                    var recentlyAdded=false

                    if(
                        indexRow   ==initialAnchorRow &&
                        indexColumn==initialAnchorCol
                    ){

                        // TH: does NOTHING, as already added below.
                        if(!valueSorted.contains(coordinate)){

                            // TH: experimental.
                            recentlyAdded=false

                        }//if NOT in value.
                        else{

                            // TH: selects particular anchor inside matrix.
                            addProductAtGivenLocationInsideMatrixOptimized(
                                mapCoordinateMatrixData[key]?.get(indexMatrixData),
                                ShelterActivity.LAYOUT_DUMMY_ID_PRODUCT,
                                indexRow,
                                indexColumn
                            )

                            clearAll()

                            // TH: toggles ADD mode.
                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.button_mode_add
                                        ),
                                        withText(
                                            "@string/button_mode_add_text"
                                        ),
                                        isDisplayed()
                                    )
                                )

                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.button_mode_add
                                        )
                                    )
                                )
                                .perform(
                                    scrollTo(),
                                    click()
                                )

                            selectRecyclerViewMatrixAnchor(
                                mapCoordinateMatrixData[key]?.get(indexMatrixData).toString(),
                                indexRow,
                                indexColumn
                            )

                            recentlyAdded=true

                        }//else in value.

                    }//if initialAnchor.
                    else{

                        if(!valueSorted.contains(coordinate)){

                            // TH: addRight(1) to indexColumn-1.
                            // TH: puts EditText in focus.
                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.productIdAdd
                                        ),
                                        withHint(
                                            "@string/productIdAdd_hint"
                                        ),
                                        isDisplayed()
                                    )
                                )

                            // TH: types text in EditText.
                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.productIdAdd
                                        )
                                    )
                                )
                                .perform(
                                    scrollTo(),
                                    click(),
                                    typeText(
                                        ShelterActivity.LAYOUT_DUMMY_ID_PRODUCT
                                    ),
                                    closeSoftKeyboard()
                                )

                            // TH: issues insert-right to that productId, see above.
                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.button_addRIGHT_productId
                                        ),
                                        withText(
                                            "@string/button_addRIGHT_text"
                                        ),
                                        isDisplayed()
                                    )
                                )

                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.button_addRIGHT_productId
                                        )
                                    )
                                )
                                .perform(
                                    scrollTo(),
                                    click()
                                )

                            // TH: experimental.
                            recentlyAdded=false

                        }//if NOT in value.
                        else{

                            // TH: addRight(MatrixData) to indexColumn-1.
                            // TH: puts EditText in focus.
                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.productIdAdd
                                        ),
                                        withHint(
                                            "@string/productIdAdd_hint"
                                        ),
                                        isDisplayed()
                                    )
                                )

                            // TH: types text in EditText.
                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.productIdAdd
                                        )
                                    )
                                )
                                .perform(
                                    scrollTo(),
                                    click(),
                                    typeText(
                                        mapCoordinateMatrixData[key]?.get(indexMatrixData).toString()
                                    ),
                                    closeSoftKeyboard()
                                )

                            // TH: issues insert-right to that productId, see above.
                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.button_addRIGHT_productId
                                        ),
                                        withText(
                                            "@string/button_addRIGHT_text"
                                        ),
                                        isDisplayed()
                                    )
                                )

                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.button_addRIGHT_productId
                                        )
                                    )
                                )
                                .perform(
                                    scrollTo(),
                                    click()
                                )

                            recentlyAdded=true

                        }//else in value.

                    }//else !initialAnchor.

                    // TH: increments index only if already add MatrixData or,
                    if(recentlyAdded){
                        // TH: updates matrix data index.
                        indexMatrixData+=1
                    }

                }//for indexColumn.

                if(indexRow!=rowTotal-1){

                    Log.i(
                        "test_UI_matrix_Internal_Optimized",
                        "indexRow=${indexRow} != ${rowTotal-1}"
                    )

                    Espresso
                        .onView(
                            allOf(
                                withId(
                                    R.id.button_add_below
                                ),
                                withText(
                                    "@string/button_add_below_text"
                                ),
                                isDisplayed()
                            )
                        )

                    Espresso
                        .onView(
                            allOf(
                                withId(
                                    R.id.button_add_below
                                )
                            )
                        )
                        .perform(
                            scrollTo(),
                            click()
                        )

                    Log.i(
                        "test_UI_matrix_Internal_Optimized",
                        "button_add_below clicked."
                    )

                    // TH: tracks beginning of each row (i.e. first column).
                    initialAnchorRow=indexRow+1
                    initialAnchorCol=0

                }//if NOT yet last row.

            }//for indexRow.

            // TH: handles ONLY one particular case.
            // TH: submits shelter for construction.
            clearAllAndSubmit()
            break

        }//for map.

        Log.i(
            "test_UI_matrix_Internal_Optimized",
            "Ended."
        )

    }//test_UI_matrix_Internal_Optimized.

    private fun test_UI_matrix_Internal_Optimized_Setup_Twice(
        numRows: Int,
        numCols: Int,
        sortedDummies: List<ArrayList<Int>>,
        shelterOrderOfMagnitude: Int
    ) {

        Log.i(
            "test_UI_matrix_Internal_Optimized_Setup_Twice",
            "Started."
        )

        // TH: specifies matrix dimensions.
        val shelterBaseNumber  = numRows
        val rowTotal           = numRows
        val columnTotal        = numCols
        val matrix_2_by_2_dim  = rowTotal * columnTotal

        // TH: represents exact location based on coordinate.
        // TH: for 2-by-2 matrix (i.e. see formula below):
        // TH: 0 - (0,0) ; 1 - (0,1)
        // TH: 2 - (1,0) ; 3 - (1,1)
        val mapCoordinate = hashMapOf< Int, ArrayList<Int> >()

        for(rowIndex in 0 until rowTotal){

            for(columnIndex in 0 until columnTotal){

                // TH: maps coordinate to location.
                val coordinate=columnTotal*rowIndex+columnIndex
                val location  : ArrayList<Int> = arrayListOf()
                location.add(rowIndex)
                location.add(columnIndex)

                Log.i(
                    "test_UI_matrix_Internal_Optimized_Setup_Twice",
                    "coordinate=${coordinate} @ location=${location}"
                )

                // TH: initializes coordinates.
                mapCoordinate[coordinate]=location

            }//for columnIndex.

        }//for rowIndex.

        Log.i(
            "test_UI_matrix_Internal_Optimized_Setup_Twice",
            "--------------------------------------------------------"
        )

        Log.i(
            "test_UI_matrix_Internal_Optimized_Setup_Twice",
            "mapCoordinate.size=${mapCoordinate.size}"
        )

        for((key,value) in mapCoordinate){
            Log.i(
                "test_UI_matrix_Internal_Optimized_Setup_Twice-mapCoordinate",
                "key=${key} @ value=${value}"
            )
        }//for map.

        Log.i(
            "test_UI_matrix_Internal_Optimized_Setup_Twice",
            "--------------------------------------------------------"
        )

        // TH: populates data for matrix.
        val matrix_2_by_2_data : ArrayList<Int> = arrayListOf()
        for(index in 0 until matrix_2_by_2_dim){
            matrix_2_by_2_data.add(shelterBaseNumber*shelterOrderOfMagnitude+index)
        }//for index.

        for(index in 0 until matrix_2_by_2_data.size){
            Log.i(
                "test_UI_matrix_Internal_Optimized_Setup_Twice",
                "matrix_2_by_2_data[$index]=${matrix_2_by_2_data[index]}"
            )
        }//for index.

        // TH: represents what coordinates would go into matrix (i.e. per case).
        val mapCoordinateMatrix = hashMapOf< Int, ArrayList<ArrayList<Int>> >()

        // TH: represents what data would go into matrix (i.e. per case).
        val mapCoordinateMatrixData = hashMapOf< Int, ArrayList<Int> >()

        var totalBinaryString = ""
        for(rowIndex in 0 until numRows){
            var binaryPerRow = ""
            for(columnIndex in 0 until numCols){
                binaryPerRow += if(sortedDummies.contains(arrayListOf(rowIndex,columnIndex))){
                    "0"
                } else{
                    ShelterActivity.LAYOUT_DUMMY_ID_PRODUCT
                }
            }//for columnIndex.
            Log.i(
                "test_UI_matrix_Internal_Optimized_Setup_Twice",
                "binaryPerRow=${binaryPerRow}"
            )
            totalBinaryString+=binaryPerRow
        }//for rowIndex.

        Log.i(
            "test_UI_matrix_Internal_Optimized_Setup_Twice",
            "totalBinaryString.length=${totalBinaryString.length}"
        )

        Log.i(
            "test_UI_matrix_Internal_Optimized_Setup_Twice",
            "totalBinaryString=${totalBinaryString}"
        )

        val binaryArrayListForData: ArrayList<Int> = arrayListOf()
        val binaryArrayListForPosition: ArrayList<ArrayList<Int>> = arrayListOf()

        val eachBinaryLength = if(totalBinaryString.length==matrix_2_by_2_dim) matrix_2_by_2_dim else totalBinaryString.length

        for(eachBinaryIndex in 0 until eachBinaryLength){

            if(totalBinaryString[eachBinaryIndex].toString()==ShelterActivity.LAYOUT_DUMMY_ID_PRODUCT){

                /*
                Log.i(
                    "test_UI_matrix_Internal_Optimized_Setup_Twice",
                    "enabled ${matrix_2_by_2_data[eachBinaryIndex]} @ location=${mapCoordinate[eachBinaryIndex]}"
                )
                */

                binaryArrayListForData.add(matrix_2_by_2_data[eachBinaryIndex])
                mapCoordinate[eachBinaryIndex]?.let { binaryArrayListForPosition.add(it) }

            }//if '1' @ eachBinaryIndex.

        }//for eachBinaryIndex.

        mapCoordinateMatrixData[eachBinaryLength]=binaryArrayListForData
        mapCoordinateMatrix[eachBinaryLength]=binaryArrayListForPosition

        if(mapCoordinateMatrixData.size!=mapCoordinateMatrix.size){
            Log.i(
                "test_UI_matrix_Internal_Optimized_Setup_Twice",
                "Ended (Error, 0)."
            )
            exitProcess(1)
        }

        /*
        for((key,value) in mapCoordinateMatrix){
            val shelter = "$shelterBaseNumber-$key"
            Log.i(
                "test_UI_matrix_Internal_Optimized_Setup_Twice-mapCoordinateMatrix",
                "key=${key}: shelter=${shelter}, data=${mapCoordinateMatrixData[key]}, location=${value}"
            )
        }//for map.
        */

        // TH: initiates matrix construction.
        // TH: mapCoordinateMatrix: key==case, value==locations per case.
        // TH: mapCoordinateMatrixData:               points per case.
        for((key,value) in mapCoordinateMatrix){

            // TH: builds particular shelter, based on target key.
            if (key != eachBinaryLength) continue

            val shelter = "$shelterBaseNumber-$key"
            Log.i(
                "test_UI_matrix_Internal_Optimized_Setup_Twice-mapCoordinateMatrix",
                "key=${key}: shelter=${shelter}"
            )

            for(index in 0 until value.size){
                Log.i(
                    "test_UI_matrix_Internal_Optimized_Setup_Twice",
                    "mapCoordinateMatrixData[$key][$index]=${mapCoordinateMatrixData[key]?.get(index)} @ ${value[index]}."
                )
            }//for index.

            Log.i(
                "test_UI_matrix_Internal_Optimized_Setup_Twice",
                "Case=${key} Active."
            )

            Espresso
                .onView(
                    withId(
                        R.id.shelter
                    )
                )
                .perform(
                    typeText(
                        shelter
                    )
                )
                .check(
                    matches(
                        withText(
                            shelter
                        )
                    )
                )

            Espresso
                .onView(
                    withId(
                        R.id.button_submit
                    )
                )
                .perform(
                    click()
                )

            Espresso
                .onView(
                    withId(
                        R.id.activity_shelter_split_30
                    )
                )
                .check(
                    matches(
                        isDisplayed()
                    )
                )

            Espresso
                .onView(
                    allOf(
                        withId(
                            R.id.button_setup
                        ),
                        withText(
                            "@string/button_setup_text"
                        ),
                        isDisplayed()
                    )
                )

            Espresso
                .onView(
                    withId(
                        R.id.button_setup
                    )
                )
                .perform(
                    scrollTo(),
                    click()
                )

            Espresso
                .onView(
                    allOf(
                        withId(
                            R.id.button_setup
                        ),
                        withText(
                            "@string/button_setup_text"
                        ),
                        isDisplayed()
                    )
                )

            Espresso
                .onView(
                    withId(
                        R.id.button_setup
                    )
                )
                .perform(
                    scrollTo(),
                    click()
                )

            // TH: toggles ADD mode.
            Espresso
                .onView(
                    allOf(
                        withId(
                            R.id.button_mode_add
                        ),
                        withText(
                            "@string/button_mode_add_text"
                        ),
                        isDisplayed()
                    )
                )

            Espresso
                .onView(
                    allOf(
                        withId(
                            R.id.button_mode_add
                        )
                    )
                )
                .perform(
                    scrollTo(),
                    click()
                )

            var initialAnchorRow=0
            var initialAnchorCol=0
            selectRecyclerViewMatrixAnchor(ShelterActivity.LAYOUT_DUMMY_ID_PRODUCT,initialAnchorRow,initialAnchorCol)

            // TH: sorts arrayList.
            val valueSorted= value.sortedWith(compareBy({ it[0] }, { it[1] }))

            // TH: tracks matrix data.
            var indexMatrixData=0

            for (indexRow in 0 until rowTotal) {

                for(indexColumn in 0 until columnTotal){

                    val coordinate = arrayListOf(indexRow,indexColumn)

                    // TH: indicates real value or dummy, per coordinate.
                    var recentlyAdded=false

                    if(
                        indexRow   ==initialAnchorRow &&
                        indexColumn==initialAnchorCol
                    ){

                        // TH: does NOTHING, as already added below.
                        if(!valueSorted.contains(coordinate)){

                            // TH: experimental.
                            recentlyAdded=false

                        }//if NOT in value.
                        else{

                            // TH: selects particular anchor inside matrix.
                            addProductAtGivenLocationInsideMatrixOptimized(
                                mapCoordinateMatrixData[key]?.get(indexMatrixData),
                                ShelterActivity.LAYOUT_DUMMY_ID_PRODUCT,
                                indexRow,
                                indexColumn
                            )

                            clearAll()

                            // TH: toggles ADD mode.
                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.button_mode_add
                                        ),
                                        withText(
                                            "@string/button_mode_add_text"
                                        ),
                                        isDisplayed()
                                    )
                                )

                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.button_mode_add
                                        )
                                    )
                                )
                                .perform(
                                    scrollTo(),
                                    click()
                                )

                            selectRecyclerViewMatrixAnchor(
                                mapCoordinateMatrixData[key]?.get(indexMatrixData).toString(),
                                indexRow,
                                indexColumn
                            )

                            recentlyAdded=true

                        }//else in value.

                    }//if initialAnchor.
                    else{

                        if(!valueSorted.contains(coordinate)){

                            // TH: addRight(1) to indexColumn-1.
                            // TH: puts EditText in focus.
                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.productIdAdd
                                        ),
                                        withHint(
                                            "@string/productIdAdd_hint"
                                        ),
                                        isDisplayed()
                                    )
                                )

                            // TH: types text in EditText.
                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.productIdAdd
                                        )
                                    )
                                )
                                .perform(
                                    scrollTo(),
                                    click(),
                                    typeText(
                                        ShelterActivity.LAYOUT_DUMMY_ID_PRODUCT
                                    ),
                                    closeSoftKeyboard()
                                )

                            // TH: issues insert-right to that productId, see above.
                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.button_addRIGHT_productId
                                        ),
                                        withText(
                                            "@string/button_addRIGHT_text"
                                        ),
                                        isDisplayed()
                                    )
                                )

                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.button_addRIGHT_productId
                                        )
                                    )
                                )
                                .perform(
                                    scrollTo(),
                                    click()
                                )

                            // TH: experimental.
                            recentlyAdded=false

                        }//if NOT in value.
                        else{

                            // TH: addRight(MatrixData) to indexColumn-1.
                            // TH: puts EditText in focus.
                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.productIdAdd
                                        ),
                                        withHint(
                                            "@string/productIdAdd_hint"
                                        ),
                                        isDisplayed()
                                    )
                                )

                            // TH: types text in EditText.
                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.productIdAdd
                                        )
                                    )
                                )
                                .perform(
                                    scrollTo(),
                                    click(),
                                    typeText(
                                        mapCoordinateMatrixData[key]?.get(indexMatrixData).toString()
                                    ),
                                    closeSoftKeyboard()
                                )

                            // TH: issues insert-right to that productId, see above.
                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.button_addRIGHT_productId
                                        ),
                                        withText(
                                            "@string/button_addRIGHT_text"
                                        ),
                                        isDisplayed()
                                    )
                                )

                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.button_addRIGHT_productId
                                        )
                                    )
                                )
                                .perform(
                                    scrollTo(),
                                    click()
                                )

                            recentlyAdded=true

                        }//else in value.

                    }//else !initialAnchor.

                    // TH: increments index only if already add MatrixData or,
                    if(recentlyAdded){
                        // TH: updates matrix data index.
                        indexMatrixData+=1
                    }

                }//for indexColumn.

                if(indexRow!=rowTotal-1){

                    Log.i(
                        "test_UI_matrix_Internal_Optimized_Setup_Twice",
                        "indexRow=${indexRow} != ${rowTotal-1}"
                    )

                    Espresso
                        .onView(
                            allOf(
                                withId(
                                    R.id.button_add_below
                                ),
                                withText(
                                    "@string/button_add_below_text"
                                ),
                                isDisplayed()
                            )
                        )

                    Espresso
                        .onView(
                            allOf(
                                withId(
                                    R.id.button_add_below
                                )
                            )
                        )
                        .perform(
                            scrollTo(),
                            click()
                        )

                    Log.i(
                        "test_UI_matrix_Internal_Optimized_Setup_Twice",
                        "button_add_below clicked."
                    )

                    // TH: tracks beginning of each row (i.e. first column).
                    initialAnchorRow=indexRow+1
                    initialAnchorCol=0

                }//if NOT yet last row.

            }//for indexRow.

            break

        }//for map.

        clearAll()

        // TH: initiates matrix construction.
        // TH: mapCoordinateMatrix: key==case, value==locations per case.
        // TH: mapCoordinateMatrixData:               points per case.
        for((key,value) in mapCoordinateMatrix){

            // TH: builds particular shelter, based on target key.
            if (key != eachBinaryLength) continue

            val shelter = "$shelterBaseNumber-$key"
            Log.i(
                "test_UI_matrix_Internal_Optimized_Setup_Twice-mapCoordinateMatrix",
                "key=${key}: shelter=${shelter}"
            )

            for(index in 0 until value.size){
                Log.i(
                    "test_UI_matrix_Internal_Optimized_Setup_Twice",
                    "mapCoordinateMatrixData[$key][$index]=${mapCoordinateMatrixData[key]?.get(index)} @ ${value[index]}."
                )
            }//for index.

            Log.i(
                "test_UI_matrix_Internal_Optimized_Setup_Twice",
                "Case=${key} Active."
            )

            Espresso
                .onView(
                    withId(
                        R.id.activity_shelter_split_30
                    )
                )
                .check(
                    matches(
                        isDisplayed()
                    )
                )

            Espresso
                .onView(
                    allOf(
                        withId(
                            R.id.button_setup
                        ),
                        withText(
                            "@string/button_setup_text"
                        ),
                        isDisplayed()
                    )
                )

            Espresso
                .onView(
                    withId(
                        R.id.button_setup
                    )
                )
                .perform(
                    scrollTo(),
                    click()
                )

            Espresso
                .onView(
                    allOf(
                        withId(
                            R.id.button_setup
                        ),
                        withText(
                            "@string/button_setup_text"
                        ),
                        isDisplayed()
                    )
                )

            Espresso
                .onView(
                    withId(
                        R.id.button_setup
                    )
                )
                .perform(
                    scrollTo(),
                    click()
                )

            // TH: toggles ADD mode.
            Espresso
                .onView(
                    allOf(
                        withId(
                            R.id.button_mode_add
                        ),
                        withText(
                            "@string/button_mode_add_text"
                        ),
                        isDisplayed()
                    )
                )

            Espresso
                .onView(
                    allOf(
                        withId(
                            R.id.button_mode_add
                        )
                    )
                )
                .perform(
                    scrollTo(),
                    click()
                )

            var initialAnchorRow=0
            var initialAnchorCol=0
            selectRecyclerViewMatrixAnchor(ShelterActivity.LAYOUT_DUMMY_ID_PRODUCT,initialAnchorRow,initialAnchorCol)

            // TH: sorts arrayList.
            val valueSorted= value.sortedWith(compareBy({ it[0] }, { it[1] }))

            // TH: tracks matrix data.
            var indexMatrixData=0

            for (indexRow in 0 until rowTotal) {

                for(indexColumn in 0 until columnTotal){

                    val coordinate = arrayListOf(indexRow,indexColumn)

                    // TH: indicates real value or dummy, per coordinate.
                    var recentlyAdded=false

                    if(
                        indexRow   ==initialAnchorRow &&
                        indexColumn==initialAnchorCol
                    ){

                        // TH: does NOTHING, as already added below.
                        if(!valueSorted.contains(coordinate)){

                            // TH: experimental.
                            recentlyAdded=false

                        }//if NOT in value.
                        else{

                            // TH: selects particular anchor inside matrix.
                            addProductAtGivenLocationInsideMatrixOptimized(
                                mapCoordinateMatrixData[key]?.get(indexMatrixData),
                                ShelterActivity.LAYOUT_DUMMY_ID_PRODUCT,
                                indexRow,
                                indexColumn
                            )

                            clearAll()

                            // TH: toggles ADD mode.
                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.button_mode_add
                                        ),
                                        withText(
                                            "@string/button_mode_add_text"
                                        ),
                                        isDisplayed()
                                    )
                                )

                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.button_mode_add
                                        )
                                    )
                                )
                                .perform(
                                    scrollTo(),
                                    click()
                                )

                            selectRecyclerViewMatrixAnchor(
                                mapCoordinateMatrixData[key]?.get(indexMatrixData).toString(),
                                indexRow,
                                indexColumn
                            )

                            recentlyAdded=true

                        }//else in value.

                    }//if initialAnchor.
                    else{

                        if(!valueSorted.contains(coordinate)){

                            // TH: addRight(1) to indexColumn-1.
                            // TH: puts EditText in focus.
                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.productIdAdd
                                        ),
                                        withHint(
                                            "@string/productIdAdd_hint"
                                        ),
                                        isDisplayed()
                                    )
                                )

                            // TH: types text in EditText.
                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.productIdAdd
                                        )
                                    )
                                )
                                .perform(
                                    scrollTo(),
                                    click(),
                                    typeText(
                                        ShelterActivity.LAYOUT_DUMMY_ID_PRODUCT
                                    ),
                                    closeSoftKeyboard()
                                )

                            // TH: issues insert-right to that productId, see above.
                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.button_addRIGHT_productId
                                        ),
                                        withText(
                                            "@string/button_addRIGHT_text"
                                        ),
                                        isDisplayed()
                                    )
                                )

                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.button_addRIGHT_productId
                                        )
                                    )
                                )
                                .perform(
                                    scrollTo(),
                                    click()
                                )

                            // TH: experimental.
                            recentlyAdded=false

                        }//if NOT in value.
                        else{

                            // TH: addRight(MatrixData) to indexColumn-1.
                            // TH: puts EditText in focus.
                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.productIdAdd
                                        ),
                                        withHint(
                                            "@string/productIdAdd_hint"
                                        ),
                                        isDisplayed()
                                    )
                                )

                            // TH: types text in EditText.
                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.productIdAdd
                                        )
                                    )
                                )
                                .perform(
                                    scrollTo(),
                                    click(),
                                    typeText(
                                        mapCoordinateMatrixData[key]?.get(indexMatrixData).toString()
                                    ),
                                    closeSoftKeyboard()
                                )

                            // TH: issues insert-right to that productId, see above.
                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.button_addRIGHT_productId
                                        ),
                                        withText(
                                            "@string/button_addRIGHT_text"
                                        ),
                                        isDisplayed()
                                    )
                                )

                            Espresso
                                .onView(
                                    allOf(
                                        withId(
                                            R.id.button_addRIGHT_productId
                                        )
                                    )
                                )
                                .perform(
                                    scrollTo(),
                                    click()
                                )

                            recentlyAdded=true

                        }//else in value.

                    }//else !initialAnchor.

                    // TH: increments index only if already add MatrixData or,
                    if(recentlyAdded){
                        // TH: updates matrix data index.
                        indexMatrixData+=1
                    }

                }//for indexColumn.

                if(indexRow!=rowTotal-1){

                    Log.i(
                        "test_UI_matrix_Internal_Optimized_Setup_Twice",
                        "indexRow=${indexRow} != ${rowTotal-1}"
                    )

                    Espresso
                        .onView(
                            allOf(
                                withId(
                                    R.id.button_add_below
                                ),
                                withText(
                                    "@string/button_add_below_text"
                                ),
                                isDisplayed()
                            )
                        )

                    Espresso
                        .onView(
                            allOf(
                                withId(
                                    R.id.button_add_below
                                )
                            )
                        )
                        .perform(
                            scrollTo(),
                            click()
                        )

                    Log.i(
                        "test_UI_matrix_Internal_Optimized_Setup_Twice",
                        "button_add_below clicked."
                    )

                    // TH: tracks beginning of each row (i.e. first column).
                    initialAnchorRow=indexRow+1
                    initialAnchorCol=0

                }//if NOT yet last row.

            }//for indexRow.

            break

        }//for map.

        // TH: handles ONLY one particular case.
        // TH: submits shelter for construction.
        clearAllAndSubmit()

        Log.i(
            "test_UI_matrix_Internal_Optimized_Setup_Twice",
            "Ended."
        )

    }//test_UI_matrix_Internal_Optimized_Setup_Twice.

    //----------------------------------------------------------------------------------------------------------------

    private fun addProductAtGivenLocationInsideMatrixOptimized(
        data: Int?,
        anchor: String,
        row: Int,
        column: Int
    ) {

        selectRecyclerViewMatrixAnchor(anchor,row,column)

        // TH: puts EditText in focus.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.productIdAdd
                    ),
                    withHint(
                        "@string/productIdAdd_hint"
                    ),
                    isDisplayed()
                )
            )

        // TH: types text in EditText.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.productIdAdd
                    )
                )
            )
            .perform(
                scrollTo(),
                click(),
                typeText(
                    data.toString()
                ),
                closeSoftKeyboard()
            )

        // TH: issues insert-right to that productId, see above.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_addRIGHT_productId
                    ),
                    withText(
                        "@string/button_addRIGHT_text"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_addRIGHT_productId
                    )
                )
            )
            .perform(
                scrollTo(),
                click()
            )

        // TH: toggles ADD mode.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_mode_add
                    ),
                    withText(
                        "@string/button_mode_add_text"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_mode_add
                    )
                )
            )
            .perform(
                scrollTo(),
                click()
            )

        // TH: toggles DELETE mode.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_mode_delete
                    ),
                    withText(
                        "@string/button_mode_delete_text"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_mode_delete
                    )
                )
            )
            .perform(
                scrollTo(),
                click()
            )

        issueDeleteSingleMatrix(row,column,anchor)

        Log.i(
            "test_UI_matrix_Internal_Optimized-addProductAtGivenLocationInsideMatrixOptimized",
            "data=${data} added at [$row,$column]."
        )

    }//addProductAtGivenLocationInsideMatrixOptimized.

    //----------------------------------------------------------------------------------------------------------------

    private fun addProductAtGivenLocationInsideMatrix(
        data: Int?,
        anchor: String,
        row: Int,
        column: Int
    ) {

        clearAll()

        // TH: toggles ADD mode.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_mode_add
                    ),
                    withText(
                        "@string/button_mode_add_text"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_mode_add
                    )
                )
            )
            .perform(
                scrollTo(),
                click()
            )

        selectRecyclerViewMatrixAnchor(anchor,row,column)

        // TH: puts EditText in focus.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.productIdAdd
                    ),
                    withHint(
                        "@string/productIdAdd_hint"
                    ),
                    isDisplayed()
                )
            )

        // TH: types text in EditText.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.productIdAdd
                    )
                )
            )
            .perform(
                scrollTo(),
                click(),
                typeText(
                    data.toString()
                ),
                closeSoftKeyboard()
            )

        // TH: issues insert-right to that productId, see above.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_addRIGHT_productId
                    ),
                    withText(
                        "@string/button_addRIGHT_text"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_addRIGHT_productId
                    )
                )
            )
            .perform(
                scrollTo(),
                click()
            )

        // TH: toggles ADD mode.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_mode_add
                    ),
                    withText(
                        "@string/button_mode_add_text"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_mode_add
                    )
                )
            )
            .perform(
                scrollTo(),
                click()
            )

        // TH: toggles DELETE mode.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_mode_delete
                    ),
                    withText(
                        "@string/button_mode_delete_text"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_mode_delete
                    )
                )
            )
            .perform(
                scrollTo(),
                click()
            )

        issueDeleteSingleMatrix(row,column,anchor)

        Log.i(
            "test_UI_matrix_Internal_Experimental-addProductAtGivenLocationInsideMatrix",
            "data=${data} added at [$row,$column]."
        )

    }//addProductAtGivenLocationInsideMatrix.

    private fun delayMainThread(waitTime: Long) {
        /*
        val startTime = System.currentTimeMillis()
        var elapsed   = System.currentTimeMillis() - startTime
        while(elapsed<waitTime){
            elapsed = System.currentTimeMillis() - startTime
        }
        */
    }//delayMainThread.

    private fun selectRecyclerViewMatrixAnchor(
        anchor: String,
        row: Int,
        column: Int
    ) {

        // TH: recommends anchor items in RecyclerView (i.e. EditText).
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.productIdAnchor
                    ),
                    withHint(
                        "@string/productIdAnchor_hint"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.productIdAnchor
                    )
                )
            )
            .perform(
                scrollTo(),
                click(),
                typeText(
                    anchor
                ),
                closeSoftKeyboard()
            )

        // TH: recommends anchor items in RecyclerView (i.e. Button).
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_anchor_productId
                    ),
                    withText(
                        "@string/button_anchor_text"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_anchor_productId
                    )
                )
            )
            .perform(
                scrollTo(),
                click()
            )

        selectRecyclerViewMatrix(row,column)

    }//selectRecyclerViewMatrixAnchor.

    private fun clearAll() {

        // TH: represents wait for re-try.
        delayMainThread(delayThreadMain.toLong())

        // TH: experimental.
        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_clear
                    ),
                    withText(
                        "@string/button_clear_text"
                    ),
                    isDisplayed()
                )
            )

        Espresso
            .onView(
                allOf(
                    withId(
                        R.id.button_clear
                    )
                )
            )
            .perform(
                scrollTo(),
                click()
            )

    }//clearAll.

}//MainActivityTest.