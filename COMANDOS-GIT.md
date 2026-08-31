# Guia de Comandos Git — Projeto PIX

Guia rápido organizado por cenário. Use como referência no dia a dia do desenvolvimento.

## Antes de começar a trabalhar

Sempre confira em qual branch você está e atualize antes de criar algo novo.

```bash
git branch                  # mostra a branch atual (marcada com *)
git switch dev               # vai para a branch de desenvolvimento
git pull                     # traz as últimas mudanças do remoto
```

## Criar uma branch de funcionalidade

Toda funcionalidade nova sai da `dev`, nunca da `main`.

```bash
git switch dev
git pull
git switch -c feat/nome-da-funcionalidade
```

Exemplo:
```bash
git switch -c feat/criar-conta-bancaria
```

## Subir um trabalho (add, commit, push)

Depois de codar, siga essa sequência:

```bash
git status                              # vê o que mudou
git add .                               # coloca tudo em stage (ou git add arquivo.java pra escolher)
git commit -m "feat: implementa criação de conta bancária"
git push -u origin feat/nome-da-funcionalidade
```

O `-u` só é necessário na primeira vez que você envia essa branch. Depois disso, pode usar só `git push`.

Depois do push, abra o Pull Request no GitHub apontando para a `dev`.

## Renomear uma branch

**Local:**
```bash
git branch -m nome-antigo nome-novo
```
(se você já estiver na branch, pode omitir o nome antigo: `git branch -m nome-novo`)

**Refletir no remoto:**
```bash
git push origin nome-novo
git push origin --delete nome-antigo
```

## Atualizar sua branch com o que já foi mergeado na dev

Evita conflitos grandes no PR. Faça isso regularmente enquanto trabalha:

```bash
git switch dev
git pull
git switch feat/sua-branch
git merge dev
```

## Ver o que mudou antes de commitar

```bash
git diff                # mudanças ainda não adicionadas ao stage
git diff --staged       # mudanças já em stage, prestes a virar commit
```

## Desfazer coisas

| Situação | Comando |
|---|---|
| Tirar arquivo do stage (sem perder a mudança) | `git restore --staged arquivo.java` |
| Descartar mudança local (volta ao último commit) | `git restore arquivo.java` |
| Corrigir mensagem ou incluir arquivo esquecido no último commit local | `git commit --amend -m "mensagem correta"` |

**Atenção:** `git restore` (sem `--staged`) é destrutivo — apaga a mudança de verdade. `--amend` só deve ser usado em commits que ainda não foram enviados (push).

## Ver o histórico

```bash
git log --oneline           # histórico resumido
git log -p                  # histórico com o conteúdo de cada mudança (q pra sair)
git log --oneline --graph --all   # visão de todas as branches
```

## Trocar de branch

```bash
git switch nome-da-branch
```

Se a branch existe só no remoto (alguém do time criou e você ainda não tem local):
```bash
git switch nome-da-branch
```
(o Git detecta e cria a versão local automaticamente rastreando o remoto)

## Convenção de mensagens de commit

Sigam esse padrão para manter o histórico organizado:

```
feat: nova funcionalidade
fix: correção de bug
docs: mudança em documentação (README, comentários)
refactor: reorganização de código sem mudar comportamento
test: adição ou ajuste de testes
```

Exemplo completo:
```bash
git commit -m "fix: corrige validação de CPF na criação de usuário"
```

## Checklist rápido antes de abrir um PR

- [ ] `git pull` da `dev` mais recente foi feito na sua branch
- [ ] `git status` está limpo (nada esquecido fora do commit)
- [ ] Código testado localmente
- [ ] Mensagens de commit seguem o padrão `feat:` / `fix:` / etc
- [ ] Push feito: `git push`