package com.tsundoku.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest

sealed class NetworkResource<T>(
    open val data: T?,
    open val message: String? = null
) {

    data class Success<T>(override val data: T) : NetworkResource<T>(data)
    data class Error<T>(override val message: String?, override val data: T? = null) : NetworkResource<T>(data, message)
    class Loading<T> : NetworkResource<T>(null)

    companion object {
        fun <T> success(data: T): NetworkResource<T> = Success(data)

        fun <T> error(msg: String, data: T? = null): NetworkResource<T> = Error(msg, data)

        fun <T> loading(): NetworkResource<T> = Loading()

        @OptIn(ExperimentalCoroutinesApi::class)
        fun <T, R> Flow<Result<T>>.asResource(transform: (T) -> R): Flow<NetworkResource<R>> = this
            .mapLatest { success(transform(it.getOrThrow())) }
            .catch { emit(error(it.message.orEmpty())) }

        fun <T> Flow<Result<T>>.asResource(): Flow<NetworkResource<T>> = this.asResource { it }

        private fun <T> networkError() = error<T>("Network error, please check your connection and try again.")
        private fun <T> defaultError() = error<T>("Unknown error occurred, please try again later.")
        private fun <T> noDataError() = error<T>("Unknown error occurred, no data or errors received.")
    }
}