SHELL := /bin/bash

OUTDIR  = target/main/latex
INTERDIR= target/main/tmp
SRCDIR  = src/main/latex
MKFILEP := $(abspath $(lastword $(MAKEFILE_LIST)))

.PHONY: all clean dirs

all: $(OUTDIR)/messageVortex.pdf $(OUTDIR)/messageVortex_attachdocs.pdf $(OUTDIR)/messageVortex.pdfa dirs

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

$(INTERDIR)/messageVortex_attachdocs.pdf: $(SRCDIR)/messageVortex.tex $(SRCDIR)/messageVortex.bib $(INTERDIR)/messageVortex.pdf
	(cd $(SRCDIR); TEXINPUTS=.:../../../$(INTERDIR):$${TEXINPUTS} pdflatex -output-directory=../../../$(INTERDIR) -jobname=messageVortex_attachdocs "\PassOptionsToClass{attachdocs}{scrbook}\input{messageVortex.tex}")
	(cd $(INTERDIR)/; bibtex messageVortex )
	(cd $(INTERDIR)/; makeindex messageVortex )
	(cd $(SRCDIR); TEXINPUTS=.:../../../$(INTERDIR):$${TEXINPUTS} pdflatex -output-directory=../../../$(INTERDIR) -jobname=messageVortex_attachdocs "\PassOptionsToClass{attachdocs}{scrbook}\input{messageVortex.tex}")
	(cd $(SRCDIR); TEXINPUTS=.:../../../$(INTERDIR):$${TEXINPUTS} pdflatex -output-directory=../../../$(INTERDIR) -jobname=messageVortex_attachdocs "\PassOptionsToClass{attachdocs}{scrbook}\input{messageVortex.tex}")
        
$(OUTDIR)/%.pdf: $(INTERDIR)/%.pdf
	cp $< $@


%.pdfa: %.pdf
	gs -dPDFA -dBATCH -dNOPAUSE -sProcessColorModel=DeviceRGB -sDEVICE=pdfwrite -sPDFACompatibilityPolicy=1 -sOutputFile=$@ $<



