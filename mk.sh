#!/bin/bash
USE_CACHE=0

PRJ_IMAGEGROUP="abatalev"
PRJ_IMAGEVERSION="2021-10-28"
PRJ_MODULES="actions balancer configurer initdb"
CDIR=$(pwd)

function image_exist() {
    I_PRJ=$1
    V_PRJ=$2
    docker image list | grep $I_PRJ | awk ' {print $2;}' | grep $V_PRJ | wc -l
}

function image_build() {
    B_PRJ=$1
    I_PRJ=$2
    V_PRJ=$3
    D_PRJ=$4
    N_PRJ=$5
    if [ ${B_PRJ} == 0 ]; then
        echo "### build $N_PRJ"
        docker build -t $I_PRJ:$V_PRJ -f $D_PRJ .
        if [  $? -ne 0  ]; then exit 1; fi
    else 
        echo "### Build $N_PRJ skipped. Image $I_PRJ:$V_PRJ already exist"    
    fi
}

function save_image() {
    if [ ! -f cache/${1}-${3}.tar ]; then
        if [ ! -d cache ]; then
            mkdir cache
        fi
        echo "### CACHE save ${1}-${3}"
        docker save -o cache/${1}-${3}.tar ${2}:${3}
    fi
}

function load_image() {
    BX=$(image_exist $2 $3)
    if [ ${BX} != 1 ]; then
        if [ -f cache/${1}-${3}.tar ]; then
            echo "### CACHE load ${1}-${3}"
            docker load -i cache/${1}-${3}.tar
        fi
    fi
}

function build_project() {
    PRJ_NAME=$1
    PRJ_NEEDBUILD=1
    PRJ_IMAGE="${PRJ_IMAGEGROUP}/${PRJ_NAME}"
    PRJ_VERSION="${PRJ_IMAGEVERSION}"
    
    cd "${CDIR}"
    if [ -f "prj2hash" ]; then
        PRJ_VERSION=$(./prj2hash -short build/${PRJ_NAME})
    fi
    load_image ${PRJ_NAME} ${PRJ_IMAGE} ${PRJ_VERSION}
    if [ -f "prj2hash" ]; then
        PRJ_NEEDBUILD=$(image_exist $PRJ_IMAGE $PRJ_VERSION)
    fi
    cd "${CDIR}/build"
    image_build $PRJ_NEEDBUILD $PRJ_IMAGE $PRJ_VERSION "Dockerfile.${PRJ_NAME}" "${PRJ_NAME}"
    if [ $USE_CACHE == 1 ]; then
        save_image ${PRJ_NAME} ${PRJ_IMAGE} ${PRJ_VERSION}
    fi
    cd "${CDIR}"
    IMAGE_VERSION=$PRJ_IMAGE:$PRJ_VERSION yq e -i ".services.${PRJ_NAME}.image = strenv(IMAGE_VERSION)" docker-compose.yaml
}

function build_all() {
    echo "### create docker-compose"
    cp docker-compose.tmpl docker-compose.yaml
    for prj_name in ${1}
    do
        build_project "${prj_name}"
    done

    cd "${CDIR}"
    echo "### launch docker-compose"
    docker-compose up --remove-orphans # --scale actions=2
}

build_all "${PRJ_MODULES}"