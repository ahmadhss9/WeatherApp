# -*- coding: utf-8 -*-
"""Builds SRS.docx from the content modules and the generated figures."""
import os
import re
import sys

from docx import Document
from docx.enum.section import WD_SECTION
from docx.enum.table import WD_ALIGN_VERTICAL, WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_BREAK
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Emu, Inches, Pt, RGBColor

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)

import srs_content as C1          # noqa: E402
import srs_content2 as C2         # noqa: E402

FIG_DIR = os.path.join(HERE, "figures")
OUT = os.path.join(HERE, "SRS.docx")

NAVY = RGBColor(0x1B, 0x33, 0x58)
BLUE = RGBColor(0x2A, 0x5B, 0xD7)
INK = RGBColor(0x1F, 0x29, 0x37)
GREY = RGBColor(0x5D, 0x6B, 0x82)
LIGHT_GREY = RGBColor(0x84, 0x93, 0xA8)
WHITE = RGBColor(0xFF, 0xFF, 0xFF)

HEX_NAVY = "1B3358"
HEX_HEADER_FILL = "1B3358"
HEX_BAND = "E8EFFA"
HEX_ALT = "F7F9FD"
HEX_RULE = "C6D2E4"
HEX_CODE = "F4F6FA"

CONTENT_WIDTH_IN = 6.5

BLOCKS = (C1.INTRO + C1.OVERALL + C1.FEATURES + C2.EXTERNAL + C2.NFR + C2.DATA
          + C2.ARCH + C2.USECASES + C2.TRACE + C2.TEST + C2.PLAN + C2.FUTURE
          + C2.APPENDIX)

# --------------------------------------------------------------------------
# XML helpers
# --------------------------------------------------------------------------
PPR_ORDER = ["pStyle", "keepNext", "keepLines", "pageBreakBefore", "framePr", "widowControl",
             "numPr", "suppressLineNumbers", "pBdr", "shd", "tabs", "suppressAutoHyphens",
             "kinsoku", "wordWrap", "overflowPunct", "topLinePunct", "autoSpaceDE",
             "autoSpaceDN", "bidi", "adjustRightInd", "snapToGrid", "spacing", "ind",
             "contextualSpacing", "mirrorIndents", "suppressOverlap", "jc", "textDirection",
             "textAlignment", "textboxTightWrap", "outlineLvl", "divId", "cnfStyle", "rPr",
             "sectPr", "pPrChange"]
TBLPR_ORDER = ["tblStyle", "tblpPr", "tblOverlap", "bidiVisual", "tblStyleRowBandSize",
               "tblStyleColBandSize", "tblW", "jc", "tblCellSpacing", "tblInd", "tblBorders",
               "shd", "tblLayout", "tblCellMar", "tblLook", "tblCaption", "tblDescription"]
TRPR_ORDER = ["cnfStyle", "divId", "gridBefore", "gridAfter", "wBefore", "wAfter", "cantSplit",
              "trHeight", "tblHeader", "tblCellSpacing", "jc", "hidden"]
TCPR_ORDER = ["cnfStyle", "tcW", "gridSpan", "hMerge", "vMerge", "tcBorders", "shd", "noWrap",
              "tcMar", "textDirection", "tcFitText", "vAlign", "hideMark"]


def _local(tag):
    return tag.split("}")[-1]


def fix_order(element, order):
    if element is None:
        return
    children = list(element)
    index = {name: i for i, name in enumerate(order)}
    children.sort(key=lambda c: index.get(_local(c.tag), len(order)))
    for child in children:
        element.remove(child)
    for child in children:
        element.append(child)


def el(tag, **attrs):
    e = OxmlElement(tag)
    for key, value in attrs.items():
        e.set(qn(key), str(value))
    return e


def shade_cell(cell, hex_fill):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = el("w:shd", **{"w:val": "clear", "w:color": "auto", "w:fill": hex_fill})
    tcPr.append(shd)
    fix_order(tcPr, TCPR_ORDER)


def cell_margins(table, top=40, bottom=40, left=90, right=90):
    tblPr = table._tbl.tblPr
    mar = el("w:tblCellMar")
    for side, value in (("top", top), ("left", left), ("bottom", bottom), ("right", right)):
        mar.append(el(f"w:{side}", **{"w:w": value, "w:type": "dxa"}))
    tblPr.append(mar)
    fix_order(tblPr, TBLPR_ORDER)


