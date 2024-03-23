package com.tsundoku

import com.tsundoku.data.CleanData
import junit.framework.TestCase.assertEquals
import org.junit.Test

class CleanDataTests {
    @Test
    fun parseCustomLists() {
        assertEquals(listOf("Test", "Test1", "Test2", "Test3"), CleanData.parseCustomLists(StringBuilder("{Test=false, Test1=false, Test2=false, Test3=false}")))
    }
}