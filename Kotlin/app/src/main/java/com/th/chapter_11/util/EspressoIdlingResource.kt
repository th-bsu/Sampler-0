package com.th.chapter_11.util

object EspressoIdlingResource {

    private const val RESOURCE: String = "GLOBAL"

    private val counterIdler: SimpleCounterIdlingResource = SimpleCounterIdlingResource(RESOURCE)

    fun increment(){
        counterIdler.increment()
    }//increment.

    fun decrement(){
        counterIdler.decrement()
    }//decrement.

    fun getCounterIdler() = counterIdler

}//EspressoIdlingResource.
