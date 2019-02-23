#!/bin/bash

dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)

(
	cd $dir
	# build the image if needed
	if [[ ! -f mavenfiles.tar ]]
	then 
		(
			cd $dir/..
			tar -cf buildenv/mavenfiles.tar $(find . -name 'pom.xml')
		)	
	fi
	if [[ "$(find $dir/.. -newer mavenfiles.tar -name "pom.xml")" != "" ]]
	then
		(
			cd $dir/..
			tar -cf buildenv/mavenfiles.tar $(find . -name 'pom.xml')
		)	
	fi
	sudo docker build -t messagevortexbuild . && \
	tmpdir=$(mktemp -d -p $dir/..) && \
	(
		echo "temp directory for build is $tmpdir"
		echo "script dir is $dir"
		mkdir -p $tmpdir/buildenv 
		mkdir -p $tmpdir/.git
		cp $dir/../pom.xml $tmpdir
		for i in application-core-library thesis website rfc
		do
			mkdir $tmpdir/$i
			mkdir $tmpdir/$i/src
			cp $dir/../$i/pom.xml $tmpdir/$i
			cp $dir/../$i/*.sh $tmpdir/$i 2>/dev/null
		done
		ls -la $tmpdir/*
	) && \
	trap "rm -rf $tmpdir" EXIT && \
	id=$(sudo docker create -t \
	     --mount type=bind,source="$tmpdir/",target=/var/tmp/messagevortex/ \
	     --mount type=bind,source="$dir/../.git/",target=/var/tmp/messagevortex/.git/ \
	     --mount type=bind,source="$dir/../thesis/src/",target=/var/tmp/messagevortex/thesis/src/ \
	     --mount type=bind,source="$dir/../website/src/",target=/var/tmp/messagevortex/website/src/ \
	     --mount type=bind,source="$dir/../rfc/src/",target=/var/tmp/messagevortex/rfc/src/ \
	     --mount type=bind,source="$dir/../buildenv/",target=/var/tmp/messagevortex/buildenv/ \
	     --mount type=bind,source="$dir/../application-core-library/src/",target=/var/tmp/messagevortex/application-core-library/src/ \
	     "$@" messagevortexbuild:latest) && \
	echo "Created container with ID $id" && \
	sudo docker start -a $id && \
	(
		sudo docker rm $id
		if [[ "$tmpdir" != "" ]]
		then
			rm -r $dir/../target
			mv $tmpdir/target $dir/../target
			rm -rf $tmpdir
		fi	
	)
	
)

