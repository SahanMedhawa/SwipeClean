package com.example.swipeclean.ui.navigation

/**
 * String routes for the Compose Navigation graph.
 */
object Destinations {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val SWIPE = "swipe"
    const val BIN = "bin"
    const val PREVIEW = "preview/{mediaId}"
    const val ANALYTICS = "analytics"
    const val SETTINGS = "settings"

    fun preview(mediaId: Long) = "preview/$mediaId"
}
