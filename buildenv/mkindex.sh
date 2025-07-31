#!/bin/bash
XML2RFC=/usr/local/bin/xml2rfc
WWWDIR=/var/www/messagevortex/
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"

opts=`getopt -o hvgfn --long help,verbose,nogitupdate,force,noupdate -n 'mkindex.sh' -- "$@"`

function help() {
	(
		echo "usage: $0 [-h|--help] [-v|--verbose] [-g,--nogitupdate] [-f|--force]"
		echo ""
		echo "   -h|--help        Display this help text"
		echo "   -v|--verbose     Print more output while processing files"
		echo "   -g|--nogitupdate Force build but skip updating sources from git"
		echo "   -f|--force       Reset repository before doing a forced build"
	) 2>&1
	exit 101
}

if [ $? != 0 ] 
then 
	echo "ERROR processin parameters"
	help
fi

# Note the quotes around `$TEMP': they are essential!
eval set -- "$opts"

VERBOSE=false
GITUPDATE=true
FORCE=false
UPDATE=true
while true; do
  case "$1" in
    -v | --verbose ) VERBOSE=true; shift ;;
    -h | --help ) help ;;
    -g | --nogitupdate ) GITUPDATE=false; shift ;;
    -f | --force ) FORCE=true ; shift ;;
    -n | --noupdate ) UPDATE=false ; shift ;;
    -- ) shift; break ;;
    * ) break ;;
  esac
done

if [ $# -ge 1 ]
then 
	echo "ERROR: Unknown parameter $1"
	help
fi 

cd $SCRIPTPATH
if [[ "$FORCE" == "false" || "$GITUPDATE" == "false" ]]
then
	crev=$( (cd phd;ssh-agent bash -c 'ssh-add ../github_readonly.key 2>/dev/null; git fetch --all >/dev/null; git pull >/dev/null; git log -1') | grep "commit" | head -n 1 | sed 's/commit *//gi' )
	orev=$(grep '<!-- crev:' /var/www/messagevortex/status.html|sed 's~.*crev: *~~;s~  *.*~~' )
	if [[ "X$orev" == "X$crev" && "X$1" == "X" && "X$crev" != "X" ]]; 
	then 
		echo "no update needed $crev=$orev"
		logger -t mkindex.sh "aborted generation as there is no new change pending ($crev)" 
		touch /var/www/messagevortex/status.html
		(cd /var/www/messagevortex/;sed "s~<p>last update.*~<p>last update $(date)</p>~" <status.html >status.html.new && mv status.html.new status.html)
		exit
	fi
	echo "update due."
	echo "orev=$orev"
	echo "crev=$crev"
else
	echo "update forced"
fi

# exit

if [[ "$GITUPDATE" == "true" ]]
then
	if [[ "$FORCE" == "true" ]]
	then
		(cd phd;ssh-agent bash -c 'ssh-add ../github_readonly.key 2>/dev/null; git reset --hard origin/master; git clean -f -d' >/dev/null)
	fi	
	(cd phd;ssh-agent bash -c 'ssh-add ../github_readonly.key 2>/dev/null; git pull' >/dev/null )
fi

echo "creating and running build env"
timeout 3h $SCRIPTPATH/phd/buildenv/build.sh -e MAVEN_ARGS='-DskipTests' && echo "successfully finished building in docker" && \
cp $SCRIPTPATH/phd/target/thesis/main/latex/messageVortex*.pdf $SCRIPTPATH/phd/target/www/devel/
pdftk $SCRIPTPATH/phd/target/www/devel/messageVortex.pdf output - >/dev/null && \
if [[ -f $SCRIPTPATH/phd/target/www/status.html && -f $SCRIPTPATH/phd/target/www/documentation.html ]]
then
	rm -r phd/target/www/devel/repo/ 2>/dev/null
	(cd phd/target/www/devel/ && ssh-agent bash -c 'ssh-add ../../../../github_readonly.key 2>/dev/null; git clone git@github.com:mgwerder/messageVortex_internal.git repo' >/dev/null)
	
	if [[ "$UPDATE" == "true" ]]
	then
		echo "copy messagevortex page" 
		(cd $WWWDIR/devel; tar -cf /var/tmp/messagevortex_save.tar artifacts)
		rm -r $WWWDIR/*
		mkdir $WWWDIR/devel
		mkdir $WWWDIR/devel/artifacts
		cp -R  $SCRIPTPATH/phd/target/www/* $WWWDIR
		chown -R root:www-data $WWWDIR
		chown -R jenkins:www-data $WWWDIR/devel/artifacts
		chmod -R ug-w $WWWDIR
		(cd $WWWDIR/devel; tar -xf /var/tmp/messagevortex_save.tar )
		rm /var/tmp/messagevortex_save.tar
		chmod -R u+w $WWWDIR/devel/artifacts
	
		# link repository
		mkdir $WWWDIR/repos
		mkdir $WWWDIR/repos/debian
		ln -s /root/messageVortexRepos/debian/aptly/development/public $WWWDIR/repos/debian/development
		ln -s /root/messageVortexRepos/debian/aptly/integration/public $WWWDIR/repos/debian/integration
		ln -s /root/messageVortexRepos/debian/aptly/master/public      $WWWDIR/repos/debian/stable	
	
		# update revision
		sed "s/<!-- *crev:.*-->/<!-- crev:$crev -->/gi" <$WWWDIR/status.html >$WWWDIR/status.html.new && mv $WWWDIR/status.html.new $WWWDIR/status.html
	else 
		echo "warning: skipped website update"
	fi
else 
	echo "skipped copy ... build seems to be failed"	
	exit 100
fi	

exit 0