def table_borders(table, color=HEX_RULE, size=4):
    tblPr = table._tbl.tblPr
    borders = el("w:tblBorders")
    for side, style in (("top", "single"), ("left", "nil"), ("bottom", "single"),
                        ("right", "nil"), ("insideH", "single"), ("insideV", "nil")):
        borders.append(el(f"w:{side}", **{"w:val": style, "w:sz": size,
                                          "w:space": "0", "w:color": color}))
    tblPr.append(borders)
    fix_order(tblPr, TBLPR_ORDER)


def para_border(paragraph, side="bottom", color=HEX_RULE, size=6, space=4):
    pPr = paragraph._p.get_or_add_pPr()
    pBdr = pPr.find(qn("w:pBdr"))
    if pBdr is None:
        pBdr = el("w:pBdr")
        pPr.append(pBdr)
    pBdr.append(el(f"w:{side}", **{"w:val": "single", "w:sz": size,
                                   "w:space": space, "w:color": color}))
    fix_order(pPr, PPR_ORDER)


def para_shading(paragraph, fill=HEX_CODE):
    pPr = paragraph._p.get_or_add_pPr()
    pPr.append(el("w:shd", **{"w:val": "clear", "w:color": "auto", "w:fill": fill}))
    fix_order(pPr, PPR_ORDER)


RPR_ORDER = ["rStyle", "rFonts", "b", "bCs", "i", "iCs", "caps", "smallCaps", "strike",
             "dstrike", "outline", "shadow", "emboss", "imprint", "noProof", "snapToGrid",
             "vanish", "webHidden", "color", "spacing", "w", "kern", "position", "sz",
             "szCs", "highlight", "u", "effect", "bdr", "shd", "fitText", "vertAlign",
             "rtl", "cs", "em", "lang", "eastAsianLayout", "specVanish", "oMath"]


def add_field(paragraph, instruction, placeholder=""):
    run = paragraph.add_run()
    node = run._r
    node.append(el("w:fldChar", **{"w:fldCharType": "begin"}))
    instr = el("w:instrText")
    instr.set(qn("xml:space"), "preserve")
    instr.text = instruction
    node.append(instr)
    node.append(el("w:fldChar", **{"w:fldCharType": "separate"}))
    text = OxmlElement("w:t")
    text.text = placeholder
    node.append(text)
    node.append(el("w:fldChar", **{"w:fldCharType": "end"}))
    return run


def add_field_block(paragraph, instruction, lines):
    paragraph.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.LEFT
    """A field whose cached result is several lines, so the page shows a real
    list even before Word refreshes the field."""
    head = paragraph.add_run()
    head._r.append(el("w:fldChar", **{"w:fldCharType": "begin"}))
    instr = el("w:instrText")
    instr.set(qn("xml:space"), "preserve")
    instr.text = instruction
    head._r.append(instr)
    head._r.append(el("w:fldChar", **{"w:fldCharType": "separate"}))
    for i, line in enumerate(lines):
        if i:
            paragraph.add_run().add_break()
        paragraph.add_run(line)
    tail = paragraph.add_run()
    tail._r.append(el("w:fldChar", **{"w:fldCharType": "end"}))


def fix_zoom(doc):
    zoom = doc.settings.element.find(qn("w:zoom"))
    if zoom is not None and zoom.get(qn("w:percent")) is None:
        zoom.set(qn("w:percent"), "100")


SETTINGS_AFTER_UPDATE = ["w:hdrShapeDefaults", "w:footnotePr", "w:endnotePr", "w:compat",
                         "w:docVars", "w:rsids", "w:mathPr", "w:themeFontLang",
                         "w:clrSchemeMapping", "w:shapeDefaults", "w:decimalSymbol",
                         "w:listSeparator"]


def update_fields_on_open(doc):
    settings = doc.settings.element
    node = el("w:updateFields", **{"w:val": "true"})
    for tag in SETTINGS_AFTER_UPDATE:
        anchor = settings.find(qn(tag))
        if anchor is not None:
            anchor.addprevious(node)
            return
    settings.append(node)


