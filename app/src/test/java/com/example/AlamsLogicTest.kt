package com.example

import org.junit.Assert.assertEquals
import org.junit.Test

class AlamsLogicTest {

    @Test
    fun testNegativeMarkingScoreCalculation() {
        val totalQuestions = 20
        val correctCount = 15
        val wrongCount = 4
        val unansweredCount = 1
        val marksPerQuestion = 1.0f
        val negativePenaltyPerWrong = 0.25f

        val calculatedScore = (correctCount * marksPerQuestion) - (wrongCount * negativePenaltyPerWrong)
        assertEquals(14.0f, calculatedScore, 0.001f)
    }

    @Test
    fun testPhoneNumberMasking() {
        val rawPhone = "01712345678"
        val maskedPhone = if (rawPhone.length >= 7) {
            rawPhone.take(3) + "****" + rawPhone.takeLast(4)
        } else {
            "017****0000"
        }
        assertEquals("017****5678", maskedPhone)
    }

    @Test
    fun testYouTubeCleanVideoIdExtraction() {
        val fullUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=42s"
        val cleanVideoId = when {
            fullUrl.contains("v=") -> fullUrl.substringAfter("v=").substringBefore("&")
            fullUrl.contains("youtu.be/") -> fullUrl.substringAfter("youtu.be/").substringBefore("?")
            else -> fullUrl.trim()
        }
        assertEquals("dQw4w9WgXcQ", cleanVideoId)
    }
}
