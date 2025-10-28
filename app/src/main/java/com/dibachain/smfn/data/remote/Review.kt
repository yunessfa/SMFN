//package com.dibachain.smfn.data.remote
//
//
//import retrofit2.http.Header
//import retrofit2.http.Multipart
//import retrofit2.http.POST
//import retrofit2.http.Part
//
//
//interface ReviewApi {
//    @Multipart
//    @POST("/api/v1/app/review/create")
//    suspend fun addReview(
//        @Header("token") token: String,
//        @Part("rating") rating: String,
//        @Part("item") item: String,
//        @Part("comment") comment: String,
//    ): ReviewRes
//}
