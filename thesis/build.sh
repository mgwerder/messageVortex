#!/bin/bash

dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)

mkdir $dir/target $dir/target/main $dir/target/main/latex-build 2>/dev/null

(
	#removing old remainders in build directory
	rm $dir/target/main/latex-build/messageVortex.*

	# making an initial build to generate auxiliary files
	cd $dir/src/main/latex
	pdflatex -output-directory=$dir/target/main/latex-build/  messageVortex.tex
	
	# generating biliography
	(cd $dir/src/main/latex; tar -cf - $(find . -name "*.bib") | (cd $dir/target/main/latex-build/; tar -xvf -))
	cd $dir/target/main/latex-build/
	bibtex messageVortex
	
	# generating index
	(cd $dir/target/main/latex-build; makeindex messageVortex)
	
	# generating final output
	cd $dir/src/main/latex
	pdflatex -output-directory=$dir/target/main/latex-build/  messageVortex.tex
	pdflatex -output-directory=$dir/target/main/latex-build/  messageVortex.tex
	
	# cleaning output directory
	find  $dir/target/main/latex-build | egrep -v "\.pdf$" |egrep -v "\.log$" |egrep -v "latex-build$" |sort -r| xargs -i rm -r {}
)