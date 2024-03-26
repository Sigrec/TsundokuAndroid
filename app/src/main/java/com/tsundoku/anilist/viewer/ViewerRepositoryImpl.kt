package com.tsundoku.anilist.viewer

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.tsundoku.APP_NAME
import com.tsundoku.AddTsundokuListMutation
import com.tsundoku.DATABASE_MEDIA_TABLE
import com.tsundoku.DATABASE_VIEWER_TABLE
import com.tsundoku.GetCustomListsQuery
import com.tsundoku.UpdateMediaNotesMutation
import com.tsundoku.ViewerQuery
import com.tsundoku.anilist.AuthorizedClient
import com.tsundoku.extensions.asResult
import com.tsundoku.models.Media
import com.tsundoku.models.Viewer
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ViewerRepositoryImpl @Inject constructor(
    @AuthorizedClient private val authAniListClient: ApolloClient,
    private val aniListClient: ApolloClient,
    private val supabase: SupabaseClient
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
    override fun getAniListViewer() = authAniListClient
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

    /**
     * Updates the viewers notes for a specific series
     * @param mediaId The unique id of the media to update
     * @param newNote The new note to update to for the media
     */
    override fun updateAniListMediaNotes(mediaId: Int, newNote: String) = authAniListClient
        .mutation(UpdateMediaNotesMutation(mediaId = Optional.presentIfNotNull(mediaId), notes = Optional.present(newNote)))
        .fetchPolicy(FetchPolicy.CacheAndNetwork)
        .toFlow()
        .asResult { it.SaveMediaListEntry!! }

    override suspend fun getDatabaseViewerCurrencyCode(viewerId: Int): Viewer? {
        return supabase.from(DATABASE_VIEWER_TABLE).select(columns = Columns.list("currency")) {
            filter {
                eq("id", viewerId)
            }
        }.decodeSingleOrNull<Viewer>()
    }

    override suspend fun insertDatabaseViewer(viewerId: Int) {
        supabase.from(DATABASE_VIEWER_TABLE).insert(mapOf("id" to viewerId))
    }

    override suspend fun insertNewMedia(mediaList: List<Media>) {
        supabase.from(DATABASE_MEDIA_TABLE).insert(mediaList)
    }

    override suspend fun getMediaList(viewerId: Int): List<Media> {
        return supabase.from(DATABASE_MEDIA_TABLE).select(columns = Columns.list("mediaId", "curVolumes", "maxVolumes", "cost")) {
            filter {
                eq("viewerId", viewerId)
            }
        }.decodeList<Media>()
    }

    override suspend fun updateMedia(viewerId: Int, mediaId: String, updateMap: Map<String, Any?>) {
        supabase.from(DATABASE_MEDIA_TABLE).update(
            {
                if(updateMap["curVolumes"] != null) set("curVolumes", updateMap["curVolumes"].toString().toInt())
                if(updateMap["maxVolumes"] != null) set("maxVolumes", updateMap["maxVolumes"].toString().toInt())
                if(updateMap["cost"] != null) set("cost", updateMap["cost"].toString().toBigDecimal())
                if(updateMap["notes"] != null) set("notes", updateMap["notes"].toString().ifBlank { null })
            }
        ) {
            filter {
                eq("mediaId", mediaId)
                eq("viewerId", viewerId)
            }
        }
    }
}