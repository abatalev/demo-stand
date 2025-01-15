#!/bin/bash

# ----------------------------
#
# $ crc delete
# $ crc cleanup
# $ crc setup
# $ crc start --log-level debug
#
# ----------------------------

USE_CACHE=1
USE_COMPOSE=0
USE_MINIKUBE=0
USE_OPENSHIFT=1
USE_PODMAN=1

HELM_NAME=stand

HELM_STATE="install"
#HELM_STATE="delete"

PRJ_IMAGEGROUP="abatalev"
PRJ_IMAGEVERSION="2021-10-28"

CDIR=$(pwd)

function setup_mklib() {
    if [ ! -f tools/mklib/mklib.sh ]; then
        if [ -d tools ]; then
            mkdir tools
        fi
        cd tools
        git clone http://localhost:3000/andrey/mklib.git
    fi
}

setup_mklib
cd ${CDIR}

source ${CDIR}/tools/mklib/mklib.sh

build_all "actions balancer configurer initdb"