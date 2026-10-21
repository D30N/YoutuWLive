package com.deon.ytsubslive

import android.content.Context
import android.graphics.Color

/**
 * Card themes, InstaW Live style: each theme recolours the home-screen
 * widget card, the in-app milestone card and the Settings preview.
 */
object Themes {

    data class Theme(
        val key: String,
        val name: String,
        val bg: Int,
        val accent: Int,
        val text: Int,
        val sub: Int
    )

    val ALL: List<Theme> = listOf(
        Theme("midnight", "Midnight", c("#12121B"), c("#FF0033"), c("#F5F5FA"), c("#9A9AAD")),
        Theme("ocean", "Ocean", c("#0A1E33"), c("#38BDF8"), c("#EFF8FF"), c("#8FB3CC")),
        Theme("emerald", "Emerald", c("#0A241B"), c("#34D399"), c("#ECFDF5"), c("#8FC7AE")),
        Theme("violet", "Violet", c("#1B1030"), c("#A78BFA"), c("#F3EFFF"), c("#A79BC7")),
        Theme("sunset", "Sunset", c("#2A130A"), c("#FB923C"), c("#FFF5EC"), c("#CCA48C")),
        Theme("snow", "Snow", c("#F4F4F8"), c("#E62117"), c("#17171F"), c("#6E6E80"))
    )

    private fun c(hex: String): Int = Color.parseColor(hex)

    fun key(ctx: Context): String =
        YtApi.prefs(ctx).getString("card_theme", "midnight") ?: "midnight"

    fun setKey(ctx: Context, key: String) {
        YtApi.prefs(ctx).edit().putString("card_theme", key).apply()
    }

    fun current(ctx: Context): Theme =
        ALL.firstOrNull { it.key == key(ctx) } ?: ALL[0]

    fun layoutRes(key: String): Int = when (key) {
        "ocean" -> R.layout.widget_layout_ocean
        "emerald" -> R.layout.widget_layout_emerald
        "violet" -> R.layout.widget_layout_violet
        "sunset" -> R.layout.widget_layout_sunset
        "snow" -> R.layout.widget_layout_snow
        else -> R.layout.widget_layout
    }

    fun progRes(key: String): Int = when (key) {
        "ocean" -> R.drawable.prog_ocean
        "emerald" -> R.drawable.prog_emerald
        "violet" -> R.drawable.prog_violet
        "sunset" -> R.drawable.prog_sunset
        "snow" -> R.drawable.prog_snow
        else -> R.drawable.prog_midnight
    }

    fun bgRes(key: String): Int = when (key) {
        "ocean" -> R.drawable.bg_w_ocean
        "emerald" -> R.drawable.bg_w_emerald
        "violet" -> R.drawable.bg_w_violet
        "sunset" -> R.drawable.bg_w_sunset
        "snow" -> R.drawable.bg_w_snow
        else -> R.drawable.bg_w_midnight
    }
}
