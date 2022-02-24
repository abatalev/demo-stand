#!/bin/bash

USE_CACHE=1
USE_COMPOSE=0
USE_MINIKUBE=1
USE_OPENSHIFT=0
USE_PODMAN=0

HELM_NAME=stand

HELM_STATE="install"
#HELM_STATE="delete"

PRJ_IMAGEGROUP="abatalev"
PRJ_IMAGEVERSION="2021-10-28"

source ./mklib.sh

build_all "actions balancer configurer initdb"