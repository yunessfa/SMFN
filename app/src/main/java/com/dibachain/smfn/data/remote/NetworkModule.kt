package com.dibachain.smfn.data.remote

import com.dibachain.smfn.core.Public
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object NetworkModule {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    // data/remote/NetworkModule.kt
    val categoryApi: CategoryApi by lazy {
        retrofit.create(CategoryApi::class.java)
    }
    val itemsApiApi: ItemsApi by lazy {
        retrofit.create(ItemsApi::class.java)
    }
    val favoriteApi: FavoriteApi by lazy {
        retrofit.create(FavoriteApi::class.java)
    }
    val itemsSingleApi: ItemsSingelApi by lazy {
        // ✅ اگر دوست داری صددرصد مطمئن باشی، از نام کامل پکیج هم می‌تونی استفاده کنی
        retrofit.create(com.dibachain.smfn.data.remote.ItemsSingelApi::class.java)
    }// data/remote/NetworkModule.kt
    val locationApi: LocationApi by lazy {
        retrofit.create(LocationApi::class.java)
    }

    val reviewApi: ReviewApi by lazy {
        retrofit.create(ReviewApi::class.java)
    }
    val itemsCreateApi: ItemsCreateApi by lazy {
        retrofit.create(ItemsCreateApi::class.java)
    }
    val profileApi: ProfileApi by lazy {
        retrofit.create(ProfileApi::class.java)
    }
    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())   // 👈 اضافه شد
        .build()
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(Public.BASE_URL)
        .client(okHttp)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
}