# --------------------------------------------------------------------------
# Styles
# --------------------------------------------------------------------------
BODY_FONT = "Times New Roman"


def set_fonts(style, name):
    style.font.name = name
    rpr = style.element.get_or_add_rPr()
    rfonts = rpr.find(qn("w:rFonts"))
    if rfonts is None:
        rfonts = el("w:rFonts")
        rpr.insert(0, rfonts)
    for attr in ("w:ascii", "w:hAnsi", "w:eastAsia", "w:cs"):
        rfonts.set(qn(attr), name)


def setup_styles(doc):
    normal = doc.styles["Normal"]
    set_fonts(normal, BODY_FONT)
    normal.font.size = Pt(12)
    normal.font.color.rgb = INK
    pf = normal.paragraph_format
    pf.space_after = Pt(8)
    pf.space_before = Pt(0)
    pf.line_spacing = 1.15
    pf.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY

    def heading(name, size, color, before, after, border=False):
        style = doc.styles[name]
        set_fonts(style, BODY_FONT)
        style.font.size = Pt(size)
        style.font.bold = True
        style.font.color.rgb = color
        style.font.italic = False
        fmt = style.paragraph_format
        fmt.space_before = Pt(before)
        fmt.space_after = Pt(after)
        fmt.keep_with_next = True
        fmt.line_spacing = 1.05
        if border:
            pPr = style.element.get_or_add_pPr()
            pBdr = el("w:pBdr")
            pBdr.append(el("w:bottom", **{"w:val": "single", "w:sz": 8, "w:space": 6,
                                          "w:color": HEX_RULE}))
            pPr.append(pBdr)
            fix_order(pPr, PPR_ORDER)
        return style

    h1 = heading("Heading 1", 14, NAVY, 20, 8, border=True)
    h1.paragraph_format.page_break_before = True
    h1.paragraph_format.space_before = Pt(0)
    heading("Heading 2", 12, BLUE, 14, 5)
    h3 = heading("Heading 3", 12, RGBColor(0x33, 0x47, 0x5B), 10, 3)
    h3.font.italic = True

    title = doc.styles["Title"]
    set_fonts(title, BODY_FONT)
    title.font.size = Pt(30)
    title.font.color.rgb = NAVY
    title.font.bold = True

    cap = doc.styles.add_style("FigureCaption", 1)
    cap.base_style = doc.styles["Normal"]
    set_fonts(cap, BODY_FONT)
    cap.font.size = Pt(10)
    cap.font.italic = True
    cap.font.color.rgb = GREY
    cap.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.CENTER
    cap.paragraph_format.space_before = Pt(4)
    cap.paragraph_format.space_after = Pt(14)
    cap.paragraph_format.line_spacing = 1.0

    tcap = doc.styles.add_style("TableCaption", 1)
    tcap.base_style = doc.styles["Normal"]
    set_fonts(tcap, BODY_FONT)
    tcap.font.size = Pt(10)
    tcap.font.bold = True
    tcap.font.color.rgb = NAVY
    tcap.paragraph_format.space_before = Pt(14)
    tcap.paragraph_format.space_after = Pt(4)
    tcap.paragraph_format.keep_with_next = True
    tcap.paragraph_format.line_spacing = 1.0

    tt = doc.styles.add_style("TableText", 1)
    tt.base_style = doc.styles["Normal"]
    set_fonts(tt, BODY_FONT)
    tt.font.size = Pt(10)
    tt.paragraph_format.space_before = Pt(2)
    tt.paragraph_format.space_after = Pt(2)
    tt.paragraph_format.line_spacing = 1.0
    tt.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.LEFT

    code = doc.styles.add_style("CodeBlock", 1)
    code.base_style = doc.styles["Normal"]
    code.font.name = "Consolas"
    code.font.size = Pt(8.6)
    code.font.color.rgb = RGBColor(0x1B, 0x2A, 0x3A)
    code.paragraph_format.space_before = Pt(8)
    code.paragraph_format.space_after = Pt(10)
    code.paragraph_format.line_spacing = 1.0
    code.paragraph_format.left_indent = Pt(10)
    code.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.LEFT

    fl = doc.styles.add_style("FrontLabel", 1)
    fl.base_style = doc.styles["Normal"]
    set_fonts(fl, BODY_FONT)
    fl.font.size = Pt(14)
    fl.font.bold = True
    fl.font.color.rgb = NAVY
    fl.paragraph_format.space_before = Pt(0)
    fl.paragraph_format.space_after = Pt(8)

    bullet = doc.styles["List Bullet"]
    set_fonts(bullet, BODY_FONT)
    bullet.font.size = Pt(12)
    bullet.paragraph_format.space_after = Pt(3)
    bullet.paragraph_format.line_spacing = 1.15
    bullet.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY


