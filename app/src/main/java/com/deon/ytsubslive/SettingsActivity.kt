package com.deon.ytsubslive

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

class SettingsActivity : Activity() {

    private lateinit var prevCard: LinearLayout
    private lateinit var prevName: TextView
    private lateinit var prevHandle: TextView
    private lateinit var prevCount: TextView
    private lateinit var prevSubs: TextView
    private lateinit var prevDelta: TextView
    private lateinit var prevMile: TextView
    private lateinit var prevLive: TextView
    private lateinit var prevBar: ProgressBar
    private lateinit var prevAvatar: ImageView
    private val radioViews = mutableListOf<TextView>()

    private fun dp(n: Int): Int = (n * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val keyInput = findViewById<EditText>(R.id.keyInput)
        val handleInput = findViewById<EditText>(R.id.handleInput)
        keyInput.setText(YtApi.apiKey(this))
        handleInput.setText(YtApi.handle(this))

        prevCard = findViewById(R.id.prevCard)
        prevName = findViewById(R.id.prevName)
        prevHandle = findViewById(R.id.prevHandle)
        prevCount = findViewById(R.id.prevCount)
        prevSubs = findViewById(R.id.prevSubs)
        prevDelta = findViewById(R.id.prevDelta)
        prevMile = findViewById(R.id.prevMile)
        prevLive = findViewById(R.id.prevLive)
        prevBar = findViewById(R.id.prevBar)
        prevAvatar = findViewById(R.id.prevAvatar)

        fillPreviewData()
        buildThemeRows()
        applyPreview(Themes.current(this))

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            var h = handleInput.text.toString().trim()
            if (h.isEmpty()) h = "@deepak-deon"
            if (!h.startsWith("@")) h = "@$h"
            YtApi.prefs(this).edit()
                .putString("api_key", keyInput.text.toString().trim())
                .putString("handle", h)
                .apply()
            SubWidget.refreshAll(this)
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fillPreviewData() {
        val p = YtApi.prefs(this)
        val count = p.getLong("last_count", -1)
        val delta = p.getLong("last_delta", 0)
        prevName.text = p.getString("channel_title", "Deon Talks")
        prevHandle.text = YtApi.handle(this) + " · YouTube"
        prevCount.text = if (count >= 0) YtApi.fmt(count) else "—"
        prevDelta.text = when {
            count < 0 -> "—"
            delta > 0 -> "+${YtApi.fmt(delta)} today"
            delta < 0 -> "${YtApi.fmt(delta)} today"
            else -> "0 today"
        }
        prevDelta.setTextColor(if (delta < 0) 0xFFF87171.toInt() else 0xFF4ADE80.toInt())
        if (count >= 0) {
            val next = YtApi.nextMilestone(count)
            val pct = if (next > 0) (count.toDouble() / next * 1000).toInt() else 0
            prevBar.progress = pct.coerceIn(0, 1000)
            prevMile.text = "${YtApi.fmt(next - count)} to go · Next ${YtApi.fmt(next)}"
        }
        YtApi.loadAvatar(this)?.let { prevAvatar.setImageBitmap(YtApi.circleCrop(it)) }
    }

    private fun applyPreview(t: Themes.Theme) {
        prevCard.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(20).toFloat()
            setColor(t.bg)
            setStroke(dp(1), if (t.key == "snow") 0xFFDCDCE6.toInt() else 0xFF2E2E3C.toInt())
        }
        prevName.setTextColor(t.text)
        prevCount.setTextColor(t.text)
        prevHandle.setTextColor(t.sub)
        prevSubs.setTextColor(t.sub)
        prevMile.setTextColor(t.sub)
        prevLive.setTextColor(t.accent)
        prevBar.progressDrawable = resources.getDrawable(Themes.progRes(t.key), null)
    }

    private fun buildThemeRows() {
        val list = findViewById<LinearLayout>(R.id.themeList)
        val selected = Themes.key(this)
        Themes.ALL.forEachIndexed { index, theme ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), dp(10), dp(12), dp(10))
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                isClickable = true
                isFocusable = true
            }
            // mini swatch: theme card colour with an accent dot-bar
            val swatch = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dp(8).toFloat()
                    setColor(theme.bg)
                    setStroke(dp(1), 0x33FFFFFF)
                }
                setPadding(dp(7), dp(5), dp(7), dp(5))
                layoutParams = LinearLayout.LayoutParams(dp(52), dp(32))
            }
            swatch.addView(View(this).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(theme.accent)
                }
                layoutParams = LinearLayout.LayoutParams(dp(9), dp(9))
            })
            swatch.addView(View(this).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dp(2).toFloat()
                    setColor(theme.text)
                }
                layoutParams = LinearLayout.LayoutParams(dp(20), dp(5)).apply {
                    marginStart = dp(6)
                }
            })
            row.addView(swatch)
            row.addView(TextView(this).apply {
                text = theme.name
                setTextColor(Color.parseColor("#F5F5FA"))
                textSize = 15f
                layoutParams = LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
                ).apply { marginStart = dp(14) }
            })
            val radio = TextView(this).apply {
                text = if (theme.key == selected) "●" else "○"
                setTextColor(
                    if (theme.key == selected) theme.accent else Color.parseColor("#6E6E80")
                )
                textSize = 17f
                setTypeface(typeface, Typeface.BOLD)
            }
            radioViews.add(radio)
            row.addView(radio)
            row.setOnClickListener {
                Themes.setKey(this, theme.key)
                refreshRadios()
                applyPreview(theme)
                SubWidget.refreshAll(this)
            }
            list.addView(row)
            if (index < Themes.ALL.size - 1) {
                list.addView(View(this).apply {
                    setBackgroundColor(Color.parseColor("#2E2E3C"))
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dp(1)
                    ).apply {
                        marginStart = dp(12)
                        marginEnd = dp(12)
                    }
                })
            }
        }
    }

    private fun refreshRadios() {
        val selected = Themes.key(this)
        Themes.ALL.forEachIndexed { i, theme ->
            radioViews[i].text = if (theme.key == selected) "●" else "○"
            radioViews[i].setTextColor(
                if (theme.key == selected) theme.accent else Color.parseColor("#6E6E80")
            )
        }
    }
}
