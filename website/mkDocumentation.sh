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
                        r="<tr><td><a href=\"$1/$der\">$der</a></td><td>$3</td><td>"
                        der=${der%%\.*}
                        r="$r$(find . -name "$der*" |grep -v "$fn"|sort|while read derivat
                        do
                                derivat=${derivat##*\.}
                                echo "<a href=\"$1/$der.$derivat\">$derivat</a>&nbsp;"
                        done)"
                        r="$r</td></tr>"
                        echo "$r"
                done
        )
}

mkdir -p ${WWWDIR}/devel/ 2>/dev/null
(cd $dir/../application-core-library/target/apidocs; zip -9ur $dir/target/messageVortex_apidoc.zip .)
cp $dir/../rfc/src/xml2rfc/draft-gwerder-*.xml  ${WWWDIR}/devel/
cp $dir/target/messageVortex_apidoc.zip ${WWWDIR}/devel/
(cd ${WWWDIR}/devel/; mkdir apidoc 2>/dev/null; cd apidoc; unzip -qox ..//messageVortex_apidoc.zip)
cp $dir/../rfc/target/xml2rfc/draft-gwerder-*.{xmlflat,pdf,ps,epub,mobi,txt,legacytxt,rawtxt,html} ${WWWDIR}/devel/
cp $dir/../rfc/src/xml2rfc/rfc2629.xslt ${WWWDIR}/devel/
cp $dir/../thesis/target/main/latex/messageVortex.pdf ${WWWDIR}/devel/
(cd $dir/../application-core-library/src/main/asn/;zip -9 ${WWWDIR}/devel/MessageVortex_definition.zip MessageVortex-*.asn)
for i in $dir/../application-core-library/src/main/asn/MessageVortex*.asn
do
	cat $i
	echo ""
	echo ""
done >${WWWDIR}/devel/MessageVortex_definition.asn
tbl="<table>\n"
tbl="$tbl<tr><th>Filename</th><th>Description</th><th>Derivatives</th></tr>\n"
tbl="$tbl$(
	find ${WWWDIR}/devel/ -name 'draft-gwerder-*.xml' |sort -r |while read rfc;
	do 
		rfc=$(basename "$rfc")
		echo "$(addrow devel $rfc 'RFC draft document' )"
	done	
)"
tbl="$tbl$(addrow devel 'MessageVortex_definition*.asn' 'ASN.1 style definition of the MessageVortex messages' )"
tbl="$tbl<tr><td><a href=\"devel/apidoc\">Apidoc</a></td><td>Apidocs of the core library</td><td><a href=\"devel/messageVortex_apidoc.zip\">zip</a></td></tr>"
tbl="$tbl</table>\n"
t=$(cat $1)
sedcom="${t/<!--development-->/$tbl}"
echo -n -e "$sedcom" >$2
