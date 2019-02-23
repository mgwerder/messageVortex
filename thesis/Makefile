SHELL := /bin/bash

OUTDIR  = target/main/latex
INTERDIR= target/main/tmp
SRCDIR  = src/main/latex
MKFILEP := $(abspath $(lastword $(MAKEFILE_LIST)))

.PHONY: all clean dirs

all: $(OUTDIR)/messageVortex.pdf dirs

clean:
	@rm -r target 2>/dev/null; /bin/true

dirs:
	@-mkdir -p $(OUTDIR) $(INTERDIR)/rfc 2>/dev/null 

$(INTERDIR)/messageVortex.pdf: $(SRCDIR)/messageVortex.tex $(SRCDIR)/messageVortex.bib 
	@make dirs
	@echo "building tmp pdf file $@"
	@cp ../rfc/target/xml2rfc/*.pdf $(INTERDIR)/rfc
	(cd $(SRCDIR); TEXINPUTS=../../../$(INTERDIR):$${TEXINPUTS} pdflatex -output-directory=../../../$(INTERDIR) messageVortex.tex )
	(cd $(SRCDIR); tar -cf - $$(find . -name '*.bib')) | (cd $(INTERDIR); tar -xvf -)
	(cd $(INTERDIR)/; bibtex messageVortex )
	(cd $(INTERDIR)/; makeindex messageVortex )
	(cd $(SRCDIR); TEXINPUTS=../../../$(INTERDIR):$${TEXINPUTS} pdflatex -output-directory=../../../$(INTERDIR) messageVortex.tex )
	(cd $(SRCDIR); TEXINPUTS=../../../$(INTERDIR):$${TEXINPUTS} pdflatex -output-directory=../../../$(INTERDIR) messageVortex.tex )
        
$(OUTDIR)/%.pdf: $(INTERDIR)/%.pdf
	cp $< $@



