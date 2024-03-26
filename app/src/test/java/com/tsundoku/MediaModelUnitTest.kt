package com.tsundoku

import com.tsundoku.models.MediaModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class MediaModelUnitTest {
    @Test
    fun getCorrectFormat_AniList_Manga_Test() {
        assertEquals("Manga", MediaModel.getCorrectFormat("MANGA", "JP"))
    }

    @Test
    fun getCorrectFormat_AniList_Manhwa_Test() {
        assertEquals("Manhwa", MediaModel.getCorrectFormat("MANGA", "KR"))
    }

    @Test
    fun getCorrectFormat_AniList_Manhua_Test() {
        assertEquals("Manhua", MediaModel.getCorrectFormat("MANGA", "CN"))
    }

    @Test
    fun costRegex_ValidMatch_Test() {
        assertTrue(MediaModel.costRegex.matches("500.00"))
        assertTrue(MediaModel.costRegex.matches("500.0"))
        assertTrue(MediaModel.costRegex.matches("500."))
        assertTrue(MediaModel.costRegex.matches("500"))
    }

    @Test
    fun costRegex_InvalidMatch_Test() {
        assertTrue(!MediaModel.costRegex.matches("500.000"))
        assertTrue(!MediaModel.costRegex.matches(" "))
        assertTrue(!MediaModel.costRegex.matches("\t"))
        assertTrue(!MediaModel.costRegex.matches("\n"))
    }
}