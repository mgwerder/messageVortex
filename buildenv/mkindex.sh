#!/bin/bash
XML2RFC=/usr/local/bin/xml2rfc
dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
WWWDIR=$dir/../target/www

cd $dir
mtmp=`mktemp`
logger -t mkindex.sh "current revision is $crev" 
echo "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">" >$mtmp
echo "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">" >>$mtmp
echo "  <head>" >>$mtmp
echo "    <meta http-equiv=\"refresh\" content=\"300\"/>" >>$mtmp
echo "    <!-- Release: $crev -->">>$mtmp
echo "    <meta http-equiv=\"Content-type\" content=\"text/html;charset=ISO-8859-1\"/>">>$mtmp
echo "    <title>MessageVortex $crev</title>">>$mtmp
echo "    <link rel=\"stylesheet\" type=\"text/css\" href=\"main.css\" />">>$mtmp
echo "  </head>">>$mtmp
echo "  <body>">>$mtmp
echo "    <h1>MessageVortex $crev</h1>">>$mtmp
echo "    <p>last update $(date)</p>">>$mtmp
echo "    <p>">>$mtmp
echo "       <img src=\"https://www.gwerder.net/jenkins/buildStatus/icon?job=messageVortex&style=plastic\"/>">>$mtmp
echo "       <img src=\"https://www.gwerder.net/sonar/api/badges/gate?key=net.gwerder.java:messagevortex\"/>">>$mtmp
echo "       <img src=\"https://www.gwerder.net/sonar/api/badges/measure?key=net.gwerder.java:messagevortex&metric=line_coverage\"/>">>$mtmp
echo "       <img src=\"https://www.gwerder.net/sonar/api/badges/measure?key=net.gwerder.java:messagevortex&metric=comment_lines_density\"/>">>$mtmp
echo "       <img src=\"https://www.gwerder.net/sonar/api/badges/measure?key=net.gwerder.java:messagevortex&metric=lines\"/>">>$mtmp
echo "       <img src=\"https://www.gwerder.net/sonar/api/badges/measure?key=net.gwerder.java:messagevortex&metric=coverage\"/>">>$mtmp
echo "    </p>">>$mtmp
echo "    <h2>Stats</h2>">>$mtmp
rm -r ${WWWDIR}
mkdir -p ${WWWDIR}
mkdir ${WWWDIR}/devel/
mkdir ${WWWDIR}/devel/images
echo "    <table class=\"basic\">">>$mtmp
echo "      <tr><th style=\"text-align: left;\">Fixme type</th><th style=\"text-align: left;\">#</th></tr>">>$mtmp
echo "      <tr><td>Fatal fixme</td><td style=\"text-align: right;\">$(grep "(FiXme)" $dir/../thesis/target/main/latex-build/messageVortex.log | grep -i "of fatal error" |tail -n 1|sed 's/.*: *//gi;s/[\.,]$//')</td></tr>">>$mtmp
echo "      <tr><td>Error fixme</td><td style=\"text-align: right;\">$(grep "(FiXme)" $dir/../thesis/target/main/latex-build/messageVortex.log | grep -i "of error" |tail -n 1|sed 's/.*: *//gi;s/[\.,]$//')</td></tr>">>$mtmp
echo "      <tr><td>Warning fixme</td><td style=\"text-align: right;\">$(grep "(FiXme)" $dir/../thesis/target/main/latex-build/messageVortex.log | grep -i "of warning" |tail -n 1|sed 's/.*: *//gi;s/[\.,]$//')</td></tr>">>$mtmp
echo "      <tr><th>Total</th><th style=\"text-align: right;\">$(grep "(FiXme)" $dir/../thesis/target/main/latex-build/messageVortex.log | grep "Total" |tail -n 1|sed 's/.*: *//gi;s/[\.,]$//')</th></tr>">>$mtmp
echo "    </table><br/>">>$mtmp

