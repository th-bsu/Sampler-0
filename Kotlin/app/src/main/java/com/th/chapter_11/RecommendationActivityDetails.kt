package com.th.chapter_11

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.th.chapter_11.db.RecommendationEntity

class RecommendationActivityDetails : AppCompatActivity(){

    companion object {
        const val DETAIL_RECOMMENDATION = "recommendation"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommendation_details)

        val textDetails: TextView = findViewById(R.id.details_text_view)

        //val rec = intent.getParcelableExtra<RecommendationEntity>(DETAIL_RECOMMENDATION)
        val rec = intent.getSerializableExtra(DETAIL_RECOMMENDATION) as RecommendationEntity
        rec.run {
            textDetails.text = productId.toString()
        }
    }

}