#!/bin/bash
dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
WWWDIR=$(cd -P -- $(dirname -- "$2") && pwd -P)

# create www directory
function addrow() {
        (
                cd ${WWWDIR}/$1
                find . -name "$2" |while read fn;
                do
                        der=${fn##./}
                        echo -n "<tr><td><a href=\"$1/$der\">$der</a></td><td>$3</td><td>"
                        der=${der%%\.*}
                        find . -name "$der*" |grep -v "$fn"|sort|while read derivat
                        do
                                derivat=${derivat##*\.}
                                echo -n "<a href=\"$1/$der.$derivat\">$derivat</a>&nbsp;"
                        done
                        echo "</td></tr>"
                done
        )
}

mkdir -p ${WWWDIR}/devel/ 2>/dev/null
cp $dir/../rfc/src/xml2rfc/draft-gwerder-*.xml  ${WWWDIR}/devel/
cp $dir/../rfc/target/xml2rfc/draft-gwerder-*.{xmlflat,pdf,ps,epub,mobi,txt,html} ${WWWDIR}/devel/
cp $dir/../rfc/src/xml2rfc/rfc2629.xslt ${WWWDIR}/devel/
(cd $dir/../application-core-library/src/main/asn/;zip -9 ${WWWDIR}/devel/MessageVortex_definition.zip MessageVortex-*.asn)
for i in $dir/../application-core-library/src/main/asn/MessageVortex*.asn
do
	cat $i
	echo ""
	echo ""
done >${WWWDIR}/devel/MessageVortex_definition.asn
tbl="<table>\n"
tbl="$tbl<tr><th>Filename</th><th>Description</th><th>Derivatives</th></tr>\n"
find ${WWWDIR}/devel/ -name 'draft-gwerder-*.xml' |sort -r |while read rfc;
do 
	tbl="$tbl$(addrow devel $rfc 'RFC draft document' )"
done	
tbl="$tbl$(addrow devel 'MessageVortex_definition*.asn' 'ASN.1 style definition of the MessageVortex messages' )"
tbl="$tbl</table>\n"
t=$(cat $1)
sedcom="${t/<!--development-->/$tbl}"
echo -n -e "$sedcom" >$2
