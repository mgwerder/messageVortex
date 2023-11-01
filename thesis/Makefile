SHELL := /bin/bash

BIB_BINARY = biber
OUTDIR  = $(dirname $(readlink -e target/main/latex)
INTERDIR= $(dirname $(readlink -e target/main/tmp)
SRCDIR  = src/main/latex
MKFILEP := $(abspath $(lastword $(MAKEFILE_LIST)))

.PHONY: all clean dirs

all: $(OUTDIR)/messageVortex.pdf $(OUTDIR)/messageVortex_attachdocs.pdf dirs 

clean:
	@rm -r target 2>/dev/null; /bin/true

dirs:
	@-for i in aux bbl bcf; do find . -name "*.$i" -exec rm \{} \; ;done
	@-mkdir -p $(OUTDIR) $(INTERDIR) $(INTERDIR)/rfc 2>/dev/null 
	
prepInterdir:
	@cp ../rfc/target/xml2rfc/*.pdf $(INTERDIR)/rfc
	(cd $(SRCDIR); tar -cf - . ) | (cd $(INTERDIR); tar -xvf -)
	-rm $(INTERDIR)/messageVortex.aux $(INTERDIR)/messageVortex.bbl
	


$(INTERDIR)/messageVortex.pdf: $(SRCDIR)/messageVortex.tex $(SRCDIR)/messageVortex.bib 
	@make dirs
	@echo "building tmp pdf file $@"
	@make prepInterdir
	(cd $(INTERDIR); pdflatex -output-directory=$(OUTDIR) -shell-escape messageVortex.tex )
	(cd $(INTERDIR)/; $(BIB_BINARY) messageVortex )
	(cd $(INTERDIR); TEXINPUTS=$(INTERDIR):../../../$(SRCDIR):$${TEXINPUTS} pdflatex -output-directory=$(OUTDIR) messageVortex.tex )
	(cd $(INTERDIR); TEXINPUTS=.:$${TEXINPUTS} pdflatex -output-directory=$(OUTDIR) -shell-escape messageVortex.tex )
	(cd $(INTERDIR)/; makeindex messageVortex )
	(cd $(INTERDIR); TEXINPUTS=.:$${TEXINPUTS} pdflatex -output-directory=$(OUTDIR) -shell-escape messageVortex.tex )
	#(cp $(INTERDIR)/message*.pdf $(OUTDIR))

$(INTERDIR)/messageVortex_attachdocs.pdf: $(SRCDIR)/messageVortex.tex $(SRCDIR)/messageVortex.bib $(INTERDIR)/messageVortex.pdf
	cp $(INTERDIR)/messageVortex.bib $(INTERDIR)/messageVortex_attachdocs.bib
	@make prepInterdir
	(cd $(INTERDIR); pdflatex -shell-escape messageVortex.tex -output-directory=$(OUTDIR) -jobname=messageVortex_attachdocs "\PassOptionsToClass{attachdocs}{extbook}\input{messageVortex.tex}")
	(cd $(SRCDIR); TEXINPUTS=.:$${TEXINPUTS} pdflatex -shell-escape -output-directory=$(OUTDIR) -jobname=messageVortex_attachdocs "\PassOptionsToClass{attachdocs}{extbook}\input{messageVortex.tex}")
	(cd $(INTERDIR)/; $(BIB_BINARY) messageVortex_attachdocs )
	(cd $(SRCDIR); TEXINPUTS=.:$${TEXINPUTS} pdflatex -shell-escape -output-directory=$(OUTDIR) -jobname=messageVortex_attachdocs "\PassOptionsToClass{attachdocs}{extbook}\input{messageVortex.tex}")
	(cd $(SRCDIR); TEXINPUTS=.:$${TEXINPUTS} pdflatex -shell-escape -output-directory=$(OUTDIR) -jobname=messageVortex_attachdocs "\PassOptionsToClass{attachdocs}{extbook}\input{messageVortex.tex}")
	(cd $(INTERDIR)/; makeindex messageVortex_attachdocs )
	(cd $(INTERDIR); TEXINPUTS=.:$${TEXINPUTS} pdflatex -output-directory=$(OUTDIR) -shell-escape -jobname=messageVortex_attachdocs "\PassOptionsToClass{attachdocs}{extbook}\input{messageVortex.tex}")
	#(cp $(INTERDIR)/message*.pdf $(OUTDIR))
        
$(OUTDIR)/%.pdf: $(INTERDIR)/%.pdf
	cp $< $@