# --------------------------------------------------------------------------
# Cover
# --------------------------------------------------------------------------
def cover(doc):
    for _ in range(3):
        doc.add_paragraph()

    band = doc.add_table(rows=1, cols=1)
    band.autofit = False
    cell = band.cell(0, 0)
    cell.width = Inches(CONTENT_WIDTH_IN)
    band.columns[0].width = Inches(CONTENT_WIDTH_IN)
    shade_cell(cell, HEX_NAVY)
    cell.vertical_alignment = WD_ALIGN_VERTICAL.CENTER

    p = cell.paragraphs[0]
    p.paragraph_format.space_before = Pt(30)
    run = p.add_run("SOFTWARE REQUIREMENTS SPECIFICATION")
    run.font.size = Pt(9.5)
    run.font.bold = True
    run.font.color.rgb = RGBColor(0xBF, 0xD1, 0xF5)
    spacing = el("w:spacing", **{"w:val": 60})
    rpr = run._r.get_or_add_rPr()
    rpr.append(spacing)
    fix_order(rpr, RPR_ORDER)

    p2 = cell.add_paragraph()
    p2.paragraph_format.space_before = Pt(10)
    p2.paragraph_format.space_after = Pt(2)
    run = p2.add_run("WeatherApp")
    run.font.size = Pt(42)
    run.font.bold = True
    run.font.color.rgb = WHITE

    p3 = cell.add_paragraph()
    p3.paragraph_format.space_before = Pt(2)
    p3.paragraph_format.space_after = Pt(8)
    run = p3.add_run("Android weather application for current conditions and forecast")
    run.font.size = Pt(12.5)
    run.font.color.rgb = RGBColor(0xD7, 0xE7, 0xFF)

    p4 = cell.add_paragraph()
    p4.paragraph_format.space_before = Pt(4)
    p4.paragraph_format.space_after = Pt(34)
    run = p4.add_run("Release 1.0  ·  Java 11  ·  API 24–37")
    run.font.size = Pt(9.5)
    run.font.color.rgb = RGBColor(0xA9, 0xC0, 0xE8)

    doc.add_paragraph()

    meta = [
        ("Document", "Software Requirements Specification (SRS)"),
        ("Version", "1.0 — draft for review"),
        ("Date", C1.DATE),
        ("Author", "Ahmad Hassan"),
        ("Project", "WeatherApp — Android application"),
        ("Repository", "github.com/ahmadhss9/WeatherApp"),
        ("Baseline", "Commit e0b1c03, branch main"),
    ]
    table = doc.add_table(rows=0, cols=2)
    table.autofit = False
    for label, value in meta:
        row = table.add_row().cells
        row[0].width = Inches(1.55)
        row[1].width = Inches(4.95)
        p = row[0].paragraphs[0]
        run = p.add_run(label.upper())
        run.font.size = Pt(8)
        run.font.bold = True
        run.font.color.rgb = LIGHT_GREY
        p = row[1].paragraphs[0]
        run = p.add_run(value)
        run.font.size = Pt(10.5)
        run.font.color.rgb = INK
    for row in table.rows:
        for i, cell in enumerate(row.cells):
            cell.width = Inches(1.55 if i == 0 else 4.95)
    tblPr = table._tbl.tblPr
    borders = el("w:tblBorders")
    for side in ("top", "left", "bottom", "right", "insideH", "insideV"):
        borders.append(el(f"w:{side}", **{"w:val": "nil"}))
    tblPr.append(borders)
    fix_order(tblPr, TBLPR_ORDER)

    for _ in range(6):
        doc.add_paragraph()

    rule = doc.add_paragraph()
    para_border(rule, "top", HEX_RULE, 6, 2)
    note = doc.add_paragraph()
    note.paragraph_format.space_before = Pt(6)
    run = note.add_run("Prepared as the requirements baseline for the WeatherApp project. "
                       "This document is controlled: the current version is the one held in the "
                       "project repository.")
    run.font.size = Pt(8.5)
    run.font.color.rgb = LIGHT_GREY
    run.font.italic = True


