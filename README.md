# bcb2

API em Java para acesso ao Sistema Gerenciador de Séries Temporais do Banco Central do Brasil (aka SGS do BCB)
via Apache Axis2.

Dependências: Apache Axis2 + Apache ANT (p/compilação da API).

Nota: Instale o JDK e o Axis2 em /opt ou modifique o arquivo build.xml e mkLinks.sh.

Montagem da API:

 prompt% ant 

Consulta ao BCB:

 prompt% ./bcb.sh <código-série> [<data-inicial> [<data-final>]] 

Compilado e testado no Java7 e Java8.

