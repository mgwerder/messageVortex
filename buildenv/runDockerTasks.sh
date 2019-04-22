#!/bin/bash

dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
(
        cd $dir/..
        echo "dir is $dir (script is $0)"
        export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/ 
        export MAVEN_CONFIG=/var/lib/maven/ 
        export MAVEN_OPTS="-Xmx2048m -XX:MaxPermSize=512m -XX:MaxDirectMemorySize=512m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8" 
        mvn jar:jar package ${MAVEN_ARGS}
        $dir/mkindex.sh
)
