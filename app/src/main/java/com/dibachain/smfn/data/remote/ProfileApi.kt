// data/remote/ProfileApi.kt
package com.dibachain.smfn.data.remote

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface ProfileApi {
    @GET("api/v1/app/user/profile/get-id/{id}")
    suspend fun getById(
        @Header("token") token: String,
        @Path("id") id: String
    ): ProfileSelfRes
}
