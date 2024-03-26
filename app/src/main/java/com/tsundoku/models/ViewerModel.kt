package com.tsundoku.models

import kotlinx.serialization.Serializable

class ViewerModel {
    companion object {
        fun parseCustomLists(customListsOutput: StringBuilder): MutableList<String> {
            return customListsOutput.deleteCharAt(0).deleteCharAt(customListsOutput.length - 1).split(", ").map {
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