# Testes e CI do Projeto PIX

24/11/2026 - Maranhão vulgo Mara vulgo Samara vulgo Samaravilha

## Visão geral

A parte de testes garante que as regras do PIX continuem valendo a cada mudança no código, e o GitHub Actions confere isso sozinho em todo Pull Request. Responsável: Maranhão. Trabalho feito na branch `feat-ci`, criada a partir da `dev`.

**O que é testado:** a API REST do PIX (Javalin). O `./projeto.sh testar` compila o código da API e roda os testes sobre ela em duas camadas. Hoje os 45 testes exercitam os services, onde ficam as regras que a API expõe. A camada das rotas HTTP, que chama a API pelos endpoints, é o `ApiIntegracaoTest`, já escrito e ainda não executado.

O conjunto tem quatro peças, que se encaixam assim: o `projeto.sh` compila e roda os testes na máquina de qualquer pessoa, e o `ci.yml` chama esse mesmo script no servidor do GitHub.

| Peça | Onde fica | Função |
| --- | --- | --- |
| Testes JUnit | `test/` | Verificam as regras dos services e, depois, as rotas da API |
| Script único | `projeto.sh` | `rodar` sobe a API, `testar` compila e roda os testes |
| Workflow | `.github/workflows/ci.yml` | Roda tudo em PRs e pushes, e monta a imagem Docker |
| Lista de dependências | `pom.xml` | Diz quais bibliotecas baixar (Javalin, Jackson, slf4j) |

## Escopo: o que testamos

A API do PIX guarda tudo em memória (sem banco de dados), então os testes podem criar usuários, contas e chaves do zero em cada caso, sem precisar de nenhuma infraestrutura externa. O foco é nas regras de negócio, que são o que o professor e o grupo mais precisam ver funcionando.

**O que está coberto**

- Cadastro, edição e remoção de usuários (CPF e e-mail únicos, validações de formato).
- Contas: abertura, depósito, saldo, bloqueio e desbloqueio, extrato.
- Chaves PIX: limite de 5 por conta, chave CPF igual ao CPF do dono, chave aleatória não editável, suspensão e reativação.
- Transferências: limite de R$ 2.000 por PIX, saldo suficiente, conta bloqueada, chave suspensa, no máximo 2 casas decimais e estorno.
- Consultas de transações por usuário, data e período.

**O que ainda não está coberto**

- Rotas HTTP de ponta a ponta (o `ApiIntegracaoTest` já está escrito, mas ainda não foi colado no projeto nem executado).
- `Notificacao` e `ContatoService`.
- Classes de `model` isoladas e medição de cobertura de código.
- Login e rotas de administrador, que ficaram para a segunda etapa do projeto.

## Como rodar

Tudo passa por um único script na raiz do projeto, o `projeto.sh`. Pré-requisitos: Java (JDK 21 ou mais novo) e Maven instalados, ou o Maven baixado em `.tools/`. O Maven serve só para baixar as bibliotecas.

| Comando | O que faz |
| --- | --- |
| `./projeto.sh rodar` | Baixa as dependências (se faltar `lib/`), compila `src/` e sobe a API na porta 7070 |
| `./projeto.sh testar` | Compila `src/`, baixa o JUnit Console se faltar, compila `test/` e roda todos os testes |

Ao rodar os testes, o relatório em XML fica em `relatorio-testes/`. O resultado esperado hoje é **45 testes com sucesso e 0 falhas**.

Por dentro, o `testar` executa o equivalente a:

```
java -jar junit-platform-console-standalone-1.11.3.jar execute \
  --class-path "out:out-test:lib/*" \
  --scan-class-path out-test \
  --reports-dir relatorio-testes --disable-banner
```

As pastas `lib/`, `lib-test/`, `out/`, `out-test/` e `relatorio-testes/` são geradas e estão no `.gitignore`, então nunca vão para o repositório.

## Bibliotecas e ferramentas

