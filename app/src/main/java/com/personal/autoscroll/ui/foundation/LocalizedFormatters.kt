package com.personal.autoscroll.ui.foundation

import java.text.NumberFormat
import java.util.Locale

object LocalizedFormatters {
    fun seconds(millis: Long, locale: Locale = Locale.getDefault()): String {
        val number = NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 1
            maximumFractionDigits = 1
        }
        return "${number.format(millis / 1000.0)}s"
    }

    fun seconds(seconds: Float, locale: Locale = Locale.getDefault()): String {
        val number = NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 1
            maximumFractionDigits = 1
        }
        return "${number.format(seconds)}s"
    }

    fun percent(value: Float, locale: Locale = Locale.getDefault()): String {
        val number = NumberFormat.getPercentInstance(locale).apply {
            maximumFractionDigits = 0
        }
        return number.format(value)
    }
}
