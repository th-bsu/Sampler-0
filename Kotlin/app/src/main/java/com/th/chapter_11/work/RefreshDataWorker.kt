package com.th.chapter_11.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

// TH: https://developer.android.com/codelabs/kotlin-android-training-work-manager?index=..%2F..android-kotlin-fundamentals#6

class RefreshDataWorker (appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params){

    companion object {
        const val WORK_NAME = "RefreshDataWorker"
    }

    // TH: helps execute long-running operation in background thread, without blocking main thread.
    override suspend fun doWork(): Result {

        Log.i(
            WORK_NAME,
            "doWork ..."
        )
        return Result.success()

    }//doWork.

}
