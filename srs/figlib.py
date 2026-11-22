"""Small drawing toolkit for the SRS diagrams (Pillow only, no matplotlib here).

Design coordinates are in "units"; the PNG is rendered at SCALE pixels per unit,
so a 1000-unit-wide figure becomes 2000 px and prints crisply at ~6.4 in.
"""
import os

from PIL import Image, ImageDraw, ImageFilter, ImageFont

SCALE = 2.0
FONT_DIR = r"C:\Windows\Fonts"

INK = "#172B4D"
NAVY = "#1B3358"
BLUE = "#2A5BD7"
BLUE_SOFT = "#E8F0FE"
BLUE_MID = "#BFD1F5"
TEAL = "#0F766E"
TEAL_SOFT = "#DEF3F0"
AMBER = "#A15C07"
AMBER_SOFT = "#FDF1DA"
RED = "#A31B12"
RED_SOFT = "#FBE6E4"
GREY = "#5D6B82"
GREY_LIGHT = "#8493A8"
LINE = "#C6D2E4"
PANEL = "#F5F8FD"
WHITE = "#FFFFFF"

_FONT_FILES = {
    "regular": "segoeui.ttf",
    "bold": "segoeuib.ttf",
    "semibold": "seguisb.ttf",
    "light": "segoeuil.ttf",
    "italic": "segoeuii.ttf",
}
_cache = {}


def font(size, weight="regular"):
    key = (round(size, 2), weight)
    if key not in _cache:
        path = os.path.join(FONT_DIR, _FONT_FILES[weight])
        _cache[key] = ImageFont.truetype(path, max(1, int(round(size * SCALE))))
    return _cache[key]


def _rgba(color, alpha=None):
    if color is None:
        return None
    if isinstance(color, (tuple, list)):
        c = tuple(color)
        if len(c) == 3:
            c = c + (255,)
        return c if alpha is None else c[:3] + (int(alpha),)
    if color.startswith("#") and len(color) == 7:
        c = tuple(int(color[i:i + 2], 16) for i in (1, 3, 5)) + (255,)
    elif color.startswith("#") and len(color) == 9:
        c = tuple(int(color[i:i + 2], 16) for i in (1, 3, 5)) + (int(color[7:9], 16),)
    else:
        c = (0, 0, 0, 255)
    if alpha is not None:
        c = c[:3] + (int(alpha),)
    return c


