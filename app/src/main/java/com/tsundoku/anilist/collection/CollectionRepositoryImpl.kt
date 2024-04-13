package com.tsundoku.anilist.collection

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.tsundoku.APP_NAME
import com.tsundoku.GetTsundokuCollectionQuery
import com.tsundoku.UserQuery
import com.tsundoku.anilist.AuthorizedClient
import com.tsundoku.extensions.asResult
import com.tsundoku.type.MediaListSort
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionRepositoryImpl @Inject constructor(
    private val aniListClient: ApolloClient,
    @AuthorizedClient private val authAniListClient: ApolloClient,
): CollectionRepository {
    override fun getSearchedUser(username: String) = aniListClient
        .query(UserQuery(username = username))
        .fetchPolicy(FetchPolicy.CacheAndNetwork)
        .toFlow()
        .asResult { it.User!! }

    override fun getTsundokuCollection(username: String?, userId: Int?, titleSort: List<MediaListSort?>): Flow<Result<List<GetTsundokuCollectionQuery.List?>>> {
        return authAniListClient
            .query(GetTsundokuCollectionQuery(Optional.presentIfNotNull(username), Optional.presentIfNotNull(userId), titleSort))
            .fetchPolicy(FetchPolicy.CacheAndNetwork)
            .toFlow()
            .asResult { data ->
                data.MediaListCollection!!.lists!!.filter { list ->
                    list!!.name == APP_NAME
                }
            }
    }
}