# --------------------------------------------------------------------------
# Front matter
# --------------------------------------------------------------------------
def front_matter(doc):
    doc.add_page_break()
    doc.add_paragraph("Document control", style="FrontLabel")
    p = doc.add_paragraph()
    run = p.add_run("Revision history")
    run.font.bold = True
    run.font.size = Pt(10.5)
    run.font.color.rgb = NAVY
    p.paragraph_format.space_before = Pt(6)
    p.paragraph_format.space_after = Pt(4)

    rows = [["0.1", "18 Nov 2026", "Ahmad Hassan", "Outline, scope and feature list derived from the project note"],
            ["0.2", "20 Nov 2026", "Ahmad Hassan", "Functional and non-functional requirements added; API contract drafted"],
            ["1.0", C1.DATE, "Ahmad Hassan", "First complete issue: diagrams, traceability, test plan and risks"]]
    add_table(doc, ["Version", "Date", "Author", "Summary of change"], rows, [0.8, 1.2, 1.5, 3.0])

    p = doc.add_paragraph()
    run = p.add_run("Approval and distribution")
    run.font.bold = True
    run.font.size = Pt(10.5)
    run.font.color.rgb = NAVY
    p.paragraph_format.space_before = Pt(16)
    p.paragraph_format.space_after = Pt(4)
    add_table(doc, ["Role", "Name", "Signature", "Date"],
              [["Author", "Ahmad Hassan", "", ""],
               ["Reviewer", "", "", ""],
               ["Accepted by", "", "", ""]],
              [1.7, 2.0, 1.8, 1.0])

    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(16)
    run = p.add_run("This document supersedes no earlier issue. Changes after the first review are "
                    "recorded above by version, and requirement identifiers are never reused for a "
                    "different requirement.")
    run.font.size = Pt(9.5)
    run.font.color.rgb = GREY

    # contents
    doc.add_page_break()
    doc.add_paragraph("Contents", style="FrontLabel")
    outline = [resolve(b[1]) for b in BLOCKS if b[0] in ("h1", "h1c", "h2")]
    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(0)
    add_field_block(p, 'TOC \\o "1-3" \\h \\z \\u', outline)
    for run in p.runs:
        run.font.size = Pt(10.5)

    # list of figures and tables
    doc.add_page_break()
    doc.add_paragraph("Figures and tables", style="FrontLabel")
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(2)
    run = p.add_run("List of figures")
    run.font.bold = True
    run.font.size = Pt(10.5)
    run.font.color.rgb = NAVY
    p.paragraph_format.space_after = Pt(4)
    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(0)
    figs = [(b[2]) for b in BLOCKS if b[0] == "fig"]
    add_field_block(p, 'TOC \\h \\z \\c "Figure"',
                    [f"Figure {i + 1}  —  {resolve(f)}" for i, f in enumerate(figs)])
    for run in p.runs:
        run.font.size = Pt(10.5)

    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(20)
    run = p.add_run("List of tables")
    run.font.bold = True
    run.font.size = Pt(10.5)
    run.font.color.rgb = NAVY
    p.paragraph_format.space_after = Pt(4)
    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(0)
    tabs = [b[1] for b in BLOCKS if b[0] == "table"]
    add_field_block(p, 'TOC \\h \\z \\c "Table"',
                    [f"Table {i + 1}  —  {resolve(c)}" for i, c in enumerate(tabs)])
    for run in p.runs:
        run.font.size = Pt(10.5)


# --------------------------------------------------------------------------
# Rendering
# --------------------------------------------------------------------------
TOKEN = re.compile(r"\{([tf]):([A-Za-z0-9_]+)\}")


