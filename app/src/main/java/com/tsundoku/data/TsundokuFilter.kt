package com.tsundoku.data

import androidx.compose.ui.util.fastFirstOrNull

enum class TsundokuFilter(val value: String) {
    NONE("None"),
    MANGA("Manga"),
    NOVEL("Novel"),
    ONGOING("Ongoing"),
    FINISHED("Finished"),
    COMING_SOON("Coming Soon"),
    CANCELLED("Cancelled"),
    HIATUS("Hiatus"),
    COMPLETE("Complete"),
    INCOMPLETE("Incomplete");

    companion object {
        fun parse(input: String): TsundokuFilter {
            return TsundokuFilter.entries.fastFirstOrNull { it.value == input } ?: NONE
        }
    }

    override fun toString(): String {
        return value
    }
}