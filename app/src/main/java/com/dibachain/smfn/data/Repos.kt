// data/Repos.kt
package com.dibachain.smfn.data

import android.app.Application
import android.content.Context
import com.dibachain.smfn.data.remote.AuthApi
import com.dibachain.smfn.data.remote.FavoriteApi
import com.dibachain.smfn.data.remote.NetworkModule

object Repos {

    // --- init once at app start ---
    private lateinit var appCtx: Context
    fun init(app: Application) { appCtx = app.applicationContext }

    // --- Auth / Profile ---
    val authRepository: AuthRepository by lazy {
        val api = NetworkModule.retrofit.create(AuthApi::class.java)
        AuthRepository(api, moshi = NetworkModule.moshi)
    }

    val profileRepository: ProfileRepository by lazy {
        ProfileRepository(
            authApi = NetworkModule.retrofit.create(AuthApi::class.java),
            profileApi = NetworkModule.profileApi
        )
    }

    // --- Offers / Inventory / Category ---
    val offersRepository: OffersRepository by lazy {
        OffersRepository(
            api = NetworkModule.retrofit.create(
                com.dibachain.smfn.data.remote.OffersApi::class.java
            ),
            moshi = NetworkModule.moshi
        )
    }

    val inventoryRepository: InventoryRepository by lazy {
        InventoryRepository(
            api = NetworkModule.retrofit.create(
                com.dibachain.smfn.data.remote.UserItemsApi::class.java
            )
        )
    }
    // در object Repos:
    val collectionRepository: CollectionRepository by lazy {
        CollectionRepository(
            api = NetworkModule.retrofit.create(
                com.dibachain.smfn.data.remote.CollectionApi::class.java
            )
        )
    }
    val followRepository: FollowRepository by lazy {
        FollowRepository(NetworkModule.followApi)
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(NetworkModule.categoryApi)
    }

    val chatRepository: ChatRepository by lazy {
        ChatRepository(
            chatApi = NetworkModule.chatApi,
            messageApi = NetworkModule.messageApi
        )
    }

    // --- Item detail / reviews ---
    val itemDetailRepository: ItemDetailRepository by lazy {
        ItemDetailRepository(
            itemsSingleApi = NetworkModule.itemsSingleApi,
            reviewApi = NetworkModule.reviewApi
        )
    }

    // --- Favorites ---
    val favoriteRepository: FavoriteRepository by lazy {
        // اگر favoriteApi در NetworkModule داری:
        FavoriteRepository(NetworkModule.favoriteApi)
        // اگر نداری، این را جایگزین کن:
        // FavoriteRepository(NetworkModule.retrofit.create(FavoriteApi::class.java))
    }

    // --- Location ---
    val locationRepository: LocationRepository by lazy {
        LocationRepository(NetworkModule.locationApi)
    }

    // --- Item Create (needs Context) ---
    val itemCreateRepository: ItemCreateRepository by lazy {
        check(::appCtx.isInitialized) {
            "Repos.init(app) must be called before using itemCreateRepository."
        }
        ItemCreateRepository(
            api = NetworkModule.itemsCreateApi,
            appContext = appCtx
        )
    }
}
