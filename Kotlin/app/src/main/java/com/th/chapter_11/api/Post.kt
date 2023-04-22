package com.th.chapter_11.api

import com.google.gson.annotations.SerializedName

// TH: parses and holds on to json data.
// TH: http://192.168.0.11:8080/product-composite-by-query-shelter?shelter=22-01
// TH: matches interface (i.e. ProductCompositeService) in backend.
data class Post(
    @SerializedName("productId") val productId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("amount") val amount: Float,
    @SerializedName("value") val value: Float,
    @SerializedName("recommendations") val recommendations: List<Recommendation>,
    @SerializedName("reviews") val reviews: List<Review>,
    @SerializedName("serviceAddresses") val serviceAddresses: ServiceAddress
)