| Ferramenta | Versão | Para que serve | Por que escolhemos |
| --- | --- | --- | --- |
| JUnit 5 (Jupiter) | 5.x, dentro do console | Escrever os testes com `@Test`, `@BeforeEach` e `assertEquals`, `assertThrows` etc. | É o padrão em Java, tem documentação de sobra e o GitHub Actions entende seu relatório |
| JUnit Platform Console Standalone | 1.11.3 | Um único `.jar` que descobre e executa os testes pela linha de comando | Evita precisar de Maven ou Gradle para rodar testes, o que combina com o projeto compilado com `javac` puro |
| Javalin | 6.7.0 | Servidor da API REST (Jetty 11 por baixo) | Já é a base da API; os testes de API o iniciam de verdade |
| Jackson Databind | 2.17.2 | Converte objetos Java em JSON e o contrário | Usado pelo Javalin |
| slf4j-simple | 2.0.17 | Saída de logs | Evita o aviso de logger ausente do Javalin |
| `java.net.http.HttpClient` | JDK | Faz as chamadas HTTP dentro do `ApiIntegracaoTest` | Já vem no Java, não adiciona dependência |
| Maven | 3.9.x | **Só baixa** as bibliotecas do `pom.xml` para `lib/` | O código não é compilado por ele |
| GitHub Actions | `checkout@v4`, `setup-java@v4`, `upload-artifact@v4` | Roda os testes e o build do Docker na nuvem | Gratuito para repositório e integrado ao GitHub do grupo |
| Docker | temurin 21 | Empacota a API (Dockerfile feito pelo Nick) | O CI só confere que a imagem ainda constrói |

## Decisões e justificativas

**Sem Maven no código.** O projeto já compilava com `javac` direto. Trocar isso a poucos dias da entrega mudaria a estrutura de pastas e o Dockerfile do Nick. O Maven ficou só como baixador de dependências.

**Testes fora de `src/`, na pasta `test/`.** Na primeira tentativa os testes ficaram em `src/test/`, e o `javac` da aplicação quebrou com 177 erros de JUnit não encontrado. Separando as pastas, a API compila sem JUnit e só o `testar` precisa dele.

**Services em memória facilitam o CI.** Como não há banco de dados, o servidor do GitHub não precisa subir nada além do Java. Cada teste cria seus próprios dados, então um teste não afeta o outro.

**Um único script, o `projeto.sh`.** Antes havia `executar.sh` e quatro `testar-*.sh` espalhados. Agora o mesmo comando serve no computador de qualquer integrante, na hora de apresentar para o professor e no CI.

**Os `testar-*.sh` antigos ficam por enquanto.** São scripts de fumaça com `curl`. Eles saem do repositório quando o `ApiIntegracaoTest` estiver passando, porque ele cobre o mesmo terreno.

**Testar primeiro os services, depois a API.** Os services concentram as regras do PIX e rodam em milissegundos. Os testes de API, que sobem o servidor numa porta livre (`app.start(0)`), vêm em seguida para garantir códigos HTTP e o JSON de erro padronizado.

**Docker no CI.** O job de Docker só faz `docker build`. O Dockerfile é do Nick e o CI avisa quando uma mudança no código ou no `pom.xml` quebrar a imagem.

**Nomes dos usuários nos testes.** Usamos Nick e Duda, no lugar de nomes genéricos, para o grupo reconhecer os casos facilmente.

## Casos de teste por classe

Total atual: **45 testes** em 4 classes, todos em `test/service/`.

### UsuarioServiceTest (10)

| Teste | O que garante |
| --- | --- |
| cadastrarGeraIdsSequenciais | Cada usuário novo recebe o próximo id |
| buscarPorIdEncontraUsuarioExistente | Busca por id devolve o usuário certo |
| buscarPorIdInexistenteRetornaVazio | Id que não existe não quebra, volta vazio |
| atualizarAlteraDadosEMantemCpf | Edição muda os dados mas o CPF permanece |
| atualizarUsuarioInexistenteRetornaFalse | Editar quem não existe falha sem erro |
| removerTiraUsuarioDaLista | Remoção realmente apaga |
| cadastrarSemSenhaUsaSenhaPadrao | Sem senha informada, vale a senha padrão |
| verificarSenhaAceitaCorretaERejeitaErrada | Conferência de senha |
| alterarSenhaInvalidaASenhaAntiga | Depois de trocar, a senha antiga não vale mais |
| buscarPorCpfOuEmailAceitaCpfOuEmailIgnorandoMaiusculas | Base do login: CPF ou e-mail, sem diferenciar maiúsculas |

### PixServiceTest (7)

