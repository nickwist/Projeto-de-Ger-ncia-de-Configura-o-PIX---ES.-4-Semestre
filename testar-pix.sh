#!/bin/bash
# Smoke test de saldo, transferencia, historico e estorno. Reinicie a API antes (memoria limpa).
B=http://localhost:7070
j='Content-Type: application/json'
p() { echo "--- $1"; shift; "$@"; echo; }
curl -s -o /dev/null -X POST $B/usuarios -H "$j" -d '{"nome":"Nick","cpf":"11111111111","email":"nick@email.com","telefone":"85999999999"}'
curl -s -o /dev/null -X POST $B/usuarios -H "$j" -d '{"nome":"Maria","cpf":"22222222222","email":"maria@email.com","telefone":"85988888888"}'
curl -s -o /dev/null -X POST $B/contas -H "$j" -d '{"numeroConta":"006-7","usuarioId":1}'
curl -s -o /dev/null -X POST $B/contas -H "$j" -d '{"numeroConta":"006-9","usuarioId":2}'
curl -s -o /dev/null -X POST $B/contas/006-7/chaves -H "$j" -d '{"tipo":"EMAIL","valor":"nick@email.com"}'
curl -s -o /dev/null -X POST $B/contas/006-9/chaves -H "$j" -d '{"tipo":"EMAIL","valor":"maria@email.com"}'
curl -s -o /dev/null -X POST $B/contas/006-9/chaves -H "$j" -d '{"tipo":"CPF","valor":"22222222222"}'
curl -s -o /dev/null -X POST $B/contas/006-7/depositos -H "$j" -d '{"valor":3000}'
echo "(preparado: Nick 006-7 com R\$3000, Maria 006-9 com chaves email e CPF)"
p "saldo Nick"                  curl -s $B/contas/006-7/saldo
p "transferir 100 por email (201)" curl -s -X POST $B/pix/transferencias -H "$j" -d '{"contaOrigem":"006-7","chaveDestino":"maria@email.com","valor":100}'
p "transferir 50.5 por CPF formatado (201)" curl -s -X POST $B/pix/transferencias -H "$j" -d '{"contaOrigem":"006-7","chaveDestino":"222.222.222-22","valor":50.5}'
p "saldo Maria (150.5)"         curl -s $B/contas/006-9/saldo
p "acima do limite (422)"       curl -s -X POST $B/pix/transferencias -H "$j" -d '{"contaOrigem":"006-7","chaveDestino":"maria@email.com","valor":2500}'
p "valor 2000 ok (201)"         curl -s -X POST $B/pix/transferencias -H "$j" -d '{"contaOrigem":"006-7","chaveDestino":"maria@email.com","valor":2000}'
p "saldo insuficiente (422)"    curl -s -X POST $B/pix/transferencias -H "$j" -d '{"contaOrigem":"006-7","chaveDestino":"maria@email.com","valor":1000}'
p "mesma conta (422)"           curl -s -X POST $B/pix/transferencias -H "$j" -d '{"contaOrigem":"006-7","chaveDestino":"nick@email.com","valor":10}'
p "chave inexistente (422)"     curl -s -X POST $B/pix/transferencias -H "$j" -d '{"contaOrigem":"006-7","chaveDestino":"x@y.com","valor":10}'
p "valor negativo (400)"        curl -s -X POST $B/pix/transferencias -H "$j" -d '{"contaOrigem":"006-7","chaveDestino":"maria@email.com","valor":-5}'
p "3 casas decimais (400)"      curl -s -X POST $B/pix/transferencias -H "$j" -d '{"contaOrigem":"006-7","chaveDestino":"maria@email.com","valor":10.123}'
curl -s -o /dev/null -X POST $B/chaves/maria@email.com/suspensao
p "chave suspensa (422)"        curl -s -X POST $B/pix/transferencias -H "$j" -d '{"contaOrigem":"006-7","chaveDestino":"maria@email.com","valor":10}'
curl -s -o /dev/null -X DELETE $B/chaves/maria@email.com/suspensao
curl -s -o /dev/null -X POST $B/contas/006-7/bloqueio
p "origem bloqueada (422)"      curl -s -X POST $B/pix/transferencias -H "$j" -d '{"contaOrigem":"006-7","chaveDestino":"maria@email.com","valor":10}'
curl -s -o /dev/null -X DELETE $B/contas/006-7/bloqueio
p "listar transacoes"           curl -s $B/transacoes
p "buscar transacao 1"          curl -s $B/transacoes/1
p "transacao 99 (404)"          curl -s $B/transacoes/99
p "por usuario 2"               curl -s "$B/transacoes?usuarioId=2"
p "por data de hoje"            curl -s "$B/transacoes?data=$(date +%F)"
p "data invalida (400)"         curl -s "$B/transacoes?data=ontem"
p "por periodo"                 curl -s "$B/transacoes?inicio=$(date +%F)&fim=$(date +%F)"
p "extrato da Maria"            curl -s $B/contas/006-9/extrato
p "estornar transacao 1"        curl -s -X POST $B/transacoes/1/estorno
p "estornar de novo (422)"      curl -s -X POST $B/transacoes/1/estorno
p "saldo final Nick"            curl -s $B/contas/006-7/saldo
