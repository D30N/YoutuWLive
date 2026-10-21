package com.deon.ytsubslive

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object YtApi {
    private const val PREFS = "ytsubs"

    fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun apiKey(ctx: Context): String = prefs(ctx).getString("api_key", "") ?: ""

    /** The user's own key if set, otherwise the built-in default key. */
    fun effectiveKey(ctx: Context): String {
        val own = apiKey(ctx).trim()
        return if (own.isNotEmpty()) own else BuildConfig.DEFAULT_API_KEY.trim()
    }
    fun handle(ctx: Context): String =
        prefs(ctx).getString("handle", "@deepak-deon") ?: "@deepak-deon"

    data class Channel(
        val title: String,
        val subs: Long,
        val hidden: Boolean,
        val thumbUrl: String
    )

    /** Blocking network call — invoke from a background thread. Throws on HTTP/parse errors. */
    fun fetchChannel(ctx: Context): Channel {
        val key = effectiveKey(ctx).trim()
        require(key.isNotEmpty()) { "No API key" }
        var h = handle(ctx).trim()
        if (h.isEmpty()) h = "@deepak-deon"
        if (!h.startsWith("@")) h = "@$h"
        val url = "https://www.googleapis.com/youtube/v3/channels?part=snippet,statistics" +
            "&forHandle=" + URLEncoder.encode(h, "UTF-8") +
            "&key=" + URLEncoder.encode(key, "UTF-8")
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 12000
        conn.readTimeout = 12000
        val code = conn.responseCode
        val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
            ?.bufferedReader()?.readText() ?: ""
        if (code !in 200..299) {
            val reason = try {
                JSONObject(body).getJSONObject("error").getJSONArray("errors")
                    .getJSONObject(0).getString("reason")
            } catch (e: Exception) { "http$code" }
            throw IllegalStateException("API error: $reason")
        }
        val items = JSONObject(body).optJSONArray("items")
        require(items != null && items.length() > 0) { "Channel not found for $h" }
        val item = items.getJSONObject(0)
        val snippet = item.getJSONObject("snippet")
        val stats = item.getJSONObject("statistics")
        val hidden = stats.optBoolean("hiddenSubscriberCount", false)
        val subs = if (hidden) -1L else stats.optString("subscriberCount", "0").toLongOrNull() ?: 0L
        val thumb = snippet.optJSONObject("thumbnails")
            ?.optJSONObject("medium")?.optString("url")
            ?: snippet.optJSONObject("thumbnails")?.optJSONObject("default")?.optString("url")
            ?: ""
        return Channel(snippet.optString("title", h), subs, hidden, thumb)
    }

    /** Persist a fresh result and compute today's delta. Returns delta for today. */
    fun saveResult(ctx: Context, ch: Channel): Long {
        val p = prefs(ctx)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        var dayStart = p.getLong("day_start_count", -1)
        if (p.getString("day_stamp", "") != today || dayStart < 0) {
            dayStart = ch.subs
            p.edit().putString("day_stamp", today).putLong("day_start_count", dayStart).apply()
        }
        val delta = if (ch.hidden) 0 else ch.subs - dayStart
        p.edit()
            .putLong("last_count", ch.subs)
            .putLong("last_delta", delta)
            .putString("channel_title", ch.title)
            .putLong("last_fetch", System.currentTimeMillis())
            .apply()
        return delta
    }

    fun avatarFile(ctx: Context) = File(ctx.filesDir, "avatar.png")

    fun downloadAvatar(ctx: Context, url: String) {
        if (url.isEmpty()) return
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        conn.inputStream.use { input ->
            val bmp = BitmapFactory.decodeStream(input) ?: return
            avatarFile(ctx).outputStream().use { out ->
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }
    }

    fun loadAvatar(ctx: Context): Bitmap? {
        val f = avatarFile(ctx)
        return if (f.exists()) BitmapFactory.decodeFile(f.absolutePath) else null
    }

    fun circleCrop(src: android.graphics.Bitmap): android.graphics.Bitmap {
        val size = minOf(src.width, src.height)
        val out = android.graphics.Bitmap.createBitmap(
            size, size, android.graphics.Bitmap.Config.ARGB_8888
        )
        val canvas = android.graphics.Canvas(out)
        val path = android.graphics.Path()
        path.addCircle(size / 2f, size / 2f, size / 2f, android.graphics.Path.Direction.CCW)
        canvas.clipPath(path)
        val left = (src.width - size) / 2f
        val top = (src.height - size) / 2f
        canvas.drawBitmap(src, -left, -top, android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG))
        return out
    }

    fun fmt(n: Long): String = String.format(Locale.US, "%,d", n)

    fun nextMilestone(count: Long): Long {
        if (count < 100) return ((count / 10) + 1) * 10
        var step = 1L
        while (step * 10 <= count) step *= 10
        val half = step / 2
        return ((count / half) + 1) * half
    }
}
