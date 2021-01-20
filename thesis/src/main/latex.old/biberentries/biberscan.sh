#!/bin/bash

let n=0
rm bib[0-9]*.bib
if [[ "$1" == "" ]]
then
	cat -
else
	cat $1
fi | while read l;
do
	if [[ $l == @* ]]
	then 
		let n=$n+1
		if test "[ ( $2 -eq $n ) -o ( "$2" == "" )  ]"
		then 
			echo -n "" >bib$n.bib
		fi	
	fi 	
	if test "[ ( $2 -eq $n ) -o ( "$2" == "" ) ]"
	then 
		echo "$l" >>bib$n.bib
	fi	
done				
 

# script processes all .bib files in current direcory with "biber --tool"
let n=0 # counter for files
# count files and print their number
for j in *.bib
do 
	let n=n+1
done
echo $n bibtex files found

# process files one by one with biber --tool
if [[ "$2" != "" ]]
then
	biber --tool bib$2.bib
else 	
	for j in *.bib; do 
		echo "FILE: $j"
		ret="$(biber --tool $j 2>&1 |egrep -v INFO )"
		rm $j.blg
	
		if [[ "$ret" == "" ]]
		then 
			rm $j
		else 
			echo "$ret"	
		fi	
	done
fi