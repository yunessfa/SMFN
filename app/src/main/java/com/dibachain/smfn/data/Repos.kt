// data/Repos.kt
package com.dibachain.smfn.data

import android.app.Application
import android.content.Context
import com.dibachain.smfn.data.remote.NetworkModule
import com.dibachain.smfn.data.remote.AuthApi

object Repos {
    // --- init once at app start ---
    private lateinit var appCtx: Context
    fun init(app: Application) { appCtx = app.applicationContext }

    val authRepository by lazy {
        val api = NetworkModule.retrofit.create(AuthApi::class.java)
        AuthRepository(api, moshi = NetworkModule.moshi)
    }
    val profileRepository by lazy {
        ProfileRepository(
            authApi = NetworkModule.retrofit.create(com.dibachain.smfn.data.remote.AuthApi::class.java),
            profileApi = NetworkModule.profileApi
        )
    }
    // data/Repos.kt
    val offersRepository by lazy {
        OffersRepository(
            api = NetworkModule.retrofit.create(com.dibachain.smfn.data.remote.OffersApi::class.java),
            moshi = NetworkModule.moshi
        )
    }
    val inventoryRepository by lazy {
        InventoryRepository(
            api = NetworkModule.retrofit.create(com.dibachain.smfn.data.remote.UserItemsApi::class.java)
        )
    }

    val categoryRepository by lazy { CategoryRepository(NetworkModule.categoryApi) }

    val itemDetailRepository by lazy {
        ItemDetailRepository(NetworkModule.itemsSingleApi, NetworkModule.reviewApi)
    }
    val locationRepository by lazy {
        LocationRepository(NetworkModule.locationApi)
    }
    val itemCreateRepository by lazy {
        // ✅ دیگه خطای App.instance نمیدی
        ItemCreateRepository(
            api = NetworkModule.itemsCreateApi,
            appContext = appCtx
        )
    }
}
