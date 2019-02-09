#!/bin/sh

dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
(
        cd $dir/..
        mvn clean package
        $dir/mkindex.sh
)
