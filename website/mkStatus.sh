#!/bin/bash

dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)


f=$(cat $1)
echo "getting fixmes"
ffatal=$(grep "(FiXme)" $dir/../thesis/target/main/tmp/messageVortex.log | grep -i "of fatal error" |tail -n 1|sed 's/.*: *//gi;s/[\.,]$//')
ferror=$(grep "(FiXme)" $dir/../thesis/target/main/tmp/messageVortex.log | grep -i "of error" |tail -n 1|sed 's/.*: *//gi;s/[\.,]$//')
fwarning=$(grep "(FiXme)" $dir/../thesis/target/main/tmp/messageVortex.log | grep -i "of warning" |tail -n 1|sed 's/.*: *//gi;s/[\.,]$//')
ftotal=$(grep "(FiXme)" $dir/../thesis/target/main/tmp/messageVortex.log | grep "Total" |tail -n 1|sed 's/.*: *//gi;s/[\.,]$//')

cref="unknown"
fileRows="unknown"
progressPercents="unknown"
progressWords="unknown"
progressSourcecode="unknown"
historyTable="FIXME"

# process text
tmp=`mktemp`
mtmp=`mktemp`
echo -n "0">$tmp
(cd $dir/../thesis/src/main/latex;ls *.tex papers/*.tex|while read f; 
do 
  echo  -n "+`(detex ${f} |wc -w)`">>$tmp
  echo "      <tr>">>$mtmp
  echo "        <th style=\"text-align: left;\"><a href=\"devel/doc/${f%%.tex}.pdf\">$f</a>(<a href=\"phd/doc/${f%%.tex}.odt\">odt</a>)</th>">>$mtmp
  pandoc --data-dir=$(pwd) -f latex -t odt -o $dir/../target/${f%%.tex}.odt $f >/dev/null
  echo "        <td style=\"text-align: right;\">`(detex ${f} |wc -l)`</td>">>$mtmp
  echo "        <td style=\"text-align: right;\">`(detex ${f} |wc -w)`</td>">>$mtmp
  echo  -n "+`(detex ${f} |wc -w)`">>$tmp
  echo "      </tr>">>$mtmp
done)
fileRows="$(cat $mtmp)"
progressWords=$((cat $tmp ;echo "")| bc)
progressPercents=$(echo "scale=3;100.0*$progressWords/60000"|bc)
rm $tmp $mtmp

# sourcecode analysis
progressSourcecode="$(find $dir/../application-core-library/src/main/java -name *.java |(echo -n "0";while read f; do echo -n "+$(egrep -v "^ *$" <$f |wc -l)";done;echo "")|bc)"

# git history
echo "    <table class=\"basic\">">$mtmp
echo "      <tr><th>Release</th><th>User</th><th>Date</th><th>Change size</th><th>Changed files</th><th>Comment</th></tr>">>$mtmp
(cd $dir/..;ssh-agent bash -c 'ssh-add ../github_readonly.key 2>/dev/null ; git log' )|gawk -e '
  BEGIN {
               FS="|";
               LASTREL="";
               REL="";
       }
  A==3 && /^ / {
               gsub("^ *","")
               COMMENT=COMMENT "<br/>" $0
       }
  A==2 && /Author: / {
               gsub("Author: ","");
               AUTHOR=$0;
       }
  A==2 && /Date: / {
               gsub("Date: ","");
               DATE=$0;
       }
  A==2 && /^$/ {
               A++
       }
  /^commit .*$/ && LASTREL!="" {
               A=2;
               LASTREL=REL;
               gsub("commit ","")
               REL=$0
               print "<tr><td>" LASTREL "</td><td>" AUTHOR "</td><td>" DATE "</td>";
               CMD="(cd '$dir'/..;ssh-agent bash -c '\''ssh-add ../github_readonly.key 2>/dev/null ;  git diff " LASTREL " " REL "'\'' )|wc -c "
               while ( ( CMD | getline result ) > 0 ) {
                 print  "<td>" int(result/1024+0.5) "</td>";
               }
               close(CMD);
               CMD="(cd '$dir'/..;ssh-agent bash -c '\''ssh-add '$dir'/../../github_readonly.key 2>/dev/null ; git diff " LASTREL " " REL "'\'' )| grep \"+++ b\" 2>/dev/null |sed \"s/+++ b//\"";
               print "<td><div id=\"" LASTREL "_full\" onclick=\"document.getElementById('\''" LASTREL "_full'\'').style.display=true;\">";
               C=0
               while ( ( CMD | getline result ) > 0 ) {
                 print result "<br/>"
                 C++
               }
               close(CMD);
               print "</div>"
               print "<div id=\"" LASTREL "_sum\" onclick=\"document.getElementById('\''" LASTREL "_sum'\'').style.display=display=true\">"
               print "Files: " C ;
               print "</div>"
               print "</td><td>" COMMENT "</td></tr>";
               COMMENT=""
       }
  /^commit .*$/ {
               A=2;
               LASTREL=REL;
               gsub("commit ","")
               REL=$0
       };
'  >>$mtmp
echo "    </table>">>$mtmp
historyTable="$(cat $mtmp)"
rm $mtmp

# replace occurences
f="${f/<!-- REPL:fixmeFatal -->/$ffatal}"
f="${f/<!-- REPL:fixmeError -->/$ferror}"
f="${f/<!-- REPL:fixmeWarning -->/$fwarning}"
f="${f/<!-- REPL:fixmeTotal -->/$ftotal}"
f="${f/<!-- REPL:fileRows -->/$fileRows}"
f="${f/<!-- REPL:cref -->/$crev}"
f="${f/<!-- REPL:progressPercents -->/$progressPercents}"
f="${f/<!-- REPL:progressWords -->/$progressWords}"
f="${f/<!-- REPL:progressSourcecode -->/$progressSourcecode}"
f="${f/<!-- REPL:historyTable -->/$historyTable}"

echo "$f" >$2
