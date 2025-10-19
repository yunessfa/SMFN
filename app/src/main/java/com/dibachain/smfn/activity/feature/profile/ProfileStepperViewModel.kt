package com.dibachain.smfn.activity.feature.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.AuthRepository
import com.dibachain.smfn.data.CategoryRepository
import com.dibachain.smfn.data.remote.CategoryDto
import com.dibachain.smfn.data.remote.uriToImagePartWithProgress
import com.dibachain.smfn.data.remote.uriToVideoPartWithProgress
import com.dibachain.smfn.flags.ProfileProgressStore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileUiState(
    val step: Int = 0,
    val phone: String = "",
    val phoneErr: String? = null,

    val fullName: String = "",
    val fullNameErr: String? = null,
    val username: String = "",
    val userErr: String? = null,
    val gender: String = "",
    val genderErr: String? = null,

    val avatar: String? = null,
    val kycVideo: String? = null,
    val interests: List<String> = emptyList(),
    val loading: Boolean = false,
    val uploadProgress: Int? = null,
    val uploadDone: Boolean = false,
    val kycUploadProgress: Int? = null,
    val kycUploadDone: Boolean = false,
    val catLoading: Boolean = false,
    val parents: List<CategoryDto> = emptyList(),
    val childrenByParent: Map<String, List<CategoryDto>> = emptyMap(),
    val expandedKey: String? = null,
    val loadingChildrenFor: String? = null

)

sealed interface ProfileEvent {
    data class Toast(val msg: String): ProfileEvent
    data object GoNext: ProfileEvent
    data object Done: ProfileEvent
}

