#!/bin/bash

#
HELM_NAME=stand
USE_CACHE=1

#
HELM_STATE="install"
#HELM_STATE="delete"

# new variables
B_ACTIONS=1
B_CONFIGURER=1
B_BALANCER=1
B_INITDB=1

I_GROUP="abatalev"
I_ACTIONS="${I_GROUP}/actions"
I_BALANCER="${I_GROUP}/balancer"
I_CONFIGURER="${I_GROUP}/configurer"
I_INITDB="${I_GROUP}/initdb"

V_ACTIONS="2021-10-28"
V_CONFIGURER="2021-10-28"
V_BALANCER="2021-10-28"
V_INITDB="2021-10-28"

# --------
#
# see also:
# * https://kubernetes.io/ru/docs/tasks/tools/install-minikube/
#
# --------

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

# => check status
minikube status > /dev/null
if [ $? != 0 ]; then
    echo "### Minikube status -- ($?). minikube starting"
    minikube start
fi 
minikube status > /dev/null
if [ $? != 0 ]; then
    echo "### Minikube status -- ($?). aborted"
    exit 1
fi
echo "### Minikube started"

# => setup environment
CDIR=$(pwd)
eval $(minikube -p minikube docker-env)

E_NAMESPACE=$(kubectl get namespaces | grep stand | wc -l)
if [ $E_NAMESPACE == 0 ]; then
   echo "create namespace"
   kubectl create -f minikube/stand-namespace.yaml
fi
kubectl config set-context $(kubectl config current-context) --namespace=stand

if [ -f "prj2hash" ]; then
    V_ACTIONS=$(./prj2hash -short build/actions)
    V_CONFIGURER=$(./prj2hash -short build/configurer)
    V_BALANCER=$(./prj2hash -short build/balancer)
    V_INITDB=$(./prj2hash -short build/initdb)
fi

load_image actions ${I_ACTIONS} ${V_ACTIONS}
load_image balancer ${I_BALANCER} ${V_BALANCER}
load_image configurer ${I_CONFIGURER} ${V_CONFIGURER}
load_image initdb ${I_INITDB} ${V_INITDB}

if [ -f "prj2hash" ]; then
    B_ACTIONS=$(image_exist $I_ACTIONS $V_ACTIONS)
    B_CONFIGURER=$(image_exist $I_CONFIGURER $V_CONFIGURER)
    B_BALANCER=$(image_exist $I_BALANCER $V_BALANCER)
    B_INITDB=$(image_exist $I_INITDB $V_INITDB)
fi

cd "${CDIR}/build"

image_build $B_ACTIONS $I_ACTIONS $V_ACTIONS Dockerfile.actions "actions"
image_build $B_CONFIGURER $I_CONFIGURER $V_CONFIGURER Dockerfile.configurer "configurer"
image_build $B_BALANCER $I_BALANCER $V_BALANCER Dockerfile.balancer "balancer"
image_build $B_INITDB $I_INITDB $V_INITDB Dockerfile.initdb "initdb"

cd "${CDIR}"

if [ $USE_CACHE == 1 ]; then
    save_image actions ${I_ACTIONS} ${V_ACTIONS}
    save_image balancer ${I_BALANCER} ${V_BALANCER}
    save_image configurer ${I_CONFIGURER} ${V_CONFIGURER}
    save_image initdb ${I_INITDB} ${V_INITDB}
else 
    echo "### CACHE disabled"    
fi

IMAGE_VERSION=$I_ACTIONS yq e -i '.actions-chart.image.repository = strenv(IMAGE_VERSION)' ./helm/stand-chart/values.yaml
IMAGE_VERSION=$V_ACTIONS yq e -i '.actions-chart.image.tag = strenv(IMAGE_VERSION)' ./helm/stand-chart/values.yaml
IMAGE_VERSION=$I_CONFIGURER yq e -i '.configurer-chart.image.repository = strenv(IMAGE_VERSION)' ./helm/stand-chart/values.yaml
IMAGE_VERSION=$V_CONFIGURER yq e -i '.configurer-chart.image.tag = strenv(IMAGE_VERSION)' ./helm/stand-chart/values.yaml
IMAGE_VERSION=$I_BALANCER yq e -i '.balancer-chart.image.repository = strenv(IMAGE_VERSION)' ./helm/stand-chart/values.yaml
IMAGE_VERSION=$V_BALANCER yq e -i '.balancer-chart.image.tag = strenv(IMAGE_VERSION)' ./helm/stand-chart/values.yaml
IMAGE_VERSION=$I_INITDB yq e -i '.initdb-chart.image.repository = strenv(IMAGE_VERSION)' ./helm/stand-chart/values.yaml
IMAGE_VERSION=$V_INITDB yq e -i '.initdb-chart.image.tag = strenv(IMAGE_VERSION)' ./helm/stand-chart/values.yaml

echo "### HELM: update dependency  ============================================"
helm dependency update helm/stand-chart

HELM_EXISTS=$(helm ls | grep "${HELM_NAME}" | wc -l --)
if [ ${HELM_EXISTS} == 0 ]; then
    if [ "${HELM_STATE}" == "install" ]; then
        echo "### HELM: install  ====================================================="
        helm install ${HELM_NAME} helm/stand-chart
    fi
else 
    if [ "${HELM_STATE}" == "delete" ]; then
        echo "### HELM: delete  ======================================================"
        helm delete ${HELM_NAME}
    fi
    if [ "${HELM_STATE}" == "install" ]; then
        echo "### HELM: upgrade  ====================================================="
        helm upgrade ${HELM_NAME} helm/stand-chart
    fi
fi

echo "### Kubernetes: status ================================================="
kubectl get all
echo "### ===================================================================="
