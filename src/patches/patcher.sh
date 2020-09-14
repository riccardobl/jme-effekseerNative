#!/bin/bash
set -e
function replace {
    source=$1
    search=$2
    content=$3
    php -r "file_put_contents('$source', str_replace('$search', file_get_contents('$content') , file_get_contents('$source')));"

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
    replace "$f" "SOFT_PARTICLES.HEADER.gl" "$cdir/softParticles.HEADER.gl.glsl"
    replace "$f" "SOFT_PARTICLES.BODY.gl" "$cdir/softParticles.BODY.gl.glsl"
done
unset IFS