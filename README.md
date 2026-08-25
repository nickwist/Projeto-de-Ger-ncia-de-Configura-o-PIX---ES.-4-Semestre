# GC-PIX--ES.-4-Semestre

## Sobre o Projeto

Este repositório é o projeto prático da disciplina **Gerência de Configuração** (UFC Campus Quixadá), sob orientação do professor Sidartha Carvalho.

O objetivo é simular, em grupo, um ciclo real de desenvolvimento de software, aplicando na prática os conceitos de Gerência de Configuração: controle de versão, ambientes de desenvolvimento (dev, hom, prd), revisão de código via Pull Requests e trabalho colaborativo em equipe.

O sistema desenvolvido visa implementar funcionalidades do **PIX** (sistema de pagamentos instantâneos do Banco Central do Brasil, algo que americanos não possuem), utilizando **Java** e conceitos de **Programação Orientada a Objetos**.

### Objetivos da disciplina aplicados neste projeto

- Simular um projeto real de desenvolvimento de software
- Praticar o uso dos ambientes de dev, hom e prd
- Participar de revisões de código (Pull Requests)
- Utilizar ferramentas de gerenciamento de versão (Git/GitHub)
- Construir um sistema funcional para o domínio de pagamentos PIX


## Estrutura de Branches

Este projeto segue o modelo tradicional de ambientes (dev, hom, prd), com controle de versão via Git.

- **`main`** — Branch de produção. Contém apenas código estável, testado e aprovado. Alterações diretas não são permitidas; toda mudança chega aqui via Pull Request revisado pelo coordenador.

- **`dev`** — Branch de desenvolvimento/integração. Reúne as funcionalidades já finalizadas pelas branches de feature antes de seguirem para homologação. É onde o time integra o trabalho de todos.

- **`hom`** — Branch de homologação. Simula o ambiente de produção para testes de aceitação e validação das funcionalidades antes do deploy final.

- **`feat/<nome-da-funcionalidade>`** — Branches de funcionalidade, criadas a partir da `dev` para cada item do backlog (ex: `feat/cadastro-usuario`). Cada uma é isolada, permitindo trabalho paralelo sem afetar o restante do time. Ao concluir, abre-se um Pull Request para `dev`.

### Fluxo de trabalho

1. Criar uma branch `feat/<nome>` a partir da `dev`
2. Desenvolver e commitar regularmente
3. Abrir Pull Request para `dev`
4. Após revisão do coordenador, merge na `dev`
5. Quando o conjunto de funcionalidades estiver pronto, `dev` é promovida para `hom` (testes)
6. Após validação em `hom`, o código segue para `main` (produção)