#!/bin/bash
set -e
function replace {
    source=$1
    search=$2
    content=$3
    php -r "file_put_contents(realpath('$source'), str_replace('$search', file_get_contents(realpath('$content')) , file_get_contents(realpath('$source'))));"

}
cdir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
if [ "$EFFKSEER_DIR" = "" ];
then
    export EFFKSEER_DIR="./"
fi
cd "$EFFKSEER_DIR"
git apply "$cdir/softParticles.patch"
patchedFiles="`grep '+++'   "$cdir/softParticles.patch"`"
IFS=$'\n'       
for f in $patchedFiles; 
do 
    f=${f:6}
    echo "Complete patch in $f"
    relf=`realpath --relative-to="$PWD" "$f"`
    relcdir=`realpath --relative-to="$PWD" "$cdir"`
    
    replace "$relf" "SOFT_PARTICLES.HEADER.gl" "$relcdir/softParticles.HEADER.gl.glsl"
    replace "$relf" "SOFT_PARTICLES.BODY.gl" "$relcdir/softParticles.BODY.gl.glsl"
done
unset IFS