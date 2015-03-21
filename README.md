# bcb2

API em Java para consultas ao web service <a href="http://www4.bcb.gov.br/pec/series/port/aviso.asp" title="link para o Sistema Gerenciador de Séries Temporais no website do Banco Central do Brasil">Sistema Gerenciador de Séries Temporais do Banco Central do Brasil</a> (aka SGS do BCB) via Apache Axis2.

### Dependências

<ol>
<li><a href="http://axis.apache.org/axis2/java/core/" title="link para o website do Apache Axis2">Apache Axis2</a></li>
<li><a href="https://ant.apache.org/" title="link para o website do Apache ANT">Apache ANT</a> para montagem da API</li>
</ol>

<ul>
<li>Importante: Instale o JDK e o Axis2 no diretório <code>/opt</code> ou modifique os arquivos <a href="https://github.com/dekassegui/bcb2/blob/master/build.xml">build.xml</a> e <a href="https://github.com/dekassegui/bcb2/blob/master/mkLinks.sh">mkLinks.sh</a>.</li>
</ul>

### Montagem da API

   <code>prompt% <strong>ant</strong></code>

### Consulta ao SGS do BCB

   <code>prompt% <strong>./bcb.sh</strong> código-da-série-temporal <em>[data-inicial [data-final]]</em></code>

### Características
<ul>
<li>Licenciado sob <a href="https://www.gnu.org/licenses/gpl-3.0.html" title="link para o documento da GPLv3">GPLv3</a>.</li>
<li>Funcionamento multiplataforma: compilado e testado no Java7 e Java8.</li>
<li>Acesso ao web service por conexão segura (HTTPS).</li>
<li>Suporta todos os <em>formatos</em> de séries temporais disponibilizadas ao público.</li>
<li>Implementação simples e segura, possibilitando análise das consultas mal sucedidas.</li>
<li>Contém extensão opcional para extração otimizada de dados textuais dos documentos requisitados.</li>
<li>Código fonte comentado e adequado para criação da documentação.</li>
</ul>
