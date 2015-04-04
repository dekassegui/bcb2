#!/bin/bash
#
# Script p/montagem do projeto, alternativo ao uso do ANT + build file.
#
# criação ou reinicio do diretório destino da compilação
[[ -d build ]] && rm -fr build/* || mkdir build
# criação e preenchimento do diretório das dependências (libraries)
source mkLinks.sh
declare -a dependencias=( lib/*.jar )
# compilação
PREV_IFS="$IFS"
IFS=':'
javac -cp "${dependencias[*]}" -d build/ -Xlint -Werror sources/neo/*.java
IFS="$PREV_IFS"
# criação de arquivo MANIFEST "a priori"
MANIFEST='/tmp/manifest.txt'
cat > $MANIFEST <<DOC
Main-Class: neo.ClienteBCB
Class-Path: ${dependencias[*]//lib\//}
DOC
# criação do jarfile desse projeto
jar cvfm "lib/bcb.jar" $MANIFEST resources/ cfg/ -C build/ . > /dev/null 2>&1
