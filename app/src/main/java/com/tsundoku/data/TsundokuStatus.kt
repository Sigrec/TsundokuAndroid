package com.tsundoku.data

enum class TsundokuStatus(val value: String) {
    FINISHED("Finished"),
    ONGOING("Ongoing"),
    COMING_SOON("Coming Soon"),
    CANCELLED("Cancelled"),
    HIATUS("Hiatus"),
    ERROR("Error");

    companion object {
        fun getFilterValue(value: String) = TsundokuFilter.entries.first { it.value == value }
    }

    override fun toString(): String {
        return value
    }
}