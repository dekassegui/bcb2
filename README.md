# bcb2

API programada na linguagem/tecnologia Java para consultas ao web service <a href="http://www4.bcb.gov.br/pec/series/port/aviso.asp" title="clique para acessar o Sistema Gerenciador de Séries Temporais no website do Banco Central do Brasil">Sistema Gerenciador de Séries Temporais do Banco Central do Brasil</a> (aka SGS do BCB) via **Apache Axis2** que substitui a API obsoleta *Apache Axis*.

### Características

<ul>
<li>Funcionamento multiplataforma e multiversão (compilado/testado com Java7 e Java8).</li>
<li>Suporta todos os formatos de séries temporais disponibilizadas ao público.</li>
<li>Acesso ao web service por conexão segura (HTTPS).</li>
<li>Implementação simples e segura, possibilitando análise das consultas mal sucedidas.</li>
<li>Código fonte comentado e adequado para criação da documentação dessa API.</li>
<li>Licenciado sob <a href="https://www.gnu.org/licenses/gpl-3.0.html" title="clique para acessar o documento da GPLv3">GPLv3</a>.</li>
</ul>

### Dependências

<ol>
<li><a href="http://axis.apache.org/axis2/java/core/" title="clique para acessar o website do Apache Axis2">Apache Axis2</a></li>
<li><a href="https://ant.apache.org/" title="clique para acessar o website do Apache ANT">Apache ANT</a> <em><strong>opcional</strong></em> para montagem da API, também disponível nos repositórios Linux.</li>
</ol>

**Nota:** Use **Axis2 1.6.2** disponível em <a href="http://archive.apache.org/dist/axis/axis2/java/core/1.6.2/" title="arquivo do Axis2 1.6.2 no repositório do Apache/Axis">index of dist/axis/axis2/java/core/1.6.2</a> -- *última versão do Axis com RPC*.

**Dica:** <a href="https://linuxize.com/post/install-java-on-debian-10/" title="">How to Install Java on Debian 10 Linux > Installing OpenJDK 8</a>

### Montagem da API

Instale o **Axis2** no diretório **/opt** ou modifique o arquivo <a href="https://github.com/dekassegui/bcb2/blob/master/mkLinks.sh" title="clique para acessar o script para criação/preenchimento do diretório das libraries desse projeto">mkLinks.sh</a> e então execute o comando:

   <code>prompt% <strong>ant</strong></code>

ou alternativamente, sem uso do Apache ANT, execute o comando:

   <code>prompt% <strong>./build.sh</strong></code>

### Consulta ao SGS do BCB (*via linha de comando*)

##### Consulta propriamente dita, tal que argumentos entre parenteses são opcionais:

   <code>prompt% <strong>./bcb.sh</strong> código-da-série-temporal {data-inicial {data-final}}</code>

##### Exemplos de consultas:

(1) Imprime **help** do scritp de consulta

   <code>prompt% <strong>./bcb.sh</strong></code>

(2) Informação sobre a série de código 001 (<em>Dollar Americano</em>)

   <code>prompt% <strong>./bcb.sh</strong> 001</code>

(3) Consulta série do Dollar Americano de 01/03/2015 a 28/03/2015

   <code>prompt% <strong>./bcb.sh</strong> 001 01/03/2015 28/03/2015</code>

