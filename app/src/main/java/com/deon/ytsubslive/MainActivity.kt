package com.deon.ytsubslive

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private lateinit var countText: TextView
    private lateinit var deltaText: TextView
    private lateinit var nameText: TextView
    private lateinit var handleText: TextView
    private lateinit var avatar: ImageView
    private lateinit var mileValue: TextView
    private lateinit var mileBar: ProgressBar
    private lateinit var mileLeft: TextView
    private lateinit var statusText: TextView
    private lateinit var setupCard: LinearLayout
    private lateinit var mileCard: LinearLayout
    private lateinit var mileLabel: TextView
    private lateinit var livePill: TextView
    private lateinit var btnRefresh: Button

    private val handler = Handler(Looper.getMainLooper())
    private var fetching = false
    private val autoRefresh = object : Runnable {
        override fun run() {
            refresh()
            handler.postDelayed(this, 60_000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        countText = findViewById(R.id.countText)
        deltaText = findViewById(R.id.deltaText)
        nameText = findViewById(R.id.channelName)
        handleText = findViewById(R.id.channelHandle)
        avatar = findViewById(R.id.avatar)
        mileValue = findViewById(R.id.mileValue)
        mileBar = findViewById(R.id.mileBar)
        mileLeft = findViewById(R.id.mileLeft)
        statusText = findViewById(R.id.statusText)
        setupCard = findViewById(R.id.setupCard)
        mileCard = findViewById(R.id.mileCard)
        mileLabel = findViewById(R.id.mileLabel)
        livePill = findViewById(R.id.livePill)
        btnRefresh = findViewById(R.id.btnRefresh)

        try {
            countText.typeface = Typeface.createFromAsset(assets, "fonts/Anton-Regular.ttf")
        } catch (e: Exception) { /* fall back to default bold sans */ }

        val openSettings = View.OnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener(openSettings)
        findViewById<Button>(R.id.btnSetup).setOnClickListener(openSettings)
        btnRefresh.setOnClickListener { refresh() }

        applyTheme()
        showCached()
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
        handleText.text = YtApi.handle(this)
        handler.removeCallbacks(autoRefresh)
        handler.post(autoRefresh)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(autoRefresh)
    }

    private fun applyTheme() {
        val t = Themes.current(this)
        fun cardBg() = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            cornerRadius = 16 * resources.displayMetrics.density
            setColor(t.bg)
        }
        mileCard.background = cardBg()
        setupCard.background = cardBg()
        mileBar.progressDrawable = resources.getDrawable(Themes.progRes(t.key), null)
        mileValue.setTextColor(t.text)
        mileLabel.setTextColor(t.sub)
        mileLeft.setTextColor(t.sub)
        livePill.setTextColor(t.accent)
        btnRefresh.backgroundTintList =
            android.content.res.ColorStateList.valueOf(t.accent)
    }

    private fun showCached() {
        val p = YtApi.prefs(this)
        val count = p.getLong("last_count", -1)
        nameText.text = p.getString("channel_title", "Deon Talks")
        handleText.text = YtApi.handle(this)
        YtApi.loadAvatar(this)?.let { avatar.setImageBitmap(circleCrop(it)) }
        if (count >= 0) bindCount(count, p.getLong("last_delta", 0))
        setupCard.visibility = if (YtApi.effectiveKey(this).isBlank()) View.VISIBLE else View.GONE
    }

    private fun bindCount(count: Long, delta: Long) {
        countText.text = YtApi.fmt(count)
        deltaText.text = when {
            delta > 0 -> "+${YtApi.fmt(delta)} today"
            delta < 0 -> "${YtApi.fmt(delta)} today"
            else -> "0 today"
        }
        deltaText.setTextColor(
            if (delta < 0) 0xFFF87171.toInt() else 0xFF4ADE80.toInt()
        )
        val next = YtApi.nextMilestone(count)
        mileValue.text = YtApi.fmt(next)
        val pct = if (next > 0) (count.toDouble() / next * 1000).toInt() else 0
        mileBar.progress = pct.coerceIn(0, 1000)
        mileLeft.text = "${YtApi.fmt(next - count)} to go · ${pct / 10}%"
    }

    private fun refresh() {
        if (fetching) return
        if (YtApi.effectiveKey(this).isBlank()) {
            setupCard.visibility = View.VISIBLE
            statusText.text = "Add your API key in Settings to start"
            return
        }
        setupCard.visibility = View.GONE
        fetching = true
        statusText.text = "Updating…"
        thread {
            try {
                val ch = YtApi.fetchChannel(this)
                val delta = YtApi.saveResult(this, ch)
                try { YtApi.downloadAvatar(this, ch.thumbUrl) } catch (e: Exception) { }
                runOnUiThread {
                    fetching = false
                    nameText.text = ch.title
                    if (ch.hidden) {
                        countText.text = "Hidden"
                        deltaText.text = "Subscriber count is hidden"
                        statusText.text = "This channel hides its subscriber count"
                    } else {
                        bindCount(ch.subs, delta)
                        val time = SimpleDateFormat("h:mm a", Locale.US).format(Date())
                        statusText.text = "Updated $time · auto-refreshes every minute"
                    }
                    YtApi.loadAvatar(this)?.let { avatar.setImageBitmap(circleCrop(it)) }
                    SubWidget.refreshAll(this)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    fetching = false
                    statusText.text = friendlyError(e)
                }
            }
        }
    }

    private fun friendlyError(e: Exception): String {
        val msg = e.message ?: ""
        return when {
            msg.contains("keyInvalid") || msg.contains("API_KEY_INVALID") ->
                "API key invalid — check Settings"
            msg.contains("quota") ->
                "Daily API quota over — try again tomorrow"
            msg.contains("Channel not found") ->
                "Channel not found — check the handle in Settings"
            msg.contains("No API key") ->
                "Add your API key in Settings to start"
            msg.contains("Unable to resolve host") || msg.contains("timeout", true) ->
                "No internet connection"
            else -> "Couldn't update — $msg"
        }
    }

    private fun circleCrop(src: Bitmap): Bitmap {
        val size = minOf(src.width, src.height)
        val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val path = Path()
        path.addCircle(size / 2f, size / 2f, size / 2f, Path.Direction.CCW)
        canvas.clipPath(path)
        val left = (src.width - size) / 2f
        val top = (src.height - size) / 2f
        canvas.drawBitmap(src, -left, -top, Paint(Paint.ANTI_ALIAS_FLAG))
        return out
    }
}
