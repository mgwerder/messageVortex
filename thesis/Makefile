SHELL := /bin/bash

OUTDIR  = target/main/latex
INTERDIR= target/main/tmp
SRCDIR  = src/main/latex
MKFILEP := $(abspath $(lastword $(MAKEFILE_LIST)))

.PHONY: all clean dirs

all: $(OUTDIR)/messageVortex.pdf $(OUTDIR)/messageVortex_attachdocs.pdf $(OUTDIR)/messageVortex.pdfa dirs $(OUTDIR)/filet.pdf

clean:
	@rm -r target 2>/dev/null; /bin/true

dirs:
	@-mkdir -p $(OUTDIR) $(INTERDIR)/rfc 2>/dev/null 

$(INTERDIR)/messageVortex.pdf: $(SRCDIR)/messageVortex.tex $(SRCDIR)/messageVortex.bib 
	@make dirs
	@echo "building tmp pdf file $@"
	@cp ../rfc/target/xml2rfc/*.pdf $(INTERDIR)/rfc
	(cd $(SRCDIR); tar -cf - $$(find . -name '*.bib') inc/bib/ ) | (cd $(INTERDIR); tar -xvf -)
	(cd $(SRCDIR); TEXINPUTS=../../../$(INTERDIR):$${TEXINPUTS} pdflatex -output-directory=../../../$(INTERDIR) messageVortex.tex )
	(cd $(INTERDIR)/; bibtex messageVortex )
	(cd $(SRCDIR); TEXINPUTS=../../../$(INTERDIR):$${TEXINPUTS} pdflatex -output-directory=../../../$(INTERDIR) messageVortex.tex )
	(cd $(SRCDIR); TEXINPUTS=../../../$(INTERDIR):$${TEXINPUTS} pdflatex -output-directory=../../../$(INTERDIR) messageVortex.tex )
	(cd $(INTERDIR)/; makeindex messageVortex )
	(cd $(SRCDIR); TEXINPUTS=../../../$(INTERDIR):$${TEXINPUTS} pdflatex -output-directory=../../../$(INTERDIR) messageVortex.tex )

$(INTERDIR)/messageVortex_attachdocs.pdf: $(SRCDIR)/messageVortex.tex $(SRCDIR)/messageVortex.bib $(INTERDIR)/messageVortex.pdf
	cp $(INTERDIR)/messageVortex.bib $(INTERDIR)/messageVortex_attachdocs.bib
	(cd $(SRCDIR); TEXINPUTS=.:../../../$(INTERDIR):$${TEXINPUTS} pdflatex -output-directory=../../../$(INTERDIR) -jobname=messageVortex_attachdocs "\PassOptionsToClass{attachdocs}{scrbook}\input{messageVortex.tex}")
	(cd $(INTERDIR)/; bibtex messageVortex_attachdocs )
	(cd $(SRCDIR); TEXINPUTS=.:../../../$(INTERDIR):$${TEXINPUTS} pdflatex -output-directory=../../../$(INTERDIR) -jobname=messageVortex_attachdocs "\PassOptionsToClass{attachdocs}{scrbook}\input{messageVortex.tex}")
	(cd $(SRCDIR); TEXINPUTS=.:../../../$(INTERDIR):$${TEXINPUTS} pdflatex -output-directory=../../../$(INTERDIR) -jobname=messageVortex_attachdocs "\PassOptionsToClass{attachdocs}{scrbook}\input{messageVortex.tex}")
	(cd $(INTERDIR)/; makeindex messageVortex_attachdocs )
	(cd $(SRCDIR); TEXINPUTS=.:../../../$(INTERDIR):$${TEXINPUTS} pdflatex -output-directory=../../../$(INTERDIR) -jobname=messageVortex_attachdocs "\PassOptionsToClass{attachdocs}{scrbook}\input{messageVortex.tex}")
        
$(INTERDIR)/filet.pdf: $(SRCDIR)/papers/filet/filet.tex $(SRCDIR)/messageVortex.bib 
	@make dirs
	@echo "building tmp pdf file $@"
	(cd $(SRCDIR); tar -cf - $$(find . -name '*.bib') inc/bib/ ) | (cd $(INTERDIR); tar -xvf -)
	(cd $(SRCDIR)/papers/filet; TEXINPUTS=../../../../../$(INTERDIR)/papers/filet:../../../../../$(INTERDIR):$${TEXINPUTS} pdflatex -output-directory=../../../../../$(INTERDIR) filet.tex )
	(cd $(INTERDIR)/; bibtex messageVortex )
	(cd $(SRCDIR)/papers/filet; TEXINPUTS=../../../../../$(INTERDIR)/papers/filet:../../../../../$(INTERDIR):$${TEXINPUTS} pdflatex -output-directory=../../../../../$(INTERDIR) filet.tex )
	(cd $(SRCDIR)/papers/filet; TEXINPUTS=../../../../../$(INTERDIR)/papers/filet:../../../../../$(INTERDIR):$${TEXINPUTS} pdflatex -output-directory=../../../../../$(INTERDIR) filet.tex )
	(cd $(INTERDIR)/; makeindex messageVortex )
	(cd $(SRCDIR)/papers/filet; TEXINPUTS=../../../../../$(INTERDIR)/papers/filet:../../../../../$(INTERDIR):$${TEXINPUTS} pdflatex -output-directory=../../../../../$(INTERDIR) filet.tex )

$(OUTDIR)/%.pdf: $(INTERDIR)/%.pdf
	cp $< $@


%.pdfa: %.pdf
	gs -dPDFA -dBATCH -dNOPAUSE -sProcessColorModel=DeviceRGB -sDEVICE=pdfwrite -sPDFACompatibilityPolicy=1 -sOutputFile=$@ $<