class ProfileStepperViewModel(
    app: Application,
    private val repo: AuthRepository,
    private val tokenProvider: suspend () -> String,
    private val catRepo: CategoryRepository
) : AndroidViewModel(app) {

    private val store = ProfileProgressStore(app)

    private val _ui = MutableStateFlow(ProfileUiState())
    val ui: StateFlow<ProfileUiState> = _ui.asStateFlow()

    private val _events = Channel<ProfileEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val saved = store.load()
            _ui.update {
                it.copy(
                    step = saved.step,
                    phone = saved.phone,
                    fullName = saved.fullName,
                    username = saved.username,
                    gender = saved.gender,
                    avatar = saved.avatarUri,
                    kycVideo = saved.kycVideoUri,
                    interests = saved.interests
                )
            }
        }
    }

    fun setPhone(v: String) = _ui.update { it.copy(phone = v, phoneErr = null) }
    fun setFullName(v: String) = _ui.update { it.copy(fullName = v, fullNameErr = null) }
    fun setUsername(v: String) = _ui.update { it.copy(username = v, userErr = null) }
    fun setGender(v: String) = _ui.update { it.copy(gender = v, genderErr = null) }
    fun setAvatar(uri: String?) = _ui.update { it.copy(avatar = uri) }
    fun setKyc(uri: String?) = _ui.update { it.copy(kycVideo = uri) }
    fun setInterests(keys: List<String>) = _ui.update { it.copy(interests = keys) }

    fun nextStep() = viewModelScope.launch {
        val s = (_ui.value.step + 1).coerceAtMost(4)
        _ui.update { it.copy(step = s) }
        store.saveStep(s)
    }
    fun previousStep() = viewModelScope.launch {
        val s = (_ui.value.step - 1).coerceAtLeast(0)
        _ui.update { it.copy(step = s) }
        store.saveStep(s)
    }

    /** استپ ۰: ارسال شماره به سرور + سیو محلی */
    fun submitPhone() = viewModelScope.launch {
        val phone = _ui.value.phone.trim()
        val digits = phone.filter { it.isDigit() }
        if (digits.isEmpty()) { _ui.update { it.copy(phoneErr = "Required") }; return@launch }
        if (digits.length < 8) { _ui.update { it.copy(phoneErr = "Invalid number") }; return@launch }

        _ui.update { it.copy(loading = true, phoneErr = null) }
        val token = runCatching { tokenProvider() }.getOrElse {
            _ui.update { it.copy(loading = false) }
            _events.send(ProfileEvent.Toast("No token"))
            return@launch
        }

        when (val res = repo.addPhone(token, phone)) {
            is Result.Success -> {
                store.savePhone(phone)
                _events.send(ProfileEvent.Toast("Phone verified"))
                _ui.update { it.copy(loading = false) }
                nextStep()
            }
            is Result.Error -> {
                _ui.update { it.copy(loading = false, phoneErr = res.message) }
                _events.send(ProfileEvent.Toast(res.message))
            }
        }
    }

    /** نگاشت مقدار جنسیت UI به چیزی که بک‌اند می‌خواهد */
    private fun mapGenderForApi(ui: String): String = when (ui.lowercase().trim()) {
        "male"   -> "men"
        "female" -> "women"
        else     -> "other"
    }

    /** استپ ۱: فراخوانی API اطلاعات KYC + سیو محلی + رفتن به استپ بعد */
    fun savePersonalAndNext() = viewModelScope.launch {
        val u = _ui.value
        val fnErr = if (u.fullName.isBlank()) "Required" else null
        val usErr = if (u.username.isBlank()) "Required" else null
        val gErr  = if (u.gender.isBlank()) "Required" else null
        if (fnErr != null || usErr != null || gErr != null) {
            _ui.update { it.copy(fullNameErr = fnErr, userErr = usErr, genderErr = gErr) }
            return@launch
        }

        _ui.update { it.copy(loading = true, fullNameErr = null, userErr = null, genderErr = null) }

        val token = runCatching { tokenProvider() }.getOrElse {
            _ui.update { it.copy(loading = false) }
            _events.send(ProfileEvent.Toast("No token"))
            return@launch
        }

        val genderApi = mapGenderForApi(u.gender)

        when (val res = repo.submitKycInformation(
            token = token,
            fullname = u.fullName.trim(),
            username = u.username.trim(),
            gender = genderApi
        )) {
            is Result.Success -> {
                // ذخیره محلی با مقادیر UI (نه mapped)
                store.savePersonal(u.fullName.trim(), u.username.trim(), u.gender)
                _ui.update { it.copy(loading = false) }
                _events.send(ProfileEvent.Toast("Profile info saved"))
                nextStep()
            }
            is Result.Error -> {
                // اگر پیام سرور به یوزرنیم اشاره داشت، زیر همان فیلد نشان بده
                val msg = res.message
                val userErrMsg = if (msg.contains("user", ignoreCase = true)) msg else null
                _ui.update { it.copy(loading = false, userErr = userErrMsg) }
                _events.send(ProfileEvent.Toast(msg))
            }
        }
    }

    fun saveAvatarAndNext() = viewModelScope.launch {
        val uriStr = _ui.value.avatar
        if (uriStr.isNullOrBlank()) {
            _events.send(ProfileEvent.Toast("No image selected"))
            return@launch
        }

        _ui.update { it.copy(loading = true, uploadProgress = 0, uploadDone = false) }

        val token = runCatching { tokenProvider() }.getOrElse {
            _ui.update { it.copy(loading = false, uploadProgress = null) }
            _events.send(ProfileEvent.Toast("No token"))
            return@launch
        }
        val appCtx = getApplication<Application>()
        val cr = appCtx.contentResolver

        // کال‌بک امن برای به‌روزرسانی درصد
        val onProgress: (Int) -> Unit = { p ->
            // انتشار روی Main
            viewModelScope.launch {
                _ui.update { it.copy(uploadProgress = p.coerceIn(0, 100)) }
            }
        }

        val imagePart = runCatching {
            cr.uriToImagePartWithProgress(android.net.Uri.parse(uriStr), onProgress = onProgress)
        }.getOrElse {
            _ui.update { it.copy(loading = false, uploadProgress = null) }
            _events.send(ProfileEvent.Toast("Failed to read image"))
            return@launch
        }

        when (val res = repo.setProfilePicture(token, imagePart)) {
            is Result.Success -> {
                store.saveAvatar(uriStr)
                _ui.update { it.copy(loading = false, uploadProgress = 100, uploadDone = true) }
                _events.send(ProfileEvent.Toast("Picture uploaded"))
                kotlinx.coroutines.delay(400)
                nextStep()
                _ui.update { it.copy(uploadProgress = null, uploadDone = false) }
            }
            is Result.Error -> {
                _ui.update { it.copy(loading = false) }
                _events.send(ProfileEvent.Toast(res.message))
            }
        }
    }

    fun saveKycAndNext() = viewModelScope.launch {
        val uriStr = _ui.value.kycVideo
        if (uriStr.isNullOrBlank()) {
            _events.send(ProfileEvent.Toast("No video to upload"))
            return@launch
        }

        _ui.update { it.copy(loading = true, kycUploadProgress = 0, kycUploadDone = false) }

        val token = runCatching { tokenProvider() }.getOrElse {
            _ui.update { it.copy(loading = false, kycUploadProgress = null) }
            _events.send(ProfileEvent.Toast("No token"))
            return@launch
        }

        val appCtx = getApplication<Application>()
        val cr = appCtx.contentResolver

        val onProgress: (Int) -> Unit = { p ->
            viewModelScope.launch {
                _ui.update { it.copy(kycUploadProgress = p.coerceIn(0, 100)) }
            }
        }

        val videoPart = runCatching {
            cr.uriToVideoPartWithProgress(android.net.Uri.parse(uriStr), onProgress = onProgress)
        }.getOrElse {
            _ui.update { it.copy(loading = false, kycUploadProgress = null) }
            _events.send(ProfileEvent.Toast("Failed to read video"))
            return@launch
        }

        when (val res = repo.addKycVideo(token, videoPart)) {
            is Result.Success -> {
                store.saveKyc(uriStr)
                _ui.update { it.copy(loading = false, kycUploadProgress = 100, kycUploadDone = true) }
                _events.send(ProfileEvent.Toast("Video uploaded"))
                kotlinx.coroutines.delay(500)
                nextStep()
                _ui.update { it.copy(kycUploadProgress = null, kycUploadDone = false) }
            }
            is Result.Error -> {
                _ui.update { it.copy(loading = false) }
                _events.send(ProfileEvent.Toast(res.message))
            }
        }
    }
    private fun fullIcon(path: String?): String? {
        if (path.isNullOrBlank()) return null
        val base = com.dibachain.smfn.core.Public.BASE_URL_IMAGE.trimEnd('/')
        val rel = if (path.startsWith("/")) path else "/$path"
        return base + rel
    }
    fun loadParentsIfNeeded() = viewModelScope.launch {
        if (_ui.value.parents.isNotEmpty() || _ui.value.catLoading) return@launch
        _ui.update { it.copy(catLoading = true) }
        val token = runCatching { tokenProvider() }.getOrElse {
            _ui.update { it.copy(catLoading = false) }
            _events.send(ProfileEvent.Toast("No token"))
            return@launch
        }
        when (val r = catRepo.parents(token)) {
            is Result.Success -> {
                val fixed = r.data.map { it.copy(icon = fullIcon(it.icon)) }
                _ui.update { it.copy(catLoading = false, parents = fixed) }
            }
            is Result.Error -> {
                _ui.update { it.copy(catLoading = false) }
                _events.send(ProfileEvent.Toast(r.message))
            }
        }
    }

    fun toggleExpand(parentId: String) = viewModelScope.launch {
        val curr = _ui.value.expandedKey
        val newKey = if (curr == parentId) null else parentId
        _ui.update { it.copy(expandedKey = newKey) }
        if (newKey != null) loadChildrenIfNeeded(newKey)
    }

    private fun loadChildrenIfNeeded(parentId: String) = viewModelScope.launch {
        if (_ui.value.childrenByParent[parentId]?.isNotEmpty() == true) return@launch
        val token = runCatching { tokenProvider() }.getOrElse {
            _events.send(ProfileEvent.Toast("No token")); return@launch
        }
        when (val r = catRepo.children(token, parentId)) {
            is Result.Success -> {
                val fixed = r.data.map { it.copy(icon = fullIcon(it.icon)) }
                _ui.update { it.copy(childrenByParent = it.childrenByParent + (parentId to fixed)) }
            }
            is Result.Error -> _events.send(ProfileEvent.Toast(r.message))
        }
    }

    fun submitInterestsToServer(onRequirePremium: () -> Unit) = viewModelScope.launch {
        val ids = _ui.value.interests
        if (ids.size < 4) {
            _events.send(ProfileEvent.Toast("Select at least 4"))
            return@launch
        }
        _ui.update { it.copy(loading = true) }
        val token = runCatching { tokenProvider() }.getOrElse {
            _ui.update { it.copy(loading = false) }
            _events.send(ProfileEvent.Toast("No token")); return@launch
        }
        when (val r = catRepo.setInterests(token, ids)) {
            is Result.Success -> {
                store.saveInterests(ids)
                _ui.update { it.copy(loading = false) }
                _events.send(ProfileEvent.Toast("Interests saved"))
                _events.send(ProfileEvent.Done)
            }
            is Result.Error -> {
                _ui.update { it.copy(loading = false) }
                if (r.code == 402) {
                    // Premium required (مطابق اسکرین‌شات 402)
                    onRequirePremium()
                } else {
                    _events.send(ProfileEvent.Toast(r.message))
                }
            }
        }
    }
    fun expandParent(id: String) = viewModelScope.launch {
        // اگر همین الان باز است → ببند
        if (_ui.value.expandedKey == id) {
            _ui.update { it.copy(expandedKey = null) }
            return@launch
        }

        _ui.update { it.copy(expandedKey = id) }

        // اگر قبلاً کش شده، تمام
        val cached = _ui.value.childrenByParent[id]
        if (cached != null) return@launch

        // لود با اسپینر محلی
        _ui.update { it.copy(loadingChildrenFor = id) }
        val token = runCatching { tokenProvider() }.getOrElse {
            _ui.update { it.copy(loadingChildrenFor = null) }
            _events.trySend(ProfileEvent.Toast("No token"))
            return@launch
        }

        when (val r = catRepo.children(token, id)) {
            is Result.Success -> {
                val fixed = r.data.map { it.copy(icon = fullIcon(it.icon)) }
                _ui.update {
                    it.copy(
                        loadingChildrenFor = null,
                        childrenByParent = it.childrenByParent + (id to fixed)
                    )
                }
            }
            is Result.Error -> {
                _ui.update { it.copy(loadingChildrenFor = null) }
                _events.trySend(ProfileEvent.Toast(r.message))
            }
        }
    }


}
