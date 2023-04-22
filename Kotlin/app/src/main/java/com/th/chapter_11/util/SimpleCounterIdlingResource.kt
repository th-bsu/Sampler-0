package com.th.chapter_11.util

import android.util.Log
import androidx.test.espresso.IdlingResource
import java.util.concurrent.atomic.AtomicInteger

class SimpleCounterIdlingResource(
    inputName: String,
): IdlingResource {

    private val mResourceName: String = inputName

    private var counter = AtomicInteger(0)

    private var resourceCallback: IdlingResource.ResourceCallback? = null

    override fun getName(): String = mResourceName

    override fun isIdleNow(): Boolean {
        Log.i(
            name,
            "isIdleNow: counter=${counter.get()}"
        )
        return counter.get()==0
    }

    override fun registerIdleTransitionCallback(
        callback: IdlingResource.ResourceCallback?
    ) {
        if (callback != null) {
            resourceCallback = callback
        }
    }//registerIdleTransitionCallback.

    fun increment(){
        counter.getAndIncrement()

        Log.i(
            name,
            "increment: counterVal=${counter.get()}"
        )

    }//increment.

    fun decrement(){
        val counterVal = counter.decrementAndGet()

        Log.i(
            name,
            "decrement: counterVal=${counterVal}"
        )

        if(counterVal==0){
            resourceCallback?.onTransitionToIdle()
        }
        if(counterVal<0){
            throw IllegalArgumentException(
                "SimpleCounterIdlingResource:decrement."
            )
        }//if <0.

    }//decrement.

}//SimpleCounterIdlingResource.
