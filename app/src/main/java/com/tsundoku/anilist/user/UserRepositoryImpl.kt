package com.tsundoku.anilist.user

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.tsundoku.APP_NAME
import com.tsundoku.GetCustomListsQuery
import com.tsundoku.GetTsundokuCollectionQuery
import com.tsundoku.ViewerQuery
import com.tsundoku.anilist.preferences.PreferencesRepositoryImpl
import com.tsundoku.anilist.viewer.UserRepository
import com.tsundoku.extensions.asResult
import com.tsundoku.type.MediaListSort
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val aniListClient: ApolloClient,
    preferencesRepo: PreferencesRepositoryImpl
): UserRepository {
    override fun getUserByUsername(): Flow<Result<ViewerQuery.Viewer>> {
        TODO("Not yet implemented")
    }

    override fun getCustomLists(userId: Int): Flow<Result<GetCustomListsQuery.MediaList>> {
        TODO("Not yet implemented")
    }

    override fun getTsundokuCollection(username: String?, userId: Int?, titleSort: List<MediaListSort?>) = aniListClient
        .query(GetTsundokuCollectionQuery(Optional.presentIfNotNull(username), Optional.presentIfNotNull(userId), titleSort))
        .fetchPolicy(FetchPolicy.CacheAndNetwork)
        .toFlow()
        .asResult { data ->
            data.MediaListCollection!!.lists!!.filter { list ->
                list!!.name == APP_NAME
            }
        }
}