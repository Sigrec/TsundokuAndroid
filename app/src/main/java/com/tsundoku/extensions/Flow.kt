package com.tsundoku.extensions

import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Operation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
fun <T: Operation.Data, R> Flow<ApolloResponse<T>>.asResult(transform: (T) -> R): Flow<Result<R>> = this
    .mapLatest { response ->
        if (response.data != null) {
            Result.success(transform(response.dataAssertNoErrors))
        } else if (response.hasErrors()) {
            // TODO What kind of exception do we provide here?
            throw IOException(response.errors!!.joinToString())
        } else {
            error("Unknown error occurred for ${response.operation.name()} query, no data or errors received.")
        }
    }
    .catch { exception ->
        emit(Result.failure(exception))
    }

fun <T: Operation.Data> Flow<ApolloResponse<T>>.asResult(): Flow<Result<T>> = this.asResult { it }

//@OptIn(ExperimentalCoroutinesApi::class)
//fun <R> Flow<List<TsundokuItem>>.asResult(transform: (List<TsundokuItem>) -> R): Flow<Result<R>> = this
//    .mapLatest { response ->
//        Result.success(transform(response))
//    }
//    .catch { exception ->
//        emit(Result.failure(exception))
//    }
//
//fun Flow<List<TsundokuItem>>.asResult(): Flow<Result<List<TsundokuItem>>> = this.asResult { it }