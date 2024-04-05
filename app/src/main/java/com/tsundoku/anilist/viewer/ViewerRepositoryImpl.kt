package com.tsundoku.anilist.viewer

import android.util.Log
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.tsundoku.APP_NAME
import com.tsundoku.AddMediaToTsundokuMutation
import com.tsundoku.AddTsundokuListMutation
import com.tsundoku.DATABASE_MEDIA_TABLE
import com.tsundoku.DATABASE_VIEWER_TABLE
import com.tsundoku.DeleteMediaFromTsundokuMutation
import com.tsundoku.GetCustomListsQuery
import com.tsundoku.GetMediaCustomListsQuery
import com.tsundoku.GetMediaEntryQuery
import com.tsundoku.UpdateMediaNotesMutation
import com.tsundoku.ViewerQuery
import com.tsundoku.anilist.AuthorizedClient
import com.tsundoku.data.TsundokuFormat
import com.tsundoku.extensions.asResult
import com.tsundoku.models.Media
import com.tsundoku.models.Viewer
import com.tsundoku.models.VolumeUpdateMedia
import com.tsundoku.type.MediaFormat
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
    override fun getViewerCustomLists(userId: Int) = aniListClient
            .query(GetCustomListsQuery(userId = userId))
            .fetchPolicy(FetchPolicy.CacheAndNetwork)
            .toFlow()
            .asResult { it.MediaList!! }

    /**
     * Gets the custom lists for a given media for the authenticated user
     * @param viewerId The authenticated users AniList ID
     * @param mediaId The unique id of the media to update
     */
    override fun getMediaCustomLists(viewerId: Int, mediaId: Int) = aniListClient
        .query(GetMediaCustomListsQuery(viewerId = viewerId, mediaId = mediaId))
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

    override fun getNewAniListMediaSeries(seriesId: Int?, title: String?, format: TsundokuFormat) = authAniListClient
        .query(GetMediaEntryQuery(seriesId = Optional.presentIfNotNull(seriesId), type = if (format == TsundokuFormat.NOVEL) MediaFormat.NOVEL else MediaFormat.MANGA, title = Optional.presentIfNotNull(title)))
        .fetchPolicy(FetchPolicy.CacheAndNetwork)
        .toFlow()
        .asResult { it.Media!! }

    /**
     * Updates the viewers notes for a specific series
     * @param mediaId The unique id of the media to update
     * @param newNote The new note to update to for the media
     */
    override fun updateAniListMediaNotes(mediaId: Int, newNote: String) = authAniListClient
        .mutation(UpdateMediaNotesMutation(mediaId = Optional.presentIfNotNull(mediaId), notes = Optional.present(newNote)))
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .toFlow()
        .asResult { it.SaveMediaListEntry!! }

    /**
     * Adds a AniList media to "Tsundoku" custom list in AniList
     * @param mediaId The unique AniList media ID for the series
     */
    override fun addAniListMediaToCollection(mediaId: Int, customLists: List<String>): Flow<Result<AddMediaToTsundokuMutation.SaveMediaListEntry>> {
        return authAniListClient
            .mutation(AddMediaToTsundokuMutation(mediaId = mediaId, customLists = customLists))
            .fetchPolicy(FetchPolicy.NetworkOnly)
            .toFlow()
            .asResult { it.SaveMediaListEntry!! }
    }

    /**
     * Deletes a AniList media series from the authenticated users "Tsundoku" custom list in AniList
     * @param mediaId The unique AniList media ID for the series
     * @param customLists The users current list of custom lists to not override others
     */
    override fun deleteAniListMediaFromCollection(mediaId: Int, customLists: List<String>) = authAniListClient
        .mutation(DeleteMediaFromTsundokuMutation(mediaId = mediaId, customLists = customLists))
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .toFlow()
        .asResult { it.SaveMediaListEntry!! }

    /**
     *
     */
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

    override suspend fun updateCurrencyCode(viewerId: Int, currencyCode: String) {
        supabase.from(DATABASE_VIEWER_TABLE).update(
            {
                set("currency", currencyCode)
            }
        ) {
            filter {
                eq("id", viewerId)
            }
        }
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
                if(updateMap["cost"] != null) set("cost", updateMap["cost"].toString())
                if(updateMap["notes"] != null) set("notes", updateMap["notes"].toString().ifBlank { null })
            }
        ) {
            filter {
                eq("mediaId", mediaId)
                eq("viewerId", viewerId)
            }
        }
    }

    override suspend fun batchCurVolumesUpdateMedia(viewerId: Int, updateList: MutableList<VolumeUpdateMedia>) {
        Log.d("Tsundoku", "Batch Updating $updateList for Viewer $viewerId")
        supabase.from(DATABASE_MEDIA_TABLE).upsert(updateList) {
            filter {
                eq("viewerId", viewerId)
            }
        }
    }

    override suspend fun deleteMedia(viewerId: Int, deleteList: List<String>) {
        Log.d("Supabase", "Deleting $deleteList for Viewer $viewerId")
        supabase.from(DATABASE_MEDIA_TABLE).delete {
            filter {
                eq("viewerId", viewerId)
                isIn("mediaId", deleteList)
            }
        }
    }
}