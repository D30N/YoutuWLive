package com.deon.ytsubslive

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import kotlin.concurrent.thread

class SubWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val pending = goAsync()
        thread {
            try {
                if (YtApi.effectiveKey(context).isNotBlank()) {
                    val ch = YtApi.fetchChannel(context)
                    YtApi.saveResult(context, ch)
                    try { YtApi.downloadAvatar(context, ch.thumbUrl) } catch (e: Exception) { }
                }
            } catch (e: Exception) {
                // keep showing cached values
            } finally {
                for (id in ids) manager.updateAppWidget(id, buildViews(context))
                pending.finish()
            }
        }
    }

    companion object {
        fun buildViews(context: Context): RemoteViews {
            val p = YtApi.prefs(context)
            val theme = Themes.current(context)
            val views = RemoteViews(context.packageName, Themes.layoutRes(theme.key))
            val count = p.getLong("last_count", -1)
            val delta = p.getLong("last_delta", 0)
            val title = p.getString("channel_title", "Deon Talks") ?: "Deon Talks"

            views.setTextViewText(R.id.widgetName, title)
            views.setTextViewText(
                R.id.widgetHandle,
                YtApi.handle(context) + " · YouTube"
            )
            views.setTextViewText(
                R.id.widgetCount,
                if (count >= 0) YtApi.fmt(count) else "—"
            )
            views.setTextViewText(
                R.id.widgetDelta,
                when {
                    count < 0 -> "—"
                    delta > 0 -> "+${YtApi.fmt(delta)} today"
                    delta < 0 -> "${YtApi.fmt(delta)} today"
                    else -> "0 today"
                }
            )
            views.setTextColor(
                R.id.widgetDelta,
                if (delta < 0) 0xFFF87171.toInt() else 0xFF4ADE80.toInt()
            )
            views.setTextColor(R.id.widgetName, theme.text)
            views.setTextColor(R.id.widgetCount, theme.text)
            views.setTextColor(R.id.widgetHandle, theme.sub)
            views.setTextColor(R.id.widgetSubs, theme.sub)
            views.setTextColor(R.id.widgetMile, theme.sub)
            views.setTextColor(R.id.widgetLive, theme.accent)
            if (count >= 0) {
                val next = YtApi.nextMilestone(count)
                val pct = if (next > 0) (count.toDouble() / next * 1000).toInt() else 0
                views.setProgressBar(R.id.widgetBar, 1000, pct.coerceIn(0, 1000), false)
                views.setTextViewText(
                    R.id.widgetMile,
                    "${YtApi.fmt(next - count)} to go · Next ${YtApi.fmt(next)}"
                )
            }
            YtApi.loadAvatar(context)?.let {
                views.setImageViewBitmap(R.id.widgetAvatar, YtApi.circleCrop(it))
                views.setViewVisibility(R.id.widgetAvatar, View.VISIBLE)
            }
            // Tapping the widget opens the YouTube channel itself.
            val channelUrl = "https://www.youtube.com/" + YtApi.handle(context)
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(channelUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val pi = PendingIntent.getActivity(
                context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetRoot, pi)
            return views
        }

        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, SubWidget::class.java))
            for (id in ids) manager.updateAppWidget(id, buildViews(context))
        }
    }
}
