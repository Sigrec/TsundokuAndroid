package com.tsundoku.models

import com.tsundoku.anilist.collection.CollectionViewModel
import com.tsundoku.anilist.viewer.ViewerViewModel
import kotlinx.serialization.Serializable

class ViewerModel {
    companion object {
        suspend fun batchUpdateMediaVolumeCount(viewerViewModel: ViewerViewModel, collectionViewModel: CollectionViewModel) {
            if (viewerViewModel.updatedCollectionItems.value.isNotEmpty()) {
                val updateList: MutableList<VolumeUpdateMedia> = mutableListOf()
                val viewerId = viewerViewModel.getViewerId()
                collectionViewModel.tsundokuCollection.value.forEach {
                    if (viewerViewModel.updatedCollectionItems.value.contains(it.mediaId)) {
                        updateList.add(VolumeUpdateMedia(it.mediaId, viewerId, it.curVolumes.value))
                    }
                }

                if (updateList.isNotEmpty()) {
                    viewerViewModel.batchUpdateDatabaseMedia(updateList)
                    collectionViewModel.clearTsundokuCollection()
                }
            }
        }
        fun parseTrueCustomLists(customListsOutput: StringBuilder): MutableList<String> {
            return customListsOutput.deleteCharAt(0).deleteCharAt(customListsOutput.length - 1).split(", ")
                .filter{ it.contains("=true") }
                .map {
                    if (it.contains("=true")) it.trim().substringBefore("=true")
                    else it.trim()
                }.toMutableList()
        }

        fun parseCustomLists(customListsOutput: StringBuilder): MutableList<String> {
            return customListsOutput.deleteCharAt(0).deleteCharAt(customListsOutput.length - 1).split(", ")
                .map {
                    if (it.contains("=false")) it.trim().substringBefore("=false")
                    else if (it.contains("=true")) it.trim().substringBefore("=true")
                    else it.trim()
                }.toMutableList()
        }
    }
}

@Serializable
data class Viewer(
    val id: Int? = null,
    /**
     * Currency code the viewer currently uses
     */
    val currency: String,
)