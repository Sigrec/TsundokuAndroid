package com.tsundoku.data

enum class TsundokuFormat(val value: String) {
    MANGA("Manga"),
    MANHWA("Manhwa"),
    MANHUA("Manhua"),
    MANFRA("Manfra"),
    COMIC("Comic"),
    NOVEL("Novel"),
    ERROR("Error");

    override fun toString(): String {
        return value
    }
}