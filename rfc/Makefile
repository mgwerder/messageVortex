SHELL := /bin/bash

OUTDIR  = target/xml2rfc
INTERDIR= target/tmp
SRCDIR  = src/xml2rfc
XML2RFC = /usr/local/bin/xml2rfc
# X2R_OPTS= --v3
X2R_OPTS=
ALLFILES= $(addprefix $(INTERDIR)/draft-gwerder-messagevortexmain-,00 01)
ALLEXP  = $(addsuffix .xmlprep,$(ALLFILES)) $(addsuffix .uixml,$(ALLFILES)) $(addsuffix .nouixml,$(ALLFILES))
EXTS    = .pdf .ps .xmlflat .html .mobi .epub .txt .rawtxt .legacytxt
LEXTS   = .pdf .xmlflat .legacytxt

.PHONY: all clean dirs

.SECONDARY: $(ALLEXP)

all: $(addprefix $(OUTDIR)/draft-gwerder-messagevortexmain-03,$(EXTS)) $(addprefix $(OUTDIR)/draft-gwerder-messagevortexmain-02,$(LEXTS)) $(addprefix $(OUTDIR)/draft-gwerder-messagevortexmain-01,$(LEXTS)) $(addprefix $(OUTDIR)/draft-gwerder-messagevortexmain-00,$(LEXTS)) dirs 

clean:
	@rm -r target 2>/dev/null; /bin/true

dirs:
	@-mkdir -p $(OUTDIR) $(INTERDIR)/asn 2>/dev/null 

$(INTERDIR)/%.xmlprep: $(SRCDIR)/%.xml 
	@make dirs
	@echo "building tmp source file $@"
	@cp ../application-core-library/src/main/asn/* $(INTERDIR)/asn
	@cp $< $@
	@( \
		egrep "<artwork[^>]*src=\"[^\"]*.asn\"[^>]*/>" <$@ | while read line; \
		do \
		 	src=$$(echo "$$line"|sed 's/.*src="//;s/".*//'); \
		 	l=$$(echo "$$line"|sed 's/ src="[^"]*"//'); \
		 	rep="$${l%%/>}>"'<![CDATA['"$$(cat $(INTERDIR)/$$src)]]></artwork>"; \
		 	file=$$(cat $@); \
		 	echo "$${file/$$line/$$rep}" >$@.tmp && \
		 	mv $@.tmp $@ && \
		 	echo "      injected file $$src (new size is $$(stat --printf="%s" $@))"; \
		done \
	)

$(INTERDIR)/%.uixml: $(INTERDIR)/%.xmlprep
	@echo "building tmp source file $@"
	@cat $< | awk 'BEGIN{ incdata=0;}; /^.*<artwork[^>]* src="[^"]*"[^>\/]*>.*/ { gsub(/<!\[CDATA\[/,"");print;incdata=1; }; incdata!=1 {print $0;};incdata==1 && /.*\]\]>.*/ {gsub(/.*\]\]>/,"");print;incdata=0;}'  >$@

$(INTERDIR)/%.nouixml: $(INTERDIR)/%.xmlprep
	@echo "building tmp source file $@"
	@sed -e 's~<artwork\([^>]*\) src="[^"]*"~<artwork \1~gi' <$< >$@
	
$(OUTDIR)/%.pdf: $(INTERDIR)/%.uixml 
	@echo "building final file $@"
	@( cd $(INTERDIR); $(XML2RFC) $(X2R_OPTS) $(basename $(<F)).uixml --pdf -q -o ../../$@ )	
	
$(OUTDIR)/%.xmlflat: $(INTERDIR)/%.nouixml 
	@echo "building tmp source file $@"
	@( cd $(INTERDIR); $(XML2RFC) $(X2R_OPTS) $(basename $(<F)).nouixml --exp -q -o ../../$@)
	
$(OUTDIR)/%.txt: $(INTERDIR)/%.nouixml 
	@echo "building tmp source file $@"
	@( cd $(INTERDIR); $(XML2RFC) $(X2R_OPTS) $(basename $(<F)).nouixml --text -q -o ../../$@)
	
$(OUTDIR)/%.legacytxt: $(INTERDIR)/%.nouixml 
	@echo "building tmp source file $@"
	@( cd $(INTERDIR); $(XML2RFC) --legacy $(basename $(<F)).nouixml --text -q -o ../../$@)
	
$(OUTDIR)/%.rawtxt: $(INTERDIR)/%.nouixml 
	@echo "building tmp source file $@"
	@( cd $(INTERDIR); $(XML2RFC) --legacy $(basename $(<F)).nouixml --raw -q -o ../../$@)
	
$(OUTDIR)/%.html: $(INTERDIR)/%.uixml 
	@echo "building tmp source file $@"
	@( cd $(INTERDIR); $(XML2RFC) $(X2R_OPTS) $(basename $(<F)).uixml --html -q -o ../../$@)
	
$(OUTDIR)/%.mobi: $(OUTDIR)/%.html 
	@echo "building final file $@"
	@ebook-convert $< $@
	
$(OUTDIR)/%.epub: $(OUTDIR)/%.html
	@echo "building final file $@"
	@ebook-convert $< $@
	
$(OUTDIR)/%.ps: $(OUTDIR)/%.pdf
	@echo "building final file $@"
	@pdf2ps $< $@

