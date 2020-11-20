SHELL := /bin/bash

BIB_BINARY = biber
OUTDIR  = target/main/latex
INTERDIR= target/main/tmp
SRCDIR  = src/main/latex
MKFILEP := $(abspath $(lastword $(MAKEFILE_LIST)))

.PHONY: all clean dirs

all: $(OUTDIR)/messageVortex.pdf $(OUTDIR)/messageVortex_attachdocs.pdf dirs $(OUTDIR)/filet.pdf $(OUTDIR)/MessageVortexExcerpt.pdf

clean:
	@rm -r target 2>/dev/null; /bin/true

dirs:
	@-for i in aux bbl bcf; do find . -name "*.$i" -exec rm \{} \; ;done
	@-mkdir -p $(OUTDIR) $(INTERDIR)/rfc 2>/dev/null 
	
prepInterdir:
	@cp ../rfc/target/xml2rfc/*.pdf $(INTERDIR)/rfc
	(cd $(SRCDIR); tar -cf - . ) | (cd $(INTERDIR); tar -xvf -)
	-rm $(INTERDIR)/messageVortex.aux $(INTERDIR)/messageVortex.bbl
	


$(INTERDIR)/messageVortex.pdf: $(SRCDIR)/messageVortex.tex $(SRCDIR)/messageVortex.bib 
	@make dirs
	@echo "building tmp pdf file $@"
	@make prepInterdir
	(cd $(INTERDIR); pdflatex messageVortex.tex )
	(cd $(INTERDIR)/; $(BIB_BINARY) messageVortex )
	(cd $(INTERDIR); TEXINPUTS=$(INTERDIR):../../../$(SRCDIR):$${TEXINPUTS} pdflatex -output-directory=../../../$(INTERDIR) messageVortex.tex )
	(cd $(INTERDIR); TEXINPUTS=$(INTERDIR):../../../$(SRCDIR):$${TEXINPUTS} pdflatex -output-directory=../../../$(INTERDIR) messageVortex.tex )
	(cd $(INTERDIR)/; makeindex messageVortex )
	(cd $(INTERDIR); TEXINPUTS=$(INTERDIR):../../../$(SRCDIR):$${TEXINPUTS} pdflatex -output-directory=../../../$(INTERDIR) messageVortex.tex )

$(INTERDIR)/messageVortex_attachdocs.pdf: $(SRCDIR)/messageVortex.tex $(SRCDIR)/messageVortex.bib $(INTERDIR)/messageVortex.pdf
	cp $(INTERDIR)/messageVortex.bib $(INTERDIR)/messageVortex_attachdocs.bib
	@make prepInterdir
	(cd $(INTERDIR); pdflatex messageVortex.tex -jobname=messageVortex_attachdocs "\PassOptionsToClass{attachdocs}{extbook}\input{messageVortex.tex}")
	(cd $(SRCDIR); TEXINPUTS=.:../../../$(INTERDIR):$${TEXINPUTS} pdflatex -output-directory=../../../$(INTERDIR) -jobname=messageVortex_attachdocs "\PassOptionsToClass{attachdocs}{extbook}\input{messageVortex.tex}")
	(cd $(INTERDIR)/; $(BIB_BINARY) messageVortex_attachdocs )
	(cd $(SRCDIR); TEXINPUTS=.:../../../$(INTERDIR):$${TEXINPUTS} pdflatex -output-directory=../../../$(INTERDIR) -jobname=messageVortex_attachdocs "\PassOptionsToClass{attachdocs}{extbook}\input{messageVortex.tex}")
	(cd $(SRCDIR); TEXINPUTS=.:../../../$(INTERDIR):$${TEXINPUTS} pdflatex -output-directory=../../../$(INTERDIR) -jobname=messageVortex_attachdocs "\PassOptionsToClass{attachdocs}{extbook}\input{messageVortex.tex}")
	(cd $(INTERDIR)/; makeindex messageVortex_attachdocs )
	(cd $(INTERDIR); TEXINPUTS=.:../../../$(INTERDIR):$${TEXINPUTS} pdflatex -output-directory=../../../$(INTERDIR) -jobname=messageVortex_attachdocs "\PassOptionsToClass{attachdocs}{extbook}\input{messageVortex.tex}")
        
$(INTERDIR)/filet.pdf: $(SRCDIR)/papers/filet/filet.tex $(SRCDIR)/messageVortex.bib 
	@make dirs
	@echo "building tmp pdf file $@"
	@make prepInterdir
	-rm $(INTERDIR)//papers/filet/filet.aux $(INTERDIR)/papers/filet/filet.bbl
	touch $(INTERDIR)/papers/filet/filet.aux $(INTERDIR)/papers/filet/filet.bbl $(INTERDIR)/papers/filet/filet.log
	(cd $(INTERDIR)/papers/filet; pdflatex filet.tex )
	-(cd $(INTERDIR)/papers/filet; bibtex filet )
	(cd $(INTERDIR)/papers/filet; pdflatex filet.tex )
	(cd $(INTERDIR)/papers/filet; pdflatex filet.tex )
	cp $(INTERDIR)/papers/filet/*.pdf $(INTERDIR)

$(INTERDIR)/MessageVortexExcerpt.pdf: $(SRCDIR)/papers/filet/MessageVortexExcerpt.tex $(SRCDIR)/messageVortex.bib 
	@make dirs
	@echo "building tmp pdf file $@"
	@make prepInterdir
	-rm $(INTERDIR)/MessageVortexExcerpt.aux $(INTERDIR)/MessageVortexExcerpt.bbl
	touch $(INTERDIR)/MessageVortexExcerpt.aux $(INTERDIR)/MessageVortexExcerpt.bbl $(INTERDIR)/MessageVortexExcerpt.log
	(cd $(INTERDIR); pdflatex messageVortex.tex )
	(cd $(INTERDIR)/papers/filet; pdflatex -output-directory=../../../../../$(INTERDIR)/papers/filet MessageVortexExcerpt.tex )	
	-(cd $(INTERDIR)/; bibtex MessageVortexExcerpt )
	(cd $(INTERDIR)/papers/filet; pdflatex MessageVortexExcerpt.tex )
	(cd $(INTERDIR)/papers/filet; pdflatex MessageVortexExcerpt.tex )
	cp $(INTERDIR)/papers/filet/*.pdf $(INTERDIR)

$(OUTDIR)/%.pdf: $(INTERDIR)/%.pdf
	cp $< $@





