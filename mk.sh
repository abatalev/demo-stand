#!/bin/bash
B_ACTIONS=1
B_CONFIGURER=1
B_BALANCER=1
B_INITDB=1

V_ACTIONS="2021-10-28"
V_CONFIGURER="2021-10-28"
V_BALANCER="2021-10-28"
V_INITDB="2021-10-28"

function image_exist() {
    docker image list | grep ${1} | awk ' {print $2;}' | grep ${2} | wc -l
}

if [ -f "prj2hash" ]; then
    V_ACTIONS=$(./prj2hash -short build/actions)
    V_CONFIGURER=$(./prj2hash -short build/configurer)
    V_BALANCER=$(./prj2hash -short build/balancer)
    V_INITDB=$(./prj2hash -short build/initdb)

    B_ACTIONS=$(image_exist abatalev/actions $V_ACTIONS)
    B_CONFIGURER=$(image_exist abatalev/configurer $V_CONFIGURER)
    B_BALANCER=$(image_exist abatalev/balancer $V_BALANCER)
    B_INITDB=$(image_exist abatalev/initdb $V_INITDB)
fi

CDIR=$(pwd)
cd "${CDIR}/build"
if [ ${B_ACTIONS} == 0 ]; then
    echo "### build actions"
    docker build -t abatalev/actions:$V_ACTIONS -f Dockerfile.actions .
    if [  $? -ne 0  ]; then exit 1; fi
else 
   echo "### build actions skipped.    $V_ACTIONS"    
fi
if [ ${B_CONFIGURER} == 0 ]; then
    echo "### build configurer"
    docker build -t abatalev/configurer:$V_CONFIGURER -f Dockerfile.configurer .
    if [  $? -ne 0  ]; then exit 1; fi;
else 
   echo "### build configurer skipped. $V_CONFIGURER"    
fi
if [ ${B_BALANCER} == 0 ]; then
    echo "### build balancer"
    docker build -t abatalev/balancer:$V_BALANCER -f Dockerfile.balancer .
    if [  $? -ne 0  ]; then exit 1; fi;
else 
   echo "### build balancer skipped.   $V_BALANCER"    
fi
if [ ${B_INITDB} == 0 ]; then
    echo "### build initdb"
    docker build -t abatalev/initdb:$V_INITDB -f Dockerfile.initdb .
    if [  $? -ne 0  ]; then exit 1; fi;
else 
   echo "### build initdb skipped.     $V_INITDB"    
fi

cd "${CDIR}"
echo "### create docker-compose"
cp docker-compose.tmpl docker-compose.yaml
sed -i "" "s/XX_ACTIONS_XX/${V_ACTIONS}/g" docker-compose.yaml
sed -i "" "s/XX_CONFIGURER_XX/${V_CONFIGURER}/g" docker-compose.yaml
sed -i "" "s/XX_BALANCER_XX/${V_BALANCER}/g" docker-compose.yaml
sed -i "" "s/XX_INITDB_XX/${V_INITDB}/g" docker-compose.yaml

echo "### launch docker-compose"
docker-compose up # --scale actions=2