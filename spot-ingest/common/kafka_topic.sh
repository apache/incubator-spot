#!/bin/bash

ACTION=$1
TOPIC=$2
ZK=$3
PARTITIONS=$4

#-----------------------------------------------------------------------------------
# Validate parameters.
#-----------------------------------------------------------------------------------

USAGE="Usage:\n ${0} create topic_name zk_ip:zk_port num_of_partitions \n ${0} delete topic_name zk_ip:zk_port"

if [  -z $ACTION  ] || [ -z $TOPIC ]  ; then
    echo -e $USAGE
    exit 1
fi

#-----------------------------------------------------------------------------------
# Create/Delete topic.
#-----------------------------------------------------------------------------------

case "$ACTION" in 

    create)
        if [ -z $PARTITIONS ] || [ -z $ZK ]; then

            echo -e $USAGE
            exit 1           
        fi
        kafka-topics --create --partitions $PARTITIONS --replication-factor 1 --topic $TOPIC --zookeeper $ZK
        ;;

    delete)        
        if [  -z $ZK ]; then
            echo -e $USAGE
            exit 1
        fi
        kafka-topics --delete --topic $TOPIC --zookeeper $ZK        
        ;;

    *)
        echo -e $USAGE      
        exit 1    
esac

