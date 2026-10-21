#!/usr/bin/env python3
"""YTSubs Live — mockup v1 (PIL). Dark UI, YouTube-red theme, same family as InstaW Live."""
from PIL import Image, ImageDraw, ImageFont
import os

W, H = 1080, 1920
RED = (255, 0, 51)
RED_DARK = (230, 33, 23)
WHITE = (245, 245, 250)
GREY = (154, 154, 173)
CARD = (23, 23, 31)
LINE = (46, 46, 60)
GREEN = (74, 222, 128)

FDIR = os.path.expanduser("~/.fonts")
ANTON = os.path.join(FDIR, "Anton-Regular.ttf")
DEJA = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
DEJA_B = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"

def f(path, size):
    return ImageFont.truetype(path, size)

def gradient(img, top, bottom):
    d = ImageDraw.Draw(img)
    for y in range(img.height):
        t = y / img.height
        c = tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(3))
        d.line([(0, y), (img.width, y)], fill=c)

def rrect(d, box, rad, **kw):
    d.rounded_rectangle(box, radius=rad, **kw)

def text_c(d, cx, y, s, font, fill):
    w = d.textbbox((0, 0), s, font=font)[2]
    d.text((cx - w / 2, y), s, font=font, fill=fill)
    return w

def text_l(d, x, y, s, font, fill):
    d.text((x, y), s, font=font, fill=fill)
    return d.textbbox((0, 0), s, font=font)[2]

def spaced_c(d, cx, y, s, font, fill, gap=10):
    widths = [d.textbbox((0, 0), ch, font=font)[2] for ch in s]
    total = sum(widths) + gap * (len(s) - 1)
    x = cx - total / 2
    for ch, w in zip(s, widths):
        d.text((x, y), ch, font=font, fill=fill)
        x += w + gap

img = Image.new("RGB", (W, H), (10, 10, 15))
gradient(img, (10, 10, 16), (18, 18, 30))
d = ImageDraw.Draw(img)

# ---------- phone frame ----------
rrect(d, (54, 54, 1026, 1340), 48, fill=(13, 13, 20), outline=LINE, width=3)
# status bar
text_l(d, 104, 92, "9:41", f(DEJA_B, 30), WHITE)
for i in range(4):
    d.rectangle((872 + i * 16, 118 - i * 6, 884 + i * 16, 120), fill=WHITE)
rrect(d, (950, 100, 990, 124), 5, outline=WHITE, width=2)
d.rectangle((954, 104, 978, 120), fill=WHITE)

# header
rrect(d, (100, 158, 176, 234), 18, fill=RED_DARK)
d.polygon([(128, 178), (128, 214), (158, 196)], fill=(255, 255, 255))
text_l(d, 200, 172, "YTSubs Live", f(DEJA_B, 46), WHITE)
for i in range(3):
    d.ellipse((958, 172 + i * 22, 972, 186 + i * 22), fill=GREY)

d.line((100, 268, 980, 268), fill=LINE, width=2)

# channel row
d.ellipse((100, 300, 212, 412), fill=RED_DARK)
text_c(d, 156, 322, "D", f(ANTON, 62), (255, 255, 255))
text_l(d, 244, 306, "Deon Talks", f(DEJA_B, 46), WHITE)
text_l(d, 244, 368, "@deepak-deon", f(DEJA, 30), GREY)
# LIVE pill
rrect(d, (806, 316, 980, 368), 26, fill=(42, 12, 14), outline=RED, width=2)
d.ellipse((830, 334, 846, 350), fill=RED)
text_l(d, 858, 328, "LIVE", f(DEJA_B, 28), RED)

# big count
text_c(d, W // 2, 452, "12,480", f(ANTON, 185), WHITE)
spaced_c(d, W // 2, 712, "SUBSCRIBERS", f(DEJA_B, 30), GREY, gap=12)

# delta pill
rrect(d, (382, 772, 698, 840), 34, fill=(14, 34, 24), outline=(31, 81, 54), width=2)
text_c(d, W // 2, 788, "+36 today", f(DEJA_B, 34), GREEN)

# milestone card
rrect(d, (100, 896, 980, 1120), 24, fill=CARD)
text_l(d, 140, 926, "Next milestone", f(DEJA, 29), GREY)
w15 = d.textbbox((0, 0), "15,000", font=f(DEJA_B, 44))[2]
text_l(d, 940 - w15, 916, "15,000", f(DEJA_B, 44), WHITE)
rrect(d, (140, 1000, 940, 1034), 17, fill=(43, 43, 56))
rrect(d, (140, 1000, 140 + int(800 * 0.83), 1034), 17, fill=RED)
text_l(d, 140, 1056, "2,520 to go  ·  83%", f(DEJA, 28), GREY)

# refresh button
rrect(d, (100, 1160, 980, 1252), 22, fill=RED)
text_c(d, W // 2, 1186, "Refresh", f(DEJA_B, 38), (255, 255, 255))

# credit inside app
text_c(d, W // 2, 1286, "This app was made by Deon  ·  @deepak.deon", f(DEJA, 23), (110, 110, 128))

# ---------- widget preview ----------
spaced_c(d, W // 2, 1420, "HOME-SCREEN WIDGET", f(DEJA_B, 27), GREY, gap=8)
rrect(d, (140, 1480, 940, 1700), 34, fill=(18, 18, 27), outline=LINE, width=2)
rrect(d, (184, 1536, 252, 1604), 14, fill=RED_DARK)
d.polygon([(208, 1554), (208, 1586), (234, 1570)], fill=(255, 255, 255))
text_l(d, 284, 1512, "12,480", f(DEJA_B, 56), WHITE)
text_l(d, 284, 1586, "Deon Talks · subscribers", f(DEJA, 28), GREY)
w36 = d.textbbox((0, 0), "+36", font=f(DEJA_B, 40))[2]
text_l(d, 900 - w36, 1524, "+36", f(DEJA_B, 40), GREEN)

text_c(d, W // 2, 1780, "Mockup · sample numbers", f(DEJA, 26), (110, 110, 128))

out = os.path.expanduser("~/workspace/your_files/YTSubsLive-mockup-v1.png")
img.save(out)
print("saved", out, img.size)