| Teste | O que garante |
| --- | --- |
| cpfPrecisaTer11Digitos | Validação de chave CPF |
| cnpjPrecisaTer14Digitos | Validação de chave CNPJ |
| emailPrecisaTerArrobaEPonto | Validação de chave e-mail |
| telefonePrecisaTer10Ou11Digitos | Validação de chave telefone |
| tipoNaoDependeDeMaiusculasOuMinusculas | `cpf` e `CPF` valem igual |
| entradaNulaOuTipoDesconhecidoEhInvalida | Nulo ou tipo estranho é rejeitado |
| chaveAleatoriaEhUuidEDiferenteACadaChamada | Chave aleatória é um UUID e nunca repete |

### ContaServiceTest (12)

| Teste | O que garante |
| --- | --- |
| criarContaFicaDisponivelParaBusca | Conta criada pode ser consultada |
| atualizarContaTrocaONumero | Edição de número da conta |
| removerContaTiraDaLista | Remoção de conta |
| associarChaveValidaEncontraContaPelaChave | Chave válida liga a conta e permite achar a conta pela chave |
| associarChaveInvalidaOuEmContaInexistenteFalha | Chave inválida ou conta inexistente é recusada |
| mesmaChaveNaoPodeSerCadastradaDuasVezes | Unicidade da chave |
| alterarChaveTrocaOValor | Troca de valor da chave |
| suspenderEDesbloquearChaveMudaOStatus | Suspensão e reativação |
| transferirViaPixMoveOSaldoEntreContas | Caminho feliz da transferência por chave |
| transferirSemSaldoNaoAlteraNada | Transferência falha sem efeito colateral |
| transferirParaChaveSuspensaFalha | Não envia para chave suspensa |
| receberViaPixDepositaSoComChaveAtivaEValorPositivo | Recebimento exige chave ativa e valor positivo |

### TransacaoServiceTest (16)

| Teste | O que garante |
| --- | --- |
| transferenciaMoveSaldoERegistraNoHistorico | Saldo muda e a transação fica registrada |
| idsDasTransacoesSaoSequenciais | Ids de transação em sequência |
| valorZeroOuNegativoEhRejeitado | Valor inválido |
| limiteDe2000PorPixEhRespeitado | Teto de R$ 2.000 por PIX |
| saldoInsuficienteEhRejeitadoESemEfeito | Saldo insuficiente não altera nada |
| contaNaoPodeTransferirParaSiMesma | Origem e destino iguais |
| contasNulasSaoRejeitadas | Entradas nulas |
| contaBloqueadaNaoMovimenta | Conta bloqueada não transfere |
| consultarSaldoRetornaOSaldoDaConta | Consulta de saldo |
| estornoDevolveOValorEMarcaComoEstornada | Estorno devolve o dinheiro e muda o status |
| naoEstornaDuasVezesNemTransacaoInexistente | Estorno só uma vez |
| naoEstornaSeODestinoJaGastouOValor | Estorno exige saldo no destino |
| transacaoConcluidaNaoPodeSerCancelada | Transação concluída não volta atrás por cancelamento |
| filtrarPorDataSoTraTransacoesDaquelaData | Filtro por data |
| relatorioPorPeriodoValidaAsDatas | Período com datas inválidas é rejeitado |
| relatorioPorUsuarioIncluiQuemEnviouEQuemRecebeu | Histórico inclui envio e recebimento |

### ApiIntegracaoTest (planejado)

Já escrito, ainda não executado. Sobe a API de verdade numa porta livre e confere, por HTTP, códigos de status (200, 201, 400, 404, 409, 422) e trechos do JSON de erro, como `CPF_JA_CADASTRADO` e `LIMITE_EXCEDIDO`.

## Pipeline de CI

Arquivo: `.github/workflows/ci.yml`, workflow chamado **CI**.

**Quando roda**

- Em todo Pull Request para `dev` ou `main`.
- Em todo push para `dev`, `main`, `feat-*`, `feat/**` e `fix-*`.

**Fluxo**

1. Um push ou PR dispara o workflow.
2. Job `testes`: baixa o código (`actions/checkout@v4`), instala o Java 21 Temurin com cache do Maven (`actions/setup-java@v4`), roda `./projeto.sh testar` e, mesmo se falhar, guarda a pasta `relatorio-testes/` como artefato (`actions/upload-artifact@v4`).
3. Job `docker`: só começa se `testes` passou (`needs: testes`) e roda `docker build -t api-pix .`.
4. O PR mostra ✓ verde ou ✗ vermelho. Se um dos jobs falha, o outro não esconde o problema.

