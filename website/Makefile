SHELL := /bin/bash

# Copies the baked website to its final location. The generated sections (release links, devel/)
# are filled in by publish/publish-osiris.sh when the site is published.
OUTDIR  = target/main/jbake
INTERDIR= target/main/jbake-tmp

.PHONY: all clean

all:
	@mkdir -p $(OUTDIR) $(INTERDIR)
	@rsync -a $(INTERDIR)/ $(OUTDIR)/

clean:
	@rm -r target 2>/dev/null; /bin/true
