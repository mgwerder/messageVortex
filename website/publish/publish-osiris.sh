#!/bin/bash
# Publishes https://messagevortex.net (replaces the Jenkins job that was lost with hermes).
#
#  - website pages:  built with JBake from branch main
#  - devel/:         built from branch development (core jars, javadoc, ASN.1 definition, RFC drafts)
#  - release links:  generated from the GitHub releases (documentation.html, download.html)
#
# Meant to run from cron every 5 minutes (see messagevortex-web.cron). It only rebuilds when main,
# development or the GitHub releases changed. Builds run in Docker; nothing besides git, docker,
# rsync, curl and python3 is needed on the host.
#
# Environment: BASE (work dir), WEBROOT (target), WEB_OWNER (owner in webroot, empty = unchanged),
#              FORCE=1 (rebuild even if nothing changed),
#              NO_DOCKER=1 (run mvn/make/xml2rfc from PATH, for testing)
set -euo pipefail

REPO_URL=${REPO_URL:-https://github.com/mgwerder/messageVortex.git}
GH_API=${GH_API:-https://api.github.com/repos/mgwerder/messageVortex}
BASE=${BASE:-/var/lib/messagevortex-web}
WEBROOT=${WEBROOT:-/var/www/messagevortex}
MVN_IMAGE=${MVN_IMAGE:-maven:3.9-eclipse-temurin-17}
M2_VOLUME=${M2_VOLUME:-messagevortex-m2}
WEB_OWNER=${WEB_OWNER-root:www-data}   # empty: keep the owner (testing without root)

mkdir -p "$BASE"
exec 9>"$BASE/.lock"
flock -n 9 || exit 0

log() { echo "$(date -Is) $*"; }

# run a command in a build tree (mounted as /w), either in Docker or directly
run_in() {
  local image=$1 dir=$2; shift 2
  if [[ "${NO_DOCKER:-}" == 1 ]]; then
    (cd "$dir" && "$@")
  else
    docker run --rm -v "$dir:/w" -v "$M2_VOLUME:/root/.m2" -w /w "$image" "$@"
  fi
}

# checkout of a commit in its own directory (shares the objects of $BASE/src)
checkout() {
  local dir=$1 sha=$2
  rm -rf "$dir"
  git clone -q --shared --no-checkout "$BASE/src" "$dir"
  git -C "$dir" checkout -q --detach "$sha"
}

# ---- what changed? -------------------------------------------------------------------------------
[[ -d "$BASE/src/.git" ]] || git clone -q --no-checkout "$REPO_URL" "$BASE/src"
git -C "$BASE/src" fetch -q --prune origin \
  '+refs/heads/main:refs/remotes/origin/main' '+refs/heads/development:refs/remotes/origin/development'
main=$(git -C "$BASE/src" rev-parse origin/main)
dev=$(git -C "$BASE/src" rev-parse origin/development)
publisher=$(git -C "$BASE/src" rev-parse "origin/main:website/publish")

# publishing scripts always come from main
rm -rf "$BASE/src-publish"
mkdir -p "$BASE/src-publish"
git -C "$BASE/src" archive "origin/main" website/publish | tar -x -C "$BASE/src-publish" --strip-components=2

curl -fsS -o "$BASE/releases.json.new" "$GH_API/releases?per_page=50" || { log "GitHub API not reachable"; exit 0; }
mv "$BASE/releases.json.new" "$BASE/releases.json"
releases=$(python3 "$BASE/src-publish/mkpages.py" digest "$BASE/releases.json")

state="$main $dev $releases $publisher"
if [[ "${FORCE:-}" != 1 && -f "$BASE/state" && "$(cat "$BASE/state")" == "$state" ]]; then
  exit 0
fi
log "change detected: main=${main:0:8} development=${dev:0:8} releases=${releases:0:8}"

# ---- devel/ from development (only when development changed) ----------------------------------
build_devel() {
  local w="$BASE/dev" out="$BASE/devel.new"
  checkout "$w" "$dev"
  run_in "$MVN_IMAGE" "$w" mvn -B -ntp -q -pl application-core-library -DskipTests package

  local rfc_image="messagevortex-rfc:$(sha256sum "$BASE/src-publish/rfc.Dockerfile" | cut -c1-12)"
  local targets='make -C rfc -pn 2>/dev/null | sed -n "s/^all: //p" | tr " " "\n" | grep -v -E "\.(mobi|epub)$|^dirs$" | tr "\n" " "'
  if [[ "${NO_DOCKER:-}" == 1 ]]; then
    (cd "$w" && make -C rfc XML2RFC="$(command -v xml2rfc)" X2R_OPTS="-N --cache=$BASE/x2r-cache" $(eval "$targets") >/dev/null)
  else
    docker image inspect "$rfc_image" >/dev/null 2>&1 \
      || docker build -q -t "$rfc_image" -f "$BASE/src-publish/rfc.Dockerfile" "$BASE/src-publish" >/dev/null
    docker run --rm -v "$w:/w" -w /w "$rfc_image" sh -c \
      "make -C rfc XML2RFC=/usr/local/bin/xml2rfc X2R_OPTS='-N --cache=/tmp/x2r' \$($targets) >/dev/null"
  fi

  rm -rf "$out"; mkdir -p "$out"
  local t="$w/application-core-library/target"
  cp "$t/MessageVortex-core.jar"                  "$out/MessageVortex-development.jar"
  cp "$t/MessageVortex-jar-with-dependencies.jar" "$out/MessageVortex-development-all.jar"
  cp "$t/MessageVortex-package.jar"               "$out/MessageVortex-development-installer.jar"
  cp "$t/books/adminGuide.pdf"                    "$out/MessageVortex-development-adminGuide.pdf"
  python3 -m zipfile -e "$t/MessageVortex-javadoc.jar" "$out/apidoc"
  rm -rf "$out/apidoc/META-INF"
  for f in "$w"/application-core-library/src/main/asn/MessageVortex-*.asn; do cat "$f"; echo; echo; done \
    > "$out/MessageVortex_definition.asn"
  (cd "$w/application-core-library/src/main/asn" && python3 -m zipfile -c "$out/MessageVortex_definition.zip" MessageVortex-*.asn)
  cp "$w"/rfc/target/xml2rfc/draft-* "$w"/rfc/src/xml2rfc/draft-gwerder-*.xml "$out/"
  echo "$dev $(git -C "$w" show -s --format=%cs "$dev")" > "$out/.commit"

  rm -rf "$BASE/devel"; mv "$out" "$BASE/devel"
  rm -rf "$w"
}

if [[ ! -f "$BASE/devel/.commit" || "$(cut -d' ' -f1 "$BASE/devel/.commit")" != "$dev" ]]; then
  if [[ "${FORCE:-}" != 1 && -f "$BASE/devel.failed" && "$(cat "$BASE/devel.failed")" == "$dev" ]]; then
    log "development ${dev:0:8} failed before, keeping the previous devel/"
  else
    log "building devel/ from development ${dev:0:8}"
    if (build_devel) > "$BASE/devel.log" 2>&1; then
      rm -f "$BASE/devel.failed"
    else
      echo "$dev" > "$BASE/devel.failed"
      log "devel/ build failed, see $BASE/devel.log; keeping the previous devel/"
    fi
  fi
fi

# ---- website from main ----------------------------------------------------------------------------
log "building website from main ${main:0:8}"
w="$BASE/main"
checkout "$w" "$main"
if ! run_in "$MVN_IMAGE" "$w" mvn -B -ntp -q -f website/pom.xml org.jbake:jbake-maven-plugin:0.3.5:generate > "$BASE/site.log" 2>&1; then
  log "website build failed, see $BASE/site.log"
  exit 1
fi

stage="$BASE/stage"
rm -rf "$stage"; mkdir -p "$stage/devel"
cp -a "$w/website/target/main/jbake-tmp/." "$stage/"
[[ -d "$BASE/devel" ]] && cp -a "$BASE/devel/." "$stage/devel/"

# thesis PDF (linked as devel/messageVortex.pdf from the published RFC drafts): newest release having it
thesis=$(python3 -c '
import json, sys
rel = sorted((r for r in json.load(open(sys.argv[1])) if not r.get("draft")),
             key=lambda r: r.get("published_at") or "", reverse=True)
print(next((a["browser_download_url"] for r in rel for a in r["assets"] if a["name"] == "messageVortex.pdf"), ""))
' "$BASE/releases.json")
if [[ -n "$thesis" ]]; then
  if [[ ! -f "$BASE/thesis.url" || "$(cat "$BASE/thesis.url")" != "$thesis" ]]; then
    curl -fsSL -o "$BASE/messageVortex.pdf" "$thesis" && echo "$thesis" > "$BASE/thesis.url"
  fi
fi
if [[ -f "$BASE/messageVortex.pdf" ]]; then
  cp "$BASE/messageVortex.pdf" "$stage/devel/"
elif [[ -f "$WEBROOT/devel/messageVortex.pdf" ]]; then
  cp "$WEBROOT/devel/messageVortex.pdf" "$stage/devel/"
fi

read -r dev_commit dev_date < <(cat "$BASE/devel/.commit" 2>/dev/null || echo "unknown unknown")
python3 "$BASE/src-publish/mkpages.py" render "$BASE/releases.json" "$stage" "$dev_commit" "$dev_date"
rm -f "$stage/devel/.commit"

rsync -a --delete --exclude=/repos/ --exclude=/.well-known/ \
  ${WEB_OWNER:+--chown=$WEB_OWNER} --chmod=D555,F444 "$stage/" "$WEBROOT/"
rm -rf "$w"
echo "$state" > "$BASE/state"
log "published"
