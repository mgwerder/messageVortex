#!/bin/sh

dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)

(
	cd $dir
	# build the image if needed
	sudo docker build -t messagevortexbuild . && \
	id=$(sudo docker create -t --mount type=bind,source="$dir/..",target=/var/tmp/messagevortex "$@" messagevortexbuild:latest) && \
	echo "Created container with ID $id" && \
	sudo docker start -a $id && \
	sudo docker rm $id
)

