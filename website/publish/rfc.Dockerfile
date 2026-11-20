# Image used by publish-osiris.sh to build the RFC drafts of the development branch
# (same xml2rfc version as the release workflow; epub/mobi are not built, they need calibre).
FROM python:3.12-slim-bookworm
RUN apt-get update \
 && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
      make ghostscript libpango-1.0-0 libpangoft2-1.0-0 fonts-noto-core fonts-noto-mono \
 && rm -rf /var/lib/apt/lists/*
RUN pip install --no-cache-dir 'xml2rfc[pdf]==3.35.0'
