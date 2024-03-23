package com.tsundoku.anilist.viewer

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.tsundoku.APP_NAME
import com.tsundoku.AddTsundokuListMutation
import com.tsundoku.GetCustomListsQuery
import com.tsundoku.UpdateMediaNotesMutation
import com.tsundoku.ViewerQuery
import com.tsundoku.anilist.AuthorizedClient
import com.tsundoku.anilist.preferences.PreferencesRepositoryImpl
import com.tsundoku.extensions.asResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ViewerRepositoryImpl @Inject constructor(
    @AuthorizedClient private val authAniListClient: ApolloClient,
    private val aniListClient: ApolloClient,
    preferencesRepo: PreferencesRepositoryImpl
): ViewerRepository {
    private val mediaId = 12567
    val getMediaIdQuery =
        """
            query UpdateTsundokuCollection {
            	MediaList(id: 108832) {
                  mediaId
              }
            }

        """.trimIndent()

    val addEntryToCollectionMutation = // Add "$"
        """
            mutation updateMangaEntry {
              SaveMediaListEntry(mediaId: $mediaId, customLists: []) {
                mediaId
                customLists
              }
            }

        """.trimIndent()

    /**
     * Gets the current authenticated viewer
     */
    override fun getViewer() = authAniListClient
            .query(ViewerQuery())
            .fetchPolicy(FetchPolicy.CacheAndNetwork)
            .toFlow()
            .asResult { it.Viewer!! }

    /**
     * Gets the current "custom lists" for the authenticated user
     */
    override fun getCustomLists(userId: Int) = aniListClient
            .query(GetCustomListsQuery(userId = userId))
            .fetchPolicy(FetchPolicy.CacheAndNetwork)
            .toFlow()
            .asResult { it.MediaList!! }

    /**
     * Adds "Tsundoku" custom list to the authenticated users "custom lists"
     * @param customLists The current list of "custom lists" for the authenticated user
     */
    override fun addTsundokuList(customLists: MutableList<String>): Flow<Result<AddTsundokuListMutation.UpdateUser>> {
        customLists.add(APP_NAME)
        return authAniListClient
            .mutation(AddTsundokuListMutation(customLists = customLists))
            .fetchPolicy(FetchPolicy.CacheAndNetwork)
            .toFlow()
            .asResult { it.UpdateUser!! }
    }

    override fun updateMediaNotes(mediaId: Int?, newNote: String) = authAniListClient
        .mutation(UpdateMediaNotesMutation(mediaId = Optional.presentIfNotNull(mediaId), notes = Optional.present(newNote)))
        .fetchPolicy(FetchPolicy.CacheAndNetwork)
        .toFlow()
        .asResult { it.SaveMediaListEntry!! }
}