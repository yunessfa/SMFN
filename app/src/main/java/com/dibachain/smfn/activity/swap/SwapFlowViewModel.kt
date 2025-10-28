package com.dibachain.smfn.activity.swap

import android.app.Application
import android.provider.SyncStateContract.Helpers.update
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.dibachain.smfn.BuildConfig
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.core.Public
import com.dibachain.smfn.data.AuthRepository
import com.dibachain.smfn.data.InventoryRepository
import com.dibachain.smfn.data.OffersRepository
import com.dibachain.smfn.data.remote.ProfileSelfData
import com.dibachain.smfn.data.remote.UserItemDto
import kotlinx.coroutines.launch

// activity/swap/SwapFlowViewModel.kt (یا فایل مدل سواپ)
data class SwapOther(
    val userId: String,
    val userName: String,
    val userAvatarPath: String?,
    val itemId: String,
    val itemImagePath: String?,
    // 👇 فیلدهای جدید برای نمایش اطلاعات آیتم
    val itemTitle: String? = null,
    val itemValueText: String? = null,
    val itemConditionTitle: String? = null,
    val itemLocationText: String? = null
)


data class SwapUi(
    val loading: Boolean = false,
    val error: String? = null,
    val me: ProfileSelfData? = null,
    val other: SwapOther? = null,

    val myInventory: List<UserItemDto> = emptyList(),
    val mySelectedItemId: String? = null,

    // 👇 اضافه‌ها برای سناریوی "آفر موجود"
    val myOfferItemId: String? = null,
    val myOfferItemImagePath: String? = null,

    val requestInFlight: Boolean = false,
    val requestDone: Boolean = false,
    val offerStatus: String? = null,
    val isSender: Boolean? = null
)

enum class offerStatus(val api: String) {
    ACCEPTED("accepted"),
    REJECTED("rejected")
}
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
    // SwapFlowViewModel.kt
    suspend fun debugLine(): String {
        val token = runCatching { tokenProvider() }.getOrNull().orEmpty()
        val t = token.take(6)
        val other = ui.other
        val offered = ui.mySelectedItemId
        return "token=${t}..., toUser=${other?.userId}, itemOffered=$offered, itemRequested=${other?.itemId}"
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
        val myId = ui.me?.user?._id ?: return@launch
        ui = ui.copy(loading = true, error = null)
        when (val r = inventoryRepo.getUserItems(token, myId)) {
            is Result.Success -> ui = ui.copy(myInventory = r.data, loading = false)
            is Result.Error   -> ui = ui.copy(error = r.message, loading = false)
        }
    }

    fun selectMyItem(itemId: String) {
        ui = ui.copy(mySelectedItemId = itemId)
    }

    fun sendOffer(onSuccess: () -> Unit = {}) = viewModelScope.launch {
        val token = tokenProvider()
        val other = ui.other
        val offered = ui.mySelectedItemId
        android.util.Log.d("Swap", "sendOffer token=${token.take(6)}..., toUser=${other?.userId}, itemOffered=$offered, itemRequested=${other?.itemId}")

        if (other == null) { ui = ui.copy(error = "Invalid target"); return@launch }
        if (offered == null) { ui = ui.copy(error = "Select an item first"); return@launch }

        ui = ui.copy(requestInFlight = true, error = null)
        when (val r = offersRepo.addOffer(token, other.userId,other.itemId , offered)) {
            is Result.Success -> { ui = ui.copy(requestInFlight = false, requestDone = true); onSuccess() }
            is Result.Error -> { ui = ui.copy(requestInFlight = false, error = r.message) }
        }
    }

    fun loadOffer(offerId: String) = viewModelScope.launch {
        ui = ui.copy(loading = true, error = null)
        val token = tokenProvider()
        when (val r = offersRepo.getOfferById(token, offerId)) {
            is Result.Success -> {
                val od = r.data
                val meItemThumb = od.me?.item?.thumbnail
                val partner = od.partner
                val partnerItem = partner?.item

                ui = ui.copy(
                    loading = false,
                    error = null,
                    offerStatus = od.status,
                    isSender = od.isSender == true,

                    // 👇 بالایی: آیتم خودم
                    myOfferItemImagePath = meItemThumb,

                    // 👇 پایینی: آیتم طرف مقابل + متادیتا
                    other = if (partner != null) {
                        SwapOther(
                            userId = partner._id.orEmpty(),
                            userName = partner.username.orEmpty(),
                            userAvatarPath = partner.avatar,
                            itemId = partnerItem?._id.orEmpty(),
                            itemImagePath = partnerItem?.thumbnail,
                            itemTitle = partnerItem?.title
                        )
                    } else null
                )
            }
            is Result.Error -> {
                ui = ui.copy(loading = false, error = r.message ?: "Failed to load offer")
            }
        }

    }

    fun acceptIncomingOffer(offerId: String, onDone: () -> Unit = {}) = viewModelScope.launch {
        val token = tokenProvider()
        ui = ui.copy(requestInFlight = true, error = null)
        when (val r = offersRepo.acceptOffer(token, offerId)) {
            is Result.Success -> {
                ui = ui.copy(requestInFlight = false)
                onDone()
            }
            is Result.Error -> {
                ui = ui.copy(requestInFlight = false, error = r.message)
            }
        }
    }

    fun rejectIncomingOffer(offerId: String, onDone: () -> Unit = {}) = viewModelScope.launch {
        val token = tokenProvider()
        ui = ui.copy(requestInFlight = true, error = null)
        when (val r = offersRepo.rejectOffer(token, offerId)) {
            is Result.Success -> {
                ui = ui.copy(requestInFlight = false)
                onDone()
            }
            is Result.Error -> {
                ui = ui.copy(requestInFlight = false, error = r.message)
            }
        }
    }


    // helper: ساخت URL کامل تصویر
    fun imgUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        return if (path.startsWith("http", true)) path
        else Public.BASE_URL_IMAGE.trimEnd('/') + path
    }
}
