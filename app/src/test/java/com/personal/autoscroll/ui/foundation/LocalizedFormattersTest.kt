package com.personal.autoscroll.ui.foundation

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalizedFormattersTest {
    @Test
    fun secondsUsesVietnameseDecimalSeparator() {
        assertEquals("0,6s", LocalizedFormatters.seconds(600L, Locale.forLanguageTag("vi-VN")))
    }

    @Test
    fun secondsUsesEnglishDecimalSeparator() {
        assertEquals("0.6s", LocalizedFormatters.seconds(600L, Locale.US))
    }
}
