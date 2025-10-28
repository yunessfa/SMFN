package com.dibachain.smfn.activity.edit


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dibachain.smfn.data.CategoryRepository

class EditInterestsVMFactory(
    private val app: Application,
    private val repo: CategoryRepository,
    private val tokenProvider: suspend () -> String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditInterestsViewModel(app, repo, tokenProvider) as T
    }
}
