// common/Result.kt
package com.dibachain.smfn.common
sealed interface Result<out T> {
    data class Success<T>(val data: T): Result<T>
    data class Error(val code: Int? = null, val message: String = "Unexpected error"): Result<Nothing>
}
