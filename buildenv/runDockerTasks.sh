#!/bin/sh

dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
(
        cd $dir/..
        echo "dir is $dir (script is $0)"
        MAVEN_CONFIG=/var/lib/maven/ MAVEN_OPTS="-Xmx2048m -XX:MaxPermSize=512m -XX:MaxDirectMemorySize=512m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8" mvn package ${MAVEN_ARGS}
        $dir/mkindex.sh
)
