package com.tsundoku.anilist

import android.content.Context
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import com.apollographql.apollo3.network.http.LoggingInterceptor
import com.tsundoku.ANILIST_GRAPHQL_BASE_URL
import com.tsundoku.anilist.preferences.PreferencesRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AniListModule {
    @Provides
    @Singleton
    fun provideAniListClient(@ApplicationContext context: Context): ApolloClient {
        // Cache is hit in order, so check in-memory -> check sqlite
        // We have an in-memory cache first for speed, then a SQLite cache for persistence.
        return ApolloClient.Builder()
            .dispatcher(Dispatchers.IO)
            .serverUrl(ANILIST_GRAPHQL_BASE_URL)
            .addHttpInterceptor(LoggingInterceptor(LoggingInterceptor.Level.BODY))
            .normalizedCache(MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024).chain(SqlNormalizedCacheFactory(context, "apollo.db")))
            .build()
    }

    @Provides
    @Singleton
    @AuthorizedClient
    fun provideAuthorizedAniListClient(
        @ApplicationContext context: Context,
        authorizationInterceptor: HttpInterceptor
    ): ApolloClient {
        val cacheFactory = MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024).chain(SqlNormalizedCacheFactory(context, "apollo.db"))
        return ApolloClient.Builder()
            .dispatcher(Dispatchers.IO)
            .serverUrl(ANILIST_GRAPHQL_BASE_URL)
            .addHttpInterceptor(authorizationInterceptor)
            .addHttpInterceptor(LoggingInterceptor(LoggingInterceptor.Level.BODY))
            .normalizedCache(cacheFactory)
            .build()
    }

    @Provides
    @Singleton
    fun provideHttpInterceptor(
        preferencesRepository: PreferencesRepositoryImpl
    ): HttpInterceptor = object : HttpInterceptor {
        override suspend fun intercept(
            request: HttpRequest,
            chain: HttpInterceptorChain
        ): HttpResponse {
            return chain.proceed(
                request.newBuilder().apply {
                    preferencesRepository.accessToken.first()?.let {
                        addHeader("Authorization", "Bearer $it")
                    }
                }.build()
            )
        }
    }

//    @Provides
//    @Singleton
//    fun provideUserRepo(client: ApolloClient): ViewerRepositoryImpl {
//        return ViewerRepositoryImpl(client)
//    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthorizedClient