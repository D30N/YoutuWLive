# YoutuW Live

Live YouTube subscriber tracker for Android — in the style of InstaW Live.

Track any channel's subscriber count live: big in-app counter, today's
gain/loss, next-milestone progress, and a home-screen widget that refreshes
on its own. Tap the widget to jump straight to the channel.

## Features

- Live subscriber count via the YouTube Data API v3 (auto-refreshes every minute in-app)
- Today's delta (+/- since midnight) and next-milestone progress bar
- Home-screen widget with its own fetch — tap it to open the channel
- 6 card themes (Midnight, Ocean, Emerald, Violet, Sunset, Snow) with a live card preview in Settings
- Bring your own API key in Settings, or use the built-in default
- Works with any channel handle (default: @deepak-deon)

## Setup

Install the APK from [Releases](../../releases), open the app and it starts
tracking right away. To use your own free API key instead: Google Cloud
Console → create a project → enable "YouTube Data API v3" → Credentials →
Create API key → paste it in the app's Settings.

## Notes

YouTube reports public subscriber counts rounded (e.g. 1,590) — the app
shows exactly what the API returns.

---

Made by Deon · [@deepak.deon](https://instagram.com/deepak.deon)