**Por que usar o mesmo script**

O CI chama `./projeto.sh testar`, o mesmo comando que qualquer pessoa roda no computador. Se passa na máquina de um, passa no GitHub; se quebra no GitHub, dá para reproduzir localmente.

**Como ver o resultado**

No GitHub, aba **Actions**, escolher a execução e abrir o job. O relatório fica na seção de artefatos, no fim da página da execução.

## Problemas encontrados

| Problema | Causa | Solução |
| --- | --- | --- |
| A `dev` não compilava | Faltavam imports em `Usuario.java` (`StandardCharsets`, `MessageDigest`, `NoSuchAlgorithmException`) e o método `buscarPorId` tinha sumido do `UsuarioService`; o arquivo `RegistroLogin.Java` estava com extensão em maiúsculo | Imports e método restaurados, arquivo renomeado para `RegistroLogin.java` (commit de correção na `feat-ci`) |
| Compilação quebrada com 177 erros de JUnit | Testes estavam em `src/test/` e o `javac` da aplicação os incluiu | Testes movidos para `test/` na raiz |
| `.gitignore` com duas linhas coladas (`*.loglib/`) | Falta de quebra de linha | Corrigido, `lib/` voltou a ser ignorada |
| `apt install maven` falhou | Erro do `dpkg` (estrutura precisando de limpeza) | Maven baixado em tarball para `.tools/` |
| `PixService.validarChave` não reconhece o tipo `ALEATORIA` | Tipo não previsto na validação original | A API contorna ao gerar a chave; vale corrigir no service |
| Senha padrão `123456` ao cadastrar sem senha | Decisão do service de usuário | Documentado e coberto por teste; revisar antes de login real |

## Limitações e próximos passos

**Limitações atuais**

- Os testes cobrem os services, não as rotas HTTP.
- Não há medição de cobertura, então não dá para dizer um percentual.
- `Notificacao` e `ContatoService` não têm testes.
- O workflow ainda não foi observado rodando no GitHub.
- Não há proteção de branch: hoje é possível fazer merge mesmo com o CI vermelho.

**Próximos passos**

1. Fazer commit e push do `ci.yml` e conferir a aba Actions; se ficar vermelho, corrigir.
2. Abrir o Pull Request da `feat-ci` para a `dev`, citando a correção do build, os testes, o `projeto.sh` no lugar do `executar.sh` e o workflow.
3. Colar e rodar o `ApiIntegracaoTest`; passando, remover os quatro `testar-*.sh` com `git rm`.
4. Pedir ao Nick (dono do repositório) para ativar proteção de branch na `dev` e na `main`, exigindo o CI verde.
5. Corrigir o tipo `ALEATORIA` no `PixService` e escrever testes para `Notificacao` e `ContatoService`.
6. Na segunda etapa, testar login e rotas de administrador quando existirem na API.
7. Atualizar o `README.md` com os comandos `./projeto.sh rodar` e `./projeto.sh testar`.

## Como adicionar um teste novo

1. Abra a classe de teste da área que você mexeu, por exemplo `test/service/TransacaoServiceTest.java`, ou crie uma nova em `test/service/` com o nome terminando em `Test`.
2. Escreva um método com `@Test` e um nome que diga o que deve acontecer, como `limiteDe2000PorPixEhRespeitado`.
3. Monte os dados do zero dentro do teste (usuário, conta, saldo), para não depender de outro teste.
4. Execute a ação e confira o resultado com `assertEquals`, `assertTrue`, `assertFalse` ou `assertThrows`.
5. Rode `./projeto.sh testar` e veja o total subir.
6. Faça commit e push; o GitHub Actions roda tudo de novo no Pull Request.

Modelo:

```
@Test
void saldoInsuficienteEhRejeitadoESemEfeito() {
    // monta: duas contas, a de origem com pouco saldo
    // age: tenta transferir mais do que tem
    // confere: a transferência falha e os saldos continuam iguais
}
```

Dica: um bom teste quebra quando a regra é alterada. Mude o valor do limite no código, rode os testes e veja se algum falha; se nenhum falhar, falta um teste para aquela regra.