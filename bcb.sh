#!/bin/bash
#
1>/dev/null pushd $(dirname $0)
BCB='lib/bcb.jar'
LOG4J='log4j.configuration=file:cfg/log4j.properties'
if [[ $1 =~ ^neo.(BCBCliente|ClienteBCB)$ ]]; then
  java -D$LOG4J -cp $BCB $*
else
  java -D$LOG4J -jar $BCB $*
fi
1>/dev/null popd