echo "    <table class=\"basic\">">>$mtmp
echo "      <tr><th style=\"text-align: left;\">File</th><th style=\"text-align: left;\"># Lines</th><th style=\"text-align: left;\"># words</th></tr>">>$mtmp
a=0

ls $dir/../thesis/src/main/latex/inc/*.pdf|while read img;
do
	img=${img##*/}; 
	img=${img%%.*}
	echo "  converting $img.pdf -> $img.png"
	convert $dir/../thesis/src/main/latex/inc/$img.pdf ${WWWDIR}/devel/images/$img.png
	#cp ${WWWDIR}/devel/images/$img.png phd/thesis/src/main/latex/rfc/
done	

tmp=`mktemp`
echo -n "0">$tmp
(cd $dir/../thesis/src/main/latex;ls *.tex papers/*.tex|while read f; 
do 
  echo "      <tr>">>$mtmp
  echo "        <th style=\"text-align: left;\"><a href=\"phd/doc/${f%%.tex}.pdf\">$f</a>(<a href=\"phd/doc/${f%%.tex}.odt\">odt</a>)</th>">>$mtmp
  pandoc --data-dir=$(pwd) -f latex -t odt -o ${f%%.tex}.odt $f >/dev/null
  echo "        <td style=\"text-align: right;\">`(detex ${f} |wc -l)`</td>">>$mtmp
  echo "        <td style=\"text-align: right;\">`(detex ${f} |wc -w)`</td>">>$mtmp
  echo  -n "+`(detex ${f} |wc -w)`">>$tmp
  echo "      </tr>">>$mtmp
done)
ttmp=$(mktemp)
export out=draft-gwerder-messagevortexmain-01
(
  echo "adding inclusion files"
  mkdir $dir/../thesis/src/main/latex/rfc/asn
  cp $dir/../application-core-library/src/main/asn/*.asn $dir/../thesis/src/main/latex/rfc/asn
  echo "    creating xml flatified output"
  echo "      injecting artwork"
  egrep "<artwork[^>]*src=\"[^\"]*.asn\"[^>]*/>" <$dir/../thesis/src/main/latex/rfc/$out.xml| while read l ; 
  do 
  	src=$(echo "$l"|sed 's/.*src="//;s/".*//');
  	rep="${l%%/>}>"'<![CDATA['"$(cat $dir/../thesis/src/main/latex/rfc/$src)]]></artwork>";
  	file=$(cat $dir/../thesis/src/main/latex/rfc/$out.xml); 
  	echo "${file/$l/$rep}" >$dir/../thesis/src/main/latex/rfc/$out.xml.tmp && mv $dir/../thesis/src/main/latex/rfc/$out.xml.tmp phd/thesis/src/main/latex/rfc/$out.xml && echo "      injected file $src (new size is $(stat --printf="%s"  phd/thesis/src/main/latex/rfc/$out.xml))"
  done
  echo "      flatifying"
  $XML2RFC --exp $dir/../thesis/src/main/latex/rfc/${out}.xml -q -o $dir/../thesis/src/main/latex/rfc/$out.xmlflat || exit 101
  
  echo "  creating txt output"
  $XML2RFC $dir/../thesis/src/main/latex/rfc/${out}.xmlflat --text -q -o $dir/../thesis/src/main/latex/rfc/$out.txt || exit 101
  echo "  creating html output"
  $XML2RFC --v3 $dir/../thesis/src/main/latex/rfc/${out}.xmlflat --html -q -o $dir/../thesis/src/main/latex/rfc/$out.html || exit 101
  echo "  creating nroff output"
  $XML2RFC --v3 $dir/../thesis/src/main/latex/rfc/${out}.xmlflat --nroff -q -o $dir/../thesis/src/main/latex/rfc/$out.nroff || exit 101
  echo "  creating pdf"
  enscript -DDuplex:true --title "$out" -B -L 59 --margins=70:70:70:70 -p - $dir/../thesis/src/main/latex/rfc/${out}.txt|ps2pdf - - >$dir/../thesis/src/main/latex/rfc/$out.pdf   || exit 101
  #(cd  phd/thesis/src/main/latex/rfc/; ../../../xml2rfc/bin/mkpdf.sh $out.xmlflat  && mv $out.xmlflat.pdf $out.pdf )
  echo "  creating ps"
  # (cd  phd/thesis/src/main/latex/rfc/; ../../../xml2rfc/bin/mkps.sh $out.xmlflat   && mv $out.xmlflat.ps  $out.ps )
  (cd  $dir/../thesis/src/main/latex/rfc/; pdf2ps $out.pdf $out.ps) 
  echo "  creating epub"
  (cd  $dir/../thesis/src/main/latex/rfc/; pandoc -f html -t epub3 -o $out.epub $out.html || exit 101)  || exit $?
  (cd  $dir/../thesis/src/main/latex/rfc/; ebook-convert $out.html $out.epub || exit 101)  || exit $?
  (cd  $dir/../thesis/src/main/latex/rfc/; ebook-convert $out.html $out.mobi || exit 101)  || exit $?
  #(cd  phd/thesis/src/main/latex/rfc/; ../../../xml2rfc/bin/mkepub.sh $out.xmlflat && mv $out.xmlflat     $out.epub )
  
  cat $dir/../thesis/src/main/latex/rfc/${out}.txt |wc -l >$ttmp.lines
  cat $dir/../thesis/src/main/latex/rfc/${out}.txt |wc -w >$ttmp.words
  #enscript -DDuplex:true --title "$out" -B -L 59 --margins=70:70:70:70 -p - phd/thesis/src/main/latex/rfc/${out}.txt|ps2pdf - - >phd/thesis/src/main/latex/rfc/$out.pdf
  rm $ttmp 
)
echo "      <tr>">>$mtmp
echo  -n "+`(cat $ttmp.words)`">>$tmp
echo "        <th style=\"text-align: left;\"><a href=\"phd/doc/rfc/${out}.xml\">$out.xml</a> (<a href=\"phd/doc/rfc/${out}.txt\">txt</a>, <a href=\"phd/doc/rfc/${out}.html\">html</a>, <a href=\"phd/doc/rfc/${out}.pdf\">pdf</a>)</th><td style=\"text-align: right;\">$(cat $ttmp.lines; rm $ttmp.lines)</td><td style=\"text-align: right;\">$(cat $ttmp.words; rm $ttmp.words)</td>">>$mtmp
echo "      </tr>">>$mtmp
echo "    </table>">>$mtmp
a=`((cat $tmp ;echo "")| bc)`
echo "    <p>`(echo "scale=3;100.0*$a/60000"|bc)`% completed ($a words)</p>">>$mtmp
echo "    <p>`find $dir/../application-core-library/src/main/java -name *.java |(echo -n "0";while read f; do echo -n "+$(wc -l <$f)";done;echo "")|bc` lines of code</p>">>$mtmp
#echo "    <h2>History</h2>">>$mtmp
#echo "    <table class=\"basic\">">>$mtmp
#echo "      <tr><th>Release</th><th>User</th><th>Date</th><th>Change size</th><th>Changed files</th><th>Comment</th></tr>">
#(cd phd;ssh-agent bash -c 'ssh-add ../github_readonly.key 2>/dev/null ; git log' )|gawk -e '
#  BEGIN {
#               FS="|";
#               LASTREL="";
#               REL="";
#       }
#  A==3 && /^ / {
#               gsub("^ *","")
#               COMMENT=COMMENT "<br/>" $0
#       }
#  A==2 && /Author: / {
#               gsub("Author: ","");
#               AUTHOR=$0;
#       }
#  A==2 && /Date: / {
#               gsub("Date: ","");
#               DATE=$0;
#       }
#  A==2 && /^$/ {
#               A++
#       }
#  /^commit .*$/ && LASTREL!="" {
#               A=2;
#               LASTREL=REL;
#               gsub("commit ","")
#               REL=$0
#               print "<tr><td>" LASTREL "</td><td>" AUTHOR "</td><td>" DATE "</td>";
#               CMD="(cd phd;ssh-agent bash -c ''ssh-add ../github_readonly.key 2>/dev/null ;  git diff " LASTREL " " REL "'
#               while ( ( CMD | getline result ) > 0 ) {
#                 print  "<td>" int(result/1024+0.5) "</td>";
#               }
#               close(CMD);
#               CMD="(cd phd;ssh-agent bash -c '\''ssh-add ../github_readonly.key 2>/dev/null ; git diff " LASTREL " " REL "
#               print "<td><div id=\"" LASTREL "_full\" onclick=\"document.getElementById('\''" LASTREL "_full'\'').style.di
#               C=0
#               while ( ( CMD | getline result ) > 0 ) {
#                 print result "<br/>"
#                 C++
#               }
#               close(CMD);
#               print "</div>"
#               print "<div id=\"" LASTREL "_sum\" onclick=\"document.getElementById('\''" LASTREL "_sum'\'').style.display=
#               print "Files: " C ;
#               print "</div>"
#               print "</td><td>" COMMENT "</td></tr>";
#               COMMENT=""
#       }
#  /^commit .*$/ {
#               A=2;
#               LASTREL=REL;
#               gsub("commit ","")
#               REL=$0
#       };
#'  >>$mtmp
#echo "    </table>">>$mtmp
echo "  </body>">>$mtmp
echo "</html>">>$mtmp
mv $mtmp ${WWWDIR}/status.html
chmod 644 ${WWWDIR}/status.html
#chown www-data:www-data index.html

