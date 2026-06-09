package com.personal.autoscroll.domain.model

enum class LanguageMode(
    val languageTag: String?,
) {
    System(languageTag = null),
    English(languageTag = "en"),
    Vietnamese(languageTag = "vi"),
}
