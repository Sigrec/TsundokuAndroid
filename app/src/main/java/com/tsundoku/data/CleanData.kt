package com.tsundoku.data

class CleanData {
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