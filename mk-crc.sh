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

source ./mk-setup.sh

build_all "actions balancer configurer initdb"