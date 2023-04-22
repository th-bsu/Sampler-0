package com.th.chapter_11.api

data class Recommendation (
    val recommendationId: Long
    ,val author: String
    ,val row: Int
    ,val offset: Int
    ,val shelter: String
    ,val productId: Long
    , val url: String
)
