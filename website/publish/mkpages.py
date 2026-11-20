#!/usr/bin/env python3
"""Fill the generated sections of the baked website.

documentation.html: <!--releases--> (latest GitHub release) and <!--development--> (devel/ built from
the development branch); download.html: <!--downloads--> (latest GitHub release).

  mkpages.py digest RELEASES_JSON
      print a digest of the release list that only changes when releases or their assets change
  mkpages.py render RELEASES_JSON SITE_DIR DEV_COMMIT DEV_DATE
      replace the placeholders in SITE_DIR (devel/ is read from SITE_DIR/devel)
"""
import hashlib
import html
import json
import os
import re
import sys

RELEASES_URL = "https://github.com/mgwerder/messageVortex/releases"
DRAFT = re.compile(r"^(draft-[a-z0-9-]+-(\d\d))\.([a-z]+)$")

DESCRIPTIONS = [
    (re.compile(r"-all\.jar$"), "Core library including all dependencies (runnable)"),
    (re.compile(r"-installer\.jar$"), "Installer package"),
    (re.compile(r"-javadoc\.jar$"), "API documentation (javadoc)"),
    (re.compile(r"\.jar$"), "Core library"),
    (re.compile(r"-adminGuide\.pdf$"), "Administration guide"),
    (re.compile(r"^messageVortex_attachdocs\.pdf$"), "Thesis including all attached documents"),
    (re.compile(r"^messageVortex\.pdf$"), "Thesis"),
    (re.compile(r"(definition|_definition)\.asn$"), "ASN.1 definition of the MessageVortex messages"),
    (re.compile(r"\.zip$"), "ASN.1 definition (zip)"),
]


def describe(name):
    for pattern, text in DESCRIPTIONS:
        if pattern.search(name):
            return text
    return ""


def size_str(size):
    for unit in ("B", "KB", "MB", "GB"):
        if size < 1024 or unit == "GB":
            return f"{size:.0f} {unit}" if unit == "B" else f"{size:.1f} {unit}"
        size /= 1024.0
    return ""


def table(files):
    """files: list of (name, url, size). RFC drafts are grouped into one row per draft version."""
    drafts, rows = {}, []
    for name, url, size in files:
        m = DRAFT.match(name)
        if m:
            drafts.setdefault(m.group(1), []).append((m.group(3), url))
        else:
            rows.append((name, url, size))
    out = ["<table class=\"table table-sm\">",
           "<tr><th>File</th><th>Description</th><th>Size</th></tr>"]
    for name, url, size in sorted(rows, key=lambda r: r[0].lower()):
        out.append(f"<tr><td><a href=\"{html.escape(url)}\">{html.escape(name)}</a></td>"
                   f"<td>{describe(name)}</td><td>{size_str(size) if size is not None else ''}</td></tr>")
    for key in sorted(drafts, reverse=True):
        links = "&nbsp; ".join(f"<a href=\"{html.escape(u)}\">{e}</a>" for e, u in sorted(drafts[key]))
        out.append(f"<tr><td>{html.escape(key)}</td><td>RFC draft: {links}</td><td></td></tr>")
    out.append("</table>")
    return "\n".join(out)


def load_releases(path):
    with open(path, encoding="utf-8") as f:
        return [r for r in json.load(f) if not r.get("draft")]


def latest(releases, prerelease):
    for r in sorted(releases, key=lambda r: r.get("published_at") or "", reverse=True):
        if bool(r.get("prerelease")) == prerelease:
            return r
    return None


def release_section(releases):
    stable, pre = latest(releases, False), latest(releases, True)
    parts = []
    if stable:
        files = [(a["name"], a["browser_download_url"], a["size"]) for a in stable["assets"]]
        parts.append(f"<p>Latest release: <a href=\"{html.escape(stable['html_url'])}\">"
                     f"{html.escape(stable['name'] or stable['tag_name'])}</a> "
                     f"({html.escape((stable.get('published_at') or '')[:10])})</p>")
        parts.append(table(files))
    else:
        parts.append("<p>There is no stable release yet.</p>")
    if pre and (not stable or (pre.get("published_at") or "") > (stable.get("published_at") or "")):
        parts.append(f"<p>Latest pre-release: <a href=\"{html.escape(pre['html_url'])}\">"
                     f"{html.escape(pre['name'] or pre['tag_name'])}</a></p>")
    parts.append(f"<p>All releases: <a href=\"{RELEASES_URL}\">{RELEASES_URL}</a></p>")
    return "\n".join(parts)


def devel_section(site_dir, commit, date):
    devel = os.path.join(site_dir, "devel")
    if not os.path.isdir(devel):
        return "<p>No development build available.</p>"
    files = []
    for name in sorted(os.listdir(devel)):
        p = os.path.join(devel, name)
        if os.path.isfile(p) and not name.startswith("."):
            files.append((name, "devel/" + name, os.path.getsize(p)))
    parts = [f"<p>Built from branch <code>development</code>, commit "
             f"<a href=\"https://github.com/mgwerder/messageVortex/commit/{html.escape(commit)}\">"
             f"<code>{html.escape(commit[:8])}</code></a> ({html.escape(date)}).</p>",
             table(files)]
    if os.path.isdir(os.path.join(devel, "apidoc")):
        parts.append("<p><a href=\"devel/apidoc/index.html\">API documentation of the core library</a></p>")
    return "\n".join(parts)


def replace(path, marker, content):
    with open(path, encoding="utf-8") as f:
        text = f.read()
    if marker not in text:
        raise SystemExit(f"marker {marker} missing in {path}")
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(text.replace(marker, content))


def main(argv):
    if len(argv) == 3 and argv[1] == "digest":
        rel = load_releases(argv[2])
        key = [(r["tag_name"], r.get("prerelease"), r.get("published_at"),
                sorted((a["name"], a.get("updated_at")) for a in r.get("assets", []))) for r in rel]
        print(hashlib.sha256(json.dumps(key, sort_keys=True).encode()).hexdigest())
    elif len(argv) == 6 and argv[1] == "render":
        rel = load_releases(argv[2])
        site, commit, date = argv[3], argv[4], argv[5]
        releases = release_section(rel)
        replace(os.path.join(site, "documentation.html"), "<!--releases-->", releases)
        replace(os.path.join(site, "documentation.html"), "<!--development-->",
                devel_section(site, commit, date))
        replace(os.path.join(site, "download.html"), "<!--downloads-->", releases)
    else:
        sys.exit(__doc__)


if __name__ == "__main__":
    main(sys.argv)
