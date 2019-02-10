#!/bin/sh

dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)

(
	cd $dir
	# build the image if needed
	sudo docker build -t messagevortexbuild .

	# stop old containers if needed
	sudo docker stop messageVortexBuild
	sudo docker rm messageVortexBuild

	sudo docker run -it --name messageVortexBuild --mount type=bind,source="$dir/..",target=/var/tmp/messagevortex messagevortexbuild:latest "$@"
)

