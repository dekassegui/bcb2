#!/bin/bash
# Cria os links dos seguintes arquivos em AXIS_HOME/lib/:
#   activation-1.1.jar axiom-api-1.2.13.jar axiom-impl-1.2.13.jar
#   axis2-adb-1.6.2.jar axis2-kernel-1.6.2.jar axis2-transport-http-1.6.2.jar
#   axis2-transport-local-1.6.2.jar commons-codec-1.3.jar
#   commons-httpclient-3.1.jar commons-logging-1.1.1.jar httpcore-4.0.jar
#   mail-1.4.jar neethi-3.0.2.jar wsdl4j-1.6.2.jar XmlSchema-1.4.7.jar
#   log4j-1.2.17.jar
# em lib/ com linknames sem números de versão.
[[ -d lib ]] && rm -f lib/* || mkdir lib
AXIS_HOME='/opt/axis2-1.6.2/lib'
for pat in ac axiom-[ai] axis2-{adb,k*,t*}-[0-9] c*-{co,h,l} {h,ma,n,wsd,X,l}
do
  for f in $AXIS_HOME/${pat}*.jar
  do
    nome=$(basename $f)
    ln -s $f lib/${nome%-*}.jar
  done
done
