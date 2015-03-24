#!/bin/bash
#
# Script p/montagem do projeto, alternativo ao uso do ANT + build file.
#
# criação e preenchimento do diretório das dependências (libraries)
source mkLinks.sh
# criação do classpath de compilação
declare -a jarFiles
for f in lib/*.jar; do
  jarFiles=( ${jarFiles[*]} $f )
done
jarFiles=$( echo ${jarFiles[*]} | tr ' ' ':' )
# criação da lista de arquivos fonte
declare -a sourceFiles
for f in BCBCliente ClienteBCB SafeDateFormat; do
  sourceFiles=( ${sourceFiles[*]} "sources/neo/${f}.java" )
done
# criação ou reinicio do diretório destino da compilação
[[ -d build ]] && rm -fr build/* || mkdir build
# compilação
javac -cp $jarFiles -d build/ -Xlint -Werror ${sourceFiles[*]}
# criação de arquivo MANIFEST "a priori"
cat > /tmp/manifest.txt <<DOC
Main-Class: neo.ClienteBCB
Class-Path: $( echo $jarFiles | sed 'y/:/ /; s|lib/||g' )
DOC
# criação do principal jarfile desse projeto
jar cvfm "lib/bcb.jar" /tmp/manifest.txt resources/ cfg/ -C build/ .
