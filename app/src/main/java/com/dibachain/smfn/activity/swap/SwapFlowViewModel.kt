package com.dibachain.smfn.activity.swap

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.dibachain.smfn.BuildConfig
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.AuthRepository
import com.dibachain.smfn.data.InventoryRepository
import com.dibachain.smfn.data.OffersRepository
import com.dibachain.smfn.data.remote.ProfileSelfData
import com.dibachain.smfn.data.remote.UserItemDto
import kotlinx.coroutines.launch

data class SwapOther( // طرف مقابل (از دیتیل آگهی)
    val userId: String,
    val userName: String?,
    val userAvatarPath: String?,     // مسیر نسبی (link) – با BASE_URL_IMAGE بچسبان
    val itemId: String,
    val itemImagePath: String?       // اگر داری
)

data class SwapUi(
    val loading: Boolean = false,
    val error: String? = null,
    val me: ProfileSelfData? = null,

    val other: SwapOther? = null,

    val myInventory: List<UserItemDto> = emptyList(),
    val mySelectedItemId: String? = null,

    val requestInFlight: Boolean = false,
    val requestDone: Boolean = false
)

class SwapFlowViewModel(
    private val app: Application,
    private val authRepo: AuthRepository,
    private val inventoryRepo: InventoryRepository,
    private val offersRepo: OffersRepository,
    private val tokenProvider: suspend () -> String
) : AndroidViewModel(app) {

    var ui by mutableStateOf(SwapUi())
        private set

    // step-0: از دیتیل آگهی مقدار طرف مقابل را ست کن
    fun setOther(other: SwapOther) {
        ui = ui.copy(other = other)
    }

    // step-1: لود پروفایل خودم
    fun loadMe() = viewModelScope.launch {
        ui = ui.copy(loading = true, error = null)
        val token = tokenProvider()
        when (val r = authRepo.getSelf(token)) {
            is Result.Success -> ui = ui.copy(me = r.data, loading = false)
            is Result.Error   -> ui = ui.copy(error = r.message, loading = false)
        }
    }

    // step-2: لود اینونتوری خودم
    fun loadMyInventory() = viewModelScope.launch {
        val token = tokenProvider()
        val myId = ui.me?._id ?: return@launch
        ui = ui.copy(loading = true, error = null)
        when (val r = inventoryRepo.getUserItems(token, myId)) {
            is Result.Success -> ui = ui.copy(myInventory = r.data, loading = false)
            is Result.Error   -> ui = ui.copy(error = r.message, loading = false)
        }
    }

    fun selectMyItem(itemId: String) {
        ui = ui.copy(mySelectedItemId = itemId)
    }

    // step-3: ارسال درخواست سواپ
    fun sendOffer(onSuccess: () -> Unit = {}) = viewModelScope.launch {
        val token = tokenProvider()
        val other = ui.other ?: run {
            ui = ui.copy(error = "Invalid target")
            return@launch
        }
        val offered = ui.mySelectedItemId ?: run {
            ui = ui.copy(error = "Select an item first")
            return@launch
        }

        ui = ui.copy(requestInFlight = true, error = null)
        when (val r = offersRepo.addOffer(
            token = token,
            toUser = other.userId,
            itemOffered = offered,
            itemRequested = other.itemId
        )) {
            is Result.Success -> {
                ui = ui.copy(requestInFlight = false, requestDone = true)
                onSuccess()
            }
            is Result.Error -> {
                ui = ui.copy(requestInFlight = false, error = r.message)
            }
        }
    }

    // helper: ساخت URL کامل تصویر
    fun imgUrl(path: String?): String? =
        path?.let {
            if (it.startsWith("http")) it else "${BuildConfig.BASE_URL_IMAGE}$it"
        }
}
