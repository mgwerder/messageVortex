#!/bin/sh

dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
(
        cd $dir/..
        mvn clean package ${MAVEN_ARGS}
        $dir/mkindex.sh
)
