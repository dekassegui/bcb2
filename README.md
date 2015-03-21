# bcb2

API em Java para consultas ao web service <a href="http://www4.bcb.gov.br/pec/series/port/aviso.asp" title="clique para acessar o Sistema Gerenciador de Séries Temporais no website do Banco Central do Brasil">Sistema Gerenciador de Séries Temporais do Banco Central do Brasil</a> (aka SGS do BCB) via **Apache Axis2** que substitui a API obsoleta *Apache Axis*, cujo ciclo de desenvolvimento foi encerrado pelo fabricante.

### Dependências

<ol>
<li><a href="http://axis.apache.org/axis2/java/core/" title="clique para acessar o website do Apache Axis2">Apache Axis2</a></li>
<li><a href="https://ant.apache.org/" title="clique para acessar o website do Apache ANT">Apache ANT</a> para montagem da API</li>
</ol>

### Montagem da API

Instale o **Axis2** no diretório **/opt** ou modifique o arquivo <a href="https://github.com/dekassegui/bcb2/blob/master/mkLinks.sh" title="clique para acessar o script para criação/preenchimento do diretório das libraries desse projeto">mkLinks.sh</a> e então execute o comando:

   <code>prompt% <strong>ant</strong></code>

### Consulta ao SGS do BCB (*via linha de comando*)

   <code>prompt% <strong>./bcb.sh</strong> código-da-série-temporal {data-inicial {data-final}}</code>

### Características

<ul>
<li>Licenciado sob <a href="https://www.gnu.org/licenses/gpl-3.0.html" title="clique para acessar o documento da GPLv3">GPLv3</a>.</li>
<li>Funcionamento multiplataforma e multiversão (compilado/testado com Java7 e Java8).</li>
<li>Acesso ao web service por conexão segura (HTTPS).</li>
<li>Suporta todos os formatos de séries temporais disponibilizadas ao público.</li>
<li>Implementação simples e segura, possibilitando análise das consultas mal sucedidas.</li>
<li>Código fonte comentado e adequado para criação da documentação dessa API.</li>
</ul>