def number_blocks():
    tables, figs = {}, {}
    t_n = f_n = 0
    for block in BLOCKS:
        if block[0] == "table":
            t_n += 1
            if len(block) >= 6:
                tables[block[5]] = t_n
        elif block[0] == "fig":
            f_n += 1
            if len(block) >= 5:
                figs[block[4]] = f_n
    return tables, figs


TABLES, FIGS = number_blocks()


def resolve(text):
    def sub(match):
        kind, key = match.group(1), match.group(2)
        try:
            return str(TABLES[key] if kind == "t" else FIGS[key])
        except KeyError:
            raise KeyError(f"unknown cross-reference {kind}:{key}")
    return TOKEN.sub(sub, text)


def add_paragraph(doc, text, style=None):
    p = doc.add_paragraph(style=style)
    p.add_run(resolve(text))
    return p


def add_bullets(doc, items, numbered=False):
    style = "List Number" if numbered else "List Bullet"
    for item in items:
        p = doc.add_paragraph(style=style)
        p.add_run(resolve(item))


def add_caption(doc, label, number, text, style):
    p = doc.add_paragraph(style=style)
    p.add_run(f"{label} ")
    add_field(p, f"SEQ {label} \\* ARABIC", str(number))
    p.add_run(f" \u2014 {text}")
    return p


def add_table(doc, headers, rows, widths, caption=None, number=None):
    if caption:
        add_caption(doc, "Table", number, resolve(caption), "TableCaption")

    total = sum(widths)
    widths = [w * CONTENT_WIDTH_IN / total for w in widths]

    table = doc.add_table(rows=1, cols=len(headers))
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    table_borders(table)
    cell_margins(table)

    header_cells = table.rows[0].cells
    trPr = table.rows[0]._tr.get_or_add_trPr()
    trPr.append(el("w:cantSplit"))
    trPr.append(el("w:tblHeader"))
    fix_order(trPr, TRPR_ORDER)
    for i, head in enumerate(headers):
        cell = header_cells[i]
        cell.text = ""
        shade_cell(cell, HEX_HEADER_FILL)
        cell.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
        p = cell.paragraphs[0]
        p.style = doc.styles["TableText"]
        run = p.add_run(head)
        run.font.bold = True
        run.font.color.rgb = WHITE
        run.font.size = Pt(10)

    for index, row in enumerate(rows):
        is_group = len(headers) > 1 and all(str(v).strip() == "" for v in row[1:])
        cells = table.add_row().cells
        trPr = table.rows[-1]._tr.get_or_add_trPr()
        trPr.append(el("w:cantSplit"))
        fix_order(trPr, TRPR_ORDER)
        if is_group:
            merged = cells[0].merge(cells[-1])
            merged.text = ""
            shade_cell(merged, HEX_BAND)
            p = merged.paragraphs[0]
            p.style = doc.styles["TableText"]
            p.paragraph_format.space_before = Pt(5)
            p.paragraph_format.space_after = Pt(2)
            run = p.add_run(str(row[0]))
            run.font.bold = True
            run.font.color.rgb = RGBColor(0x17, 0x3A, 0x6B)
            continue
        for i, value in enumerate(row):
            cell = cells[i]
            cell.text = ""
            if index % 2 == 1:
                shade_cell(cell, HEX_ALT)
            p = cell.paragraphs[0]
            p.style = doc.styles["TableText"]
            run = p.add_run(resolve(str(value)))
            if i == 0 and len(str(value)) <= 8:
                run.font.bold = True
                run.font.color.rgb = NAVY

    for i, width in enumerate(widths):
        table.columns[i].width = Inches(width)
        for cell in table.columns[i].cells:
            cell.width = Inches(width)

    spacer = doc.add_paragraph()
    spacer.paragraph_format.space_before = Pt(0)
    spacer.paragraph_format.space_after = Pt(2)
    spacer.paragraph_format.line_spacing = 1.0
    for run in spacer.runs:
        run.font.size = Pt(4)
    return table


