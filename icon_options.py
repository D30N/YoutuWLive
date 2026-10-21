#!/usr/bin/env python3
"""Render 6 launcher-icon concepts for YoutuW Live as a numbered sheet."""
from PIL import Image, ImageDraw, ImageFont, ImageFilter
import os

S = 512  # icon render size
ANTON = os.path.expanduser("~/.fonts/Anton-Regular.ttf")
SANS = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
SANSB = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"

RED = (230, 33, 23)
RED_D = (183, 28, 28)
WHITE = (255, 255, 255)
BLACK = (13, 13, 20)


def rounded(size, radius, color):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([0, 0, size - 1, size - 1], radius=radius, fill=color)
    return img


def vgrad(size, radius, top, bottom):
    g = Image.new("RGB", (1, size))
    for y in range(size):
        t = y / max(1, size - 1)
        g.putpixel((0, y), tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(3)))
    g = g.resize((size, size)).convert("RGBA")
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, size - 1, size - 1], radius=radius, fill=255)
    g.putalpha(mask)
    return g


def play(d, cx, cy, w, h, color):
    pts = [(cx - w / 2 + w * 0.08, cy - h / 2), (cx - w / 2 + w * 0.08, cy + h / 2), (cx + w / 2, cy)]
    d.polygon(pts, fill=color)


def arcs(d, cx, cy, radii, color, width=13, start=-78, end=18):
    for r in radii:
        d.arc([cx - r, cy - r, cx + r, cy + r], start=start, end=end, fill=color, width=width)


R = 112

def icon1():
    img = vgrad(S, R, (255, 45, 45), (198, 20, 20))
    d = ImageDraw.Draw(img)
    play(d, S * 0.47, S * 0.52, 190, 220, WHITE)
    d.ellipse([S * 0.735, S * 0.185, S * 0.795, S * 0.245], fill=WHITE)
    arcs(d, S * 0.765, S * 0.215, [48, 86], WHITE, 15)
    return img

def icon2():
    img = rounded(S, R, (14, 14, 22))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([10, 10, S - 11, S - 11], radius=R - 8, outline=(255, 0, 51), width=7)
    play(d, S * 0.47, S * 0.46, 175, 205, (255, 0, 51))
    f = ImageFont.truetype(ANTON, 74)
    d.text((S * 0.5, S * 0.79), "LIVE", font=f, fill=WHITE, anchor="mm")
    d.ellipse([S * 0.315, S * 0.755, S * 0.365, S * 0.805], fill=(255, 0, 51))
    return img

def icon3():
    img = vgrad(S, R, (255, 45, 45), (198, 20, 20))
    d = ImageDraw.Draw(img)
    f = ImageFont.truetype(ANTON, 330)
    d.text((S * 0.46, S * 0.52), "W", font=f, fill=WHITE, anchor="mm")
    # badge
    bx, by, br = S * 0.76, S * 0.76, 74
    d.ellipse([bx - br, by - br, bx + br, by + br], fill=WHITE)
    play(d, bx + 4, by, 62, 74, RED)
    return img

def icon4():
    img = rounded(S, R, (250, 250, 252))
    d = ImageDraw.Draw(img)
    # youtube-style red rounded rect with white play
    d.rounded_rectangle([S * 0.16, S * 0.27, S * 0.84, S * 0.73], radius=64, fill=RED)
    play(d, S * 0.52, S * 0.5, 130, 155, WHITE)
    d.ellipse([S * 0.745, S * 0.145, S * 0.795, S * 0.195], fill=RED)
    arcs(d, S * 0.77, S * 0.17, [42, 78], RED, 13)
    return img

def icon5():
    img = rounded(S, R, (10, 14, 28))
    d = ImageDraw.Draw(img, "RGBA")
    # glow
    glow = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    for r, a in [(190, 40), (165, 60), (142, 85)]:
        gd.ellipse([S / 2 - r, S / 2 - r, S / 2 + r, S / 2 + r], fill=(255, 0, 51, a))
    glow = glow.filter(ImageFilter.GaussianBlur(18))
    img.alpha_composite(glow)
    d = ImageDraw.Draw(img)
    r = 118
    d.ellipse([S / 2 - r, S / 2 - r, S / 2 + r, S / 2 + r], fill=(230, 25, 40))
    play(d, S * 0.52, S * 0.5, 105, 125, WHITE)
    arcs(d, S * 0.115, S * 0.5, [42, 76], WHITE, 12, start=-55, end=55)
    arcs(d, S * 0.885, S * 0.5, [42, 76], WHITE, 12, start=125, end=235)
    return img

def icon6():
    img = vgrad(S, R, (255, 45, 45), (198, 20, 20))
    d = ImageDraw.Draw(img)
    play(d, S * 0.33, S * 0.44, 130, 152, WHITE)
    # ascending tracker bars
    base = S * 0.78
    for i, h in enumerate([52, 88, 128]):
        x = S * (0.60 + i * 0.115)
        d.rounded_rectangle([x, base - h, x + 34, base], radius=10, fill=WHITE)
    return img


CONCEPTS = [
    ("1", "Classic Live", icon1),
    ("2", "Midnight Live", icon2),
    ("3", "W Badge", icon3),
    ("4", "Clean White", icon4),
    ("5", "Broadcast", icon5),
    ("6", "Tracker", icon6),
]

cols, rows = 3, 2
icon_disp = 400
cell_w, cell_h = 460, 540
sheet = Image.new("RGB", (cols * cell_w + 40, rows * cell_h + 40), (20, 20, 28))
sd = ImageDraw.Draw(sheet)
fnum = ImageFont.truetype(ANTON, 54)
fname = ImageFont.truetype(SANSB if os.path.exists(SANSB) else SANS, 30)

for i, (num, name, fn) in enumerate(CONCEPTS):
    cx = 20 + (i % cols) * cell_w
    cy = 20 + (i // cols) * cell_h
    ic = fn().resize((icon_disp, icon_disp), Image.LANCZOS)
    sheet.paste(ic, (cx + (cell_w - icon_disp) // 2, cy), ic)
    sd.text((cx + 34, cy + icon_disp + 32), num, font=fnum, fill=(255, 0, 51))
    sd.text((cx + 92, cy + icon_disp + 44), name, font=fname, fill=(235, 235, 245))

out = os.path.expanduser("~/workspace/your_files/YoutuW-icon-options-v2.png")
sheet.save(out)
print("saved", out, sheet.size)
