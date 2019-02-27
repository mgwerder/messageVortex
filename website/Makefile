SHELL := /bin/bash

OUTDIR  = target/main/jbake
INTERDIR= target/main/jbake-tmp
SRCDIR  = src/main/jabake
MKFILEP := $(abspath $(lastword $(MAKEFILE_LIST)))

.PHONY: all clean dirs

all: $(OUTDIR)/status.html $(OUTDIR)/documentation.html dirs

clean:
	@rm -r target 2>/dev/null; /bin/true

dirs:
	@-mkdir -p $(OUTDIR) $(INTERDIR) 2>/dev/null 
	@-rsync -av --progress $(INTERDIR)/ $(OUTDIR)/ --exclude status.html --exclude documentation.html

$(OUTDIR)/status.html: $(INTERDIR)/status.html
	@make dirs
	./mkStatus.sh $< $@
        
$(OUTDIR)/documentation.html: $(INTERDIR)/documentation.html
	@make dirs
	./mkDocumentation.sh $< $@



