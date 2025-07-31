SHELL := /bin/bash

BIB_BINARY = biber
OUTDIR  = target/main/latex
ROUTDIR=../../../$(OUTDIR)
INTERDIR= target/main/tmp
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
	@ln -sf ../../../application-core-library/src/main/asn $(INTERDIR)/asn
	-rm $(INTERDIR)/messageVortex.aux $(INTERDIR)/messageVortex.bbl
	


$(INTERDIR)/messageVortex.pdf: $(SRCDIR)/messageVortex.tex $(SRCDIR)/messageVortex.bib 
	@make dirs
	@echo "building tmp pdf file $@"
	@make prepInterdir
	(cd $(INTERDIR); texfot pdflatex -interaction=nonstopmode -output-directory=$(ROUTDIR) -shell-escape messageVortex.tex )
	(cd $(INTERDIR)/; $(BIB_BINARY) messageVortex )
	(cd $(INTERDIR); TEXINPUTS=$(INTERDIR):../../../$(SRCDIR):$${TEXINPUTS} texfot pdflatex -interaction=nonstopmode -output-directory=$(ROUTDIR) messageVortex.tex )
	(cd $(INTERDIR); TEXINPUTS=.:$${TEXINPUTS} texfot pdflatex -interaction=nonstopmode -output-directory=$(ROUTDIR) -shell-escape messageVortex.tex )
	(cd $(INTERDIR)/; makeindex messageVortex )
	(cd $(INTERDIR); TEXINPUTS=.:$${TEXINPUTS} texfot pdflatex -interaction=nonstopmode -output-directory=$(ROUTDIR) -shell-escape messageVortex.tex )

$(INTERDIR)/messageVortex_attachdocs.pdf: $(SRCDIR)/messageVortex.tex $(SRCDIR)/messageVortex.bib $(INTERDIR)/messageVortex.pdf
	cp $(INTERDIR)/messageVortex.bib $(INTERDIR)/messageVortex_attachdocs.bib
	cp $(INTERDIR)/messageVortex.idx $(INTERDIR)/messageVortex_attachdocs.idx
	rm $(INTERDIR)/messagevortex.{aux,bbl,bcf,blg,idx,ilg,ind,aux,log,lof,lot,ptx,toc,upa,upb} 2>/dev/null ||/bin/true
	@make prepInterdir
	(cd $(INTERDIR); texfot pdflatex -interaction=nonstopmode -shell-escape messageVortex.tex -jobname=messageVortex_attachdocs "\PassOptionsToClass{attachdocs}{extbook}\input{messageVortex.tex}")
	#(cd $(SRCDIR); TEXINPUTS=.:$${TEXINPUTS} pdflatex -interaction=nonstopmode -shell-escape -output-directory=$(ROUTDIR) -jobname=messageVortex_attachdocs "\PassOptionsToClass{attachdocs}{extbook}\input{messageVortex.tex}")
	cp $(OUTDIR)/messageVortex.bcf $(INTERDIR)/messageVortex_attachdocs.bcf
	(cd $(INTERDIR)/; $(BIB_BINARY) messageVortex_attachdocs )
	cp $(OUTDIR)/messageVortex.aux $(INTERDIR)/messageVortex_attachdocs.aux
	(cd $(INTERDIR); TEXINPUTS=.:$${TEXINPUTS} texfot pdflatex -interaction=nonstopmode -shell-escape -jobname=messageVortex_attachdocs "\PassOptionsToClass{attachdocs}{extbook}\input{messageVortex.tex}")
	cp $(OUTDIR)/messageVortex.aux $(INTERDIR)/messageVortex_attachdocs.aux
	cp $(OUTDIR)/messageVortex.aux $(INTERDIR)/messageVortex_attachdocs.aux
	(cd $(INTERDIR); TEXINPUTS=.:$${TEXINPUTS} texfot pdflatex -interaction=nonstopmode -shell-escape -jobname=messageVortex_attachdocs "\PassOptionsToClass{attachdocs}{extbook}\input{messageVortex.tex}")
	(cd $(INTERDIR)/; makeindex messageVortex_attachdocs )
	(cd $(INTERDIR); TEXINPUTS=.:$${TEXINPUTS} texfot pdflatex -shell-escape -jobname=messageVortex_attachdocs "\PassOptionsToClass{attachdocs}{extbook}\input{messageVortex.tex}")
        
$(OUTDIR)/%.pdf: $(INTERDIR)/%.pdf
	cp $< $@