def add_figure(doc, filename, caption, width, number):
    from PIL import Image
    path = os.path.join(FIG_DIR, filename)
    w, h = Image.open(path).size
    height = width * h / w
    if height > 7.3:
        width = width * 7.3 / height
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(10)
    p.paragraph_format.space_after = Pt(2)
    p.paragraph_format.keep_with_next = True
    run = p.add_run()
    run.add_picture(path, width=Inches(width))
    spPr = run._r.find(qn("w:drawing")).find(qn("wp:inline")).find(
        qn("a:graphic")).find(qn("a:graphicData")).find(qn("pic:pic")).find(qn("pic:spPr"))
    if spPr is not None and spPr.find(qn("a:ln")) is None:
        line = OxmlElement("a:ln")
        line.append(OxmlElement("a:noFill"))
        spPr.append(line)
    add_caption(doc, "Figure", number, resolve(caption), "FigureCaption")


def add_code(doc, text):
    p = doc.add_paragraph(style="CodeBlock")
    p.paragraph_format.keep_together = True
    para_shading(p, HEX_CODE)
    para_border(p, "left", "2A5BD7", 12, 6)
    lines = text.split("\n")
    for i, line in enumerate(lines):
        run = p.add_run(line)
        if i < len(lines) - 1:
            run.add_break()
    return p


def render(doc):
    t_n = f_n = 0
    for block in BLOCKS:
        kind = block[0]
        if kind in ("h1", "h1c"):
            hp = doc.add_paragraph(resolve(block[1]), style="Heading 1")
            if kind == "h1c":
                hp.paragraph_format.page_break_before = False
        elif kind == "h2":
            doc.add_paragraph(resolve(block[1]), style="Heading 2")
        elif kind == "h3":
            doc.add_paragraph(resolve(block[1]), style="Heading 3")
        elif kind == "p":
            add_paragraph(doc, block[1])
        elif kind == "bullets":
            add_bullets(doc, block[1])
        elif kind == "numbered":
            add_bullets(doc, block[1], numbered=True)
        elif kind == "table":
            t_n += 1
            add_table(doc, block[2], block[3], block[4], block[1], t_n)
        elif kind == "fig":
            f_n += 1
            add_figure(doc, block[1], block[2], block[3], f_n)
        elif kind == "code":
            add_code(doc, block[1])
        elif kind == "pagebreak":
            doc.add_page_break()
        else:
            raise ValueError(f"unknown block {kind}")


# --------------------------------------------------------------------------
# Header / footer
# --------------------------------------------------------------------------
def furniture(doc):
    section = doc.sections[0]
    section.page_width = Cm(21.0)
    section.page_height = Cm(29.7)
    section.top_margin = Cm(2.3)
    section.bottom_margin = Cm(2.1)
    section.left_margin = Cm(2.2)
    section.right_margin = Cm(2.2)
    section.header_distance = Cm(1.1)
    section.footer_distance = Cm(1.1)
    section.different_first_page_header_footer = True

    header = section.header
    p = header.paragraphs[0]
    p.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    run = p.add_run("WeatherApp  ·  Software Requirements Specification")
    run.font.size = Pt(8)
    run.font.color.rgb = LIGHT_GREY
    para_border(p, "bottom", HEX_RULE, 4, 4)

    footer = section.footer
    p = footer.paragraphs[0]
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run("SRS v1.0  ·  22 November 2026  ·  Page ")
    run.font.size = Pt(8.5)
    run.font.color.rgb = GREY
    add_field(p, "PAGE", "1")
    run = p.add_run(" of ")
    run.font.size = Pt(8.5)
    run.font.color.rgb = GREY
    add_field(p, "NUMPAGES", "1")
    for r in p.runs:
        r.font.size = Pt(8.5)
        r.font.color.rgb = GREY


# --------------------------------------------------------------------------
def main():
    doc = Document()
    setup_styles(doc)
    furniture(doc)
    cover(doc)
    front_matter(doc)
    render(doc)
    fix_zoom(doc)
    update_fields_on_open(doc)

    props = doc.core_properties
    props.title = "WeatherApp — Software Requirements Specification"
    props.subject = "Requirements for the Android weather application, release 1.0"
    props.author = "Ahmad Hassan"
    props.keywords = "SRS, requirements, Android, weather, Open-Meteo, Retrofit"
    props.comments = "Generated from the project baseline at commit e0b1c03"

    doc.save(OUT)
    print(f"wrote {OUT}")


if __name__ == "__main__":
    main()