class Canvas:
    def __init__(self, width, height, bg=WHITE):
        self.w, self.h = width, height
        self.img = Image.new("RGBA", (int(width * SCALE), int(height * SCALE)), _rgba(bg))
        self.d = ImageDraw.Draw(self.img)

    # ---------------------------------------------------------------- shapes
    def _box(self, x, y, w, h):
        return [x * SCALE, y * SCALE, (x + w) * SCALE, (y + h) * SCALE]

    def shadow(self, x, y, w, h, r=8, blur=5, alpha=38, dy=3):
        layer = Image.new("RGBA", self.img.size, (0, 0, 0, 0))
        d = ImageDraw.Draw(layer)
        d.rounded_rectangle(
            [(x + 1) * SCALE, (y + dy) * SCALE, (x + w + 1) * SCALE, (y + h + dy) * SCALE],
            radius=r * SCALE, fill=(23, 43, 77, alpha),
        )
        layer = layer.filter(ImageFilter.GaussianBlur(blur * SCALE / 2))
        self.img = Image.alpha_composite(self.img, layer)
        self.d = ImageDraw.Draw(self.img)

    def rect(self, x, y, w, h, r=8, fill=None, outline=None, width=1.2, shadow=False, dash=False):
        if shadow:
            self.shadow(x, y, w, h, r=r)
        box = self._box(x, y, w, h)
        if dash and outline:
            if fill:
                self.d.rounded_rectangle(box, radius=r * SCALE, fill=_rgba(fill))
            self._dash_rect(x, y, w, h, r, outline, width)
        else:
            self.d.rounded_rectangle(
                box, radius=r * SCALE, fill=_rgba(fill), outline=_rgba(outline),
                width=max(1, int(round(width * SCALE))),
            )

    def _dash_rect(self, x, y, w, h, r, color, width):
        r = min(r, w / 2, h / 2)
        self.d.line([(x + r) * SCALE, y * SCALE, (x + w - r) * SCALE, y * SCALE], fill=_rgba(color), width=max(1, int(width * SCALE)))
        self.d.line([(x + r) * SCALE, (y + h) * SCALE, (x + w - r) * SCALE, (y + h) * SCALE], fill=_rgba(color), width=max(1, int(width * SCALE)))
        self.d.line([x * SCALE, (y + r) * SCALE, x * SCALE, (y + h - r) * SCALE], fill=_rgba(color), width=max(1, int(width * SCALE)))
        self.d.line([(x + w) * SCALE, (y + r) * SCALE, (x + w) * SCALE, (y + h - r) * SCALE], fill=_rgba(color), width=max(1, int(width * SCALE)))
        for cx, cy, start in ((x + r, y + r, 180), (x + w - r, y + r, 270), (x + w - r, y + h - r, 0), (x + r, y + h - r, 90)):
            self.d.arc(
                [(cx - r) * SCALE, (cy - r) * SCALE, (cx + r) * SCALE, (cy + r) * SCALE],
                start, start + 90, fill=_rgba(color), width=max(1, int(width * SCALE)),
            )

    def gradient_rect(self, x, y, w, h, r, c1, c2, outline=None, width=1.0):
        W, H = int(w * SCALE), int(h * SCALE)
        grad = Image.new("RGB", (W, H))
        gd = ImageDraw.Draw(grad)
        for i in range(H):
            t = i / max(1, H - 1)
            gd.line([(0, i), (W, i)],
                    fill=tuple(int(c1[k] + (c2[k] - c1[k]) * t) for k in range(3)))
        mask = Image.new("L", (W, H), 0)
        ImageDraw.Draw(mask).rounded_rectangle([0, 0, W - 1, H - 1], radius=r * SCALE, fill=255)
        self.img.paste(grad, (int(x * SCALE), int(y * SCALE)), mask)
        if outline:
            self.d.rounded_rectangle(self._box(x, y, w, h), radius=r * SCALE,
                                     outline=_rgba(outline), width=max(1, int(width * SCALE)))

    def ellipse(self, x, y, w, h, fill=None, outline=None, width=1.2):
        self.d.ellipse(self._box(x, y, w, h), fill=_rgba(fill), outline=_rgba(outline), width=max(1, int(width * SCALE)))

    def polygon(self, pts, fill=None, outline=None):
        self.d.polygon([(x * SCALE, y * SCALE) for x, y in pts], fill=_rgba(fill), outline=_rgba(outline))

    def line(self, pts, color=INK, width=1.4, dash=None):
        pts = [(x * SCALE, y * SCALE) for x, y in pts]
        if dash:
            for a, b in zip(pts, pts[1:]):
                self._dashed(a, b, color, width, dash)
        else:
            self.d.line(pts, fill=_rgba(color), width=max(1, int(width * SCALE)), joint="curve")

    # ---------------------------------------------------------------- text
    def text(self, x, y, s, size=17, weight="regular", color=INK, anchor="mm", spacing=1.35):
        if "\n" in s:
            self.para(x, y, 10_000, s, size=size, weight=weight, color=color,
                      anchor=anchor, spacing=spacing)
            return
        self.d.text((x * SCALE, y * SCALE), s, font=font(size, weight), fill=_rgba(color), anchor=anchor)

    def wrap(self, s, size, weight, max_w):
        f = font(size, weight)
        out = []
        for chunk in s.split("\n"):
            words, cur = chunk.split(), ""
            for word in words:
                trial = (cur + " " + word).strip()
                if f.getlength(trial) <= max_w * SCALE or not cur:
                    cur = trial
                else:
                    out.append(cur)
                    cur = word
            out.append(cur)
        return out

    def para(self, x, y, w, s, size=15, weight="regular", color=INK, anchor="l",
             spacing=1.42, align="left"):
        f = font(size, weight)
        lines = self.wrap(s, size, weight, w)
        lh = size * spacing
        total = lh * (len(lines) - 1)
        if "m" in anchor:
            y = y - total / 2
        elif "b" in anchor:
            y = y - total
        for i, line in enumerate(lines):
            px = x
            if align == "center":
                px = x + (w - f.getlength(line) / SCALE) / 2
            elif align == "right":
                px = x + w - f.getlength(line) / SCALE
            self.d.text((px * SCALE, (y + i * lh) * SCALE), line, font=f, fill=_rgba(color), anchor="lm")
        return y + total + lh * 0.55

    def label(self, x, y, s, size=13, weight="semibold", color=GREY, bg=WHITE, pad=4, anchor="mm"):
        f = font(size, weight)
        tw, th = f.getlength(s) / SCALE, size * 1.15
        if bg:
            lx = x - tw / 2 - pad if "m" in anchor else x - pad
            self.d.rounded_rectangle(
                [lx * SCALE, (y - th / 2) * SCALE, (lx + tw + 2 * pad) * SCALE, (y + th / 2) * SCALE],
                radius=3 * SCALE, fill=_rgba(bg),
            )
        self.d.text((x * SCALE, y * SCALE), s, font=f, fill=_rgba(color), anchor=anchor)

    # ---------------------------------------------------------------- arrows
    def _dashed(self, a, b, color, width, pattern=7):
        import math
        dx, dy = b[0] - a[0], b[1] - a[1]
        dist = math.hypot(dx, dy)
        if dist == 0:
            return
        ux, uy = dx / dist, dy / dist
        on_len, off_len = (pattern, pattern * 0.75) if not isinstance(pattern, (tuple, list)) else pattern
        pos, on = 0.0, True
        while pos < dist:
            step = (on_len if on else off_len) * SCALE
            end = min(pos + step, dist)
            if on:
                self.d.line(
                    [(a[0] + ux * pos, a[1] + uy * pos), (a[0] + ux * end, a[1] + uy * end)],
                    fill=_rgba(color), width=max(1, int(width * SCALE)),
                )
            pos, on = end, not on

    def arrow(self, p1, p2, color=BLUE, width=1.5, head=7, dash=False,
              label=None, lpos=0.5, loff=(0, -13), lsize=13, lcolor=None, lw="semibold"):
        px1, py1 = p1[0] * SCALE, p1[1] * SCALE
        px2, py2 = p2[0] * SCALE, p2[1] * SCALE
        import math
        dx, dy = px2 - px1, py2 - py1
        dist = math.hypot(dx, dy)
        if dist < 1:
            return
        ux, uy = dx / dist, dy / dist
        h = head * SCALE
        back = (px2 - ux * h * 0.95, py2 - uy * h * 0.95)
        if dash:
            self._dashed((px1, py1), back, color, width)
        else:
            self.d.line([(px1, py1), back], fill=_rgba(color), width=max(1, int(width * SCALE)))
        perp = (-uy, ux)
        w = h * 0.46
        self.d.polygon(
            [(px2, py2),
             (back[0] + perp[0] * w, back[1] + perp[1] * w),
             (back[0] - perp[0] * w, back[1] - perp[1] * w)],
            fill=_rgba(color),
        )
        if label:
            lx = px1 + dx * lpos + loff[0] * SCALE
            ly = py1 + dy * lpos + loff[1] * SCALE
            self.label(lx / SCALE, ly / SCALE, label, size=lsize, weight=lw,
                       color=lcolor or color, bg=WHITE)

    def actor(self, x, y, s=1.0, color=INK, label=None, lsize=15):
        self.ellipse(x - 4.5 * s, y - 30 * s, 9 * s, 9 * s, fill=WHITE, outline=color, width=1.4)
        self.line([(x, y - 21 * s), (x, y - 4 * s)], color=color, width=1.4)
        self.line([(x - 10 * s, y - 16 * s), (x + 10 * s, y - 16 * s)], color=color, width=1.4)
        self.line([(x, y - 4 * s), (x - 9 * s, y + 10 * s)], color=color, width=1.4)
        self.line([(x, y - 4 * s), (x + 9 * s, y + 10 * s)], color=color, width=1.4)
        if label:
            self.text(x, y + 24 * s, label, size=lsize, weight="semibold", color=color)

    def save(self, path, trim=True, pad=30):
        img = self.img.convert("RGB")
        if trim:
            bg = Image.new("RGB", img.size, (255, 255, 255))
            from PIL import ImageChops
            diff = ImageChops.difference(img, bg).convert("L").point(lambda v: 255 if v > 6 else 0)
            box = diff.getbbox()
            if box:
                pad_px = int(pad * SCALE)
                box = (max(0, box[0] - pad_px), max(0, box[1] - pad_px),
                       min(img.width, box[2] + pad_px), min(img.height, box[3] + pad_px))
                img = img.crop(box)
        img.save(path, "PNG", optimize=True)
        return path
