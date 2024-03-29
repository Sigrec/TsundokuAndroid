package com.tsundoku.data

enum class TsundokuFilter() {
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

    lateinit var value: String

    constructor(
        value: String,
    ) : this() {
        this.value = value
    }

    companion object {
        fun getFilterValue(value: String) = TsundokuFilter.entries.first { it.value == value }
    }
}