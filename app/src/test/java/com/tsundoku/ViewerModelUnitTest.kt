package com.tsundoku

import com.tsundoku.models.ViewerModel
import junit.framework.TestCase.assertEquals
import org.junit.Test

class CleanDataTests {
    @Test
    fun parseCustomLists_Test() {
        assertEquals(listOf("Test", "Test1", "Test2", "Test3"), ViewerModel.parseCustomLists(StringBuilder("{Test=false, Test1=false, Test2=false, Test3=false}")))
    }
}