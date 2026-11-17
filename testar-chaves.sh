#!/bin/bash
# Smoke test de chaves PIX. Reinicie a API antes (memoria limpa) e rode em outro terminal.
B=http://localhost:7070
j='Content-Type: application/json'
p() { echo "--- $1"; shift; "$@"; echo; }
p "prepara usuario e conta" curl -s -X POST $B/usuarios -H "$j" -d '{"nome":"Nick","cpf":"11111111111","email":"nick@email.com","telefone":"85999999999"}'
curl -s -o /dev/null -X POST $B/contas -H "$j" -d '{"numeroConta":"006-7","usuarioId":1}'
p "gerar chave aleatoria" curl -s -X POST $B/chaves/gerar
p "validar email ok"      curl -s -X POST $B/chaves/validar -H "$j" -d '{"tipo":"EMAIL","valor":"nick@email.com"}'
p "validar cpf invalido"  curl -s -X POST $B/chaves/validar -H "$j" -d '{"tipo":"CPF","valor":"123"}'
p "associar EMAIL (201)"  curl -s -X POST $B/contas/006-7/chaves -H "$j" -d '{"tipo":"EMAIL","valor":"Nick@Email.com"}'
p "associar CPF do titular (201)" curl -s -X POST $B/contas/006-7/chaves -H "$j" -d '{"tipo":"CPF","valor":"111.111.111-11"}'
p "associar CPF de outra pessoa (422)" curl -s -X POST $B/contas/006-7/chaves -H "$j" -d '{"tipo":"CPF","valor":"22222222222"}'
p "associar TELEFONE (201)" curl -s -X POST $B/contas/006-7/chaves -H "$j" -d '{"tipo":"TELEFONE","valor":"(85) 99999-9999"}'
p "associar ALEATORIA gerada (201)" curl -s -X POST $B/contas/006-7/chaves -H "$j" -d '{"tipo":"ALEATORIA"}'
p "email duplicado (409)" curl -s -X POST $B/contas/006-7/chaves -H "$j" -d '{"tipo":"EMAIL","valor":"nick@email.com"}'
p "email invalido (400)"  curl -s -X POST $B/contas/006-7/chaves -H "$j" -d '{"tipo":"EMAIL","valor":"xxx"}'
p "tipo invalido (400)"   curl -s -X POST $B/contas/006-7/chaves -H "$j" -d '{"tipo":"RG","valor":"1"}'
p "listar chaves da conta" curl -s $B/contas/006-7/chaves
p "consultar chave por email" curl -s $B/chaves/nick@email.com
p "consultar inexistente (404)" curl -s $B/chaves/nao@existe.com
p "alterar email"         curl -s -X PUT $B/chaves/nick@email.com -H "$j" -d '{"novoValor":"nick2@email.com"}'
p "suspender"             curl -s -X POST $B/chaves/nick2@email.com/suspensao
p "suspender de novo (409)" curl -s -X POST $B/chaves/nick2@email.com/suspensao
p "desbloquear"           curl -s -X DELETE $B/chaves/nick2@email.com/suspensao
p "5a chave (201)"        curl -s -X POST $B/contas/006-7/chaves -H "$j" -d '{"tipo":"EMAIL","valor":"extra@email.com"}'
p "6a chave (422 limite)" curl -s -X POST $B/contas/006-7/chaves -H "$j" -d '{"tipo":"EMAIL","valor":"outra@email.com"}'
p "remover chave (204)"   curl -s -o /dev/null -w "%{http_code}" -X DELETE $B/chaves/extra@email.com
p "consultar removida (404)" curl -s $B/chaves/extra@email.com