# create www directory
function addrow() {
        (
                cd ${WWWDIR}/$1
                find . -name "$2" |while read fn;
                do
                        der=${fn##./}
                        echo -n "<tr><td><a href=\"$1/$der\">$der</a></td><td>$3</td><td>"
                        der=${der%%\.*}
                        find . -name "$der*" |grep -v "$fn"|while read derivat
                        do
                                derivat=${derivat##*\.}
                                echo -n "<a href=\"$1/$der.$derivat\">$derivat</a>&nbsp;"
                        done
                        echo "</td></tr>"
                done
        )
}

#Echoi "Baking page"
#curl -s "https://get.sdkman.io" | bash &&  source "/root/.sdkman/bin/sdkman-init.sh" &&  sdk install jbake || exit 101
#jbake ${WWWDIR}/
cp -R website/target/jbake/* ${WWWDIR}/
cp $dir/../thesis/src/main/latex/rfc/draft-gwerder-*.{xml,pdf,ps,epub,mobi,txt,html} ${WWWDIR}/devel/
cp $dir/../thesis/src/main/latex/rfc/rfc2629.xslt ${WWWDIR}/devel/
(cd $dir/../application-core-library/src/main/asn/;zip -9 ${WWWDIR}/devel/MessageVortex_definition.zip MessageVortex-*.asn)
for i in $dir/../application-core-library/src/main/asn/MessageVortex*.asn
do
	cat $i
	echo ""
	echo ""
done >${WWWDIR}/devel/MessageVortex_definition.asn
tbl="<table>\r\n"
tbl="$tbl<tr><th>Filename</th><th>Description</th><th>Derivatives</th></tr>\r\n"
tbl="$tbl$(addrow devel 'draft-gwerder-*.xml' 'RFC draft document' )"
tbl="$tbl$(addrow devel 'MessageVortex_definition*.asn' 'ASN.1 style definition of the MessageVortex messages' )"
tbl="$tbl</table>\r\n"
t=$(cat ${WWWDIR}/documentation.html)
sedcom="${t/<!--development-->/$tbl}"
echo -n -e "$sedcom" >${WWWDIR}/documentation.html.new
if [ -s ${WWWDIR}/documentation.html.new ]
then
	mv ${WWWDIR}/documentation.html.new ${WWWDIR}/documentation.html
else 
	echo "ERROR: insert of table failed"
fi	
