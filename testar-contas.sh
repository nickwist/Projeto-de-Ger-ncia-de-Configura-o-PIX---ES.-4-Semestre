#!/bin/bash
# Smoke test de contas. Rode com a API ligada (./executar.sh em outro terminal).
# Se ja rodou outros testes, reinicie a API para zerar a memoria.
B=http://localhost:7070
j='Content-Type: application/json'
echo "--- cria usuario 1";      curl -s -X POST $B/usuarios -H "$j" -d '{"nome":"Nick","cpf":"11111111111","email":"nick@email.com","telefone":"85999999999"}'; echo
echo "--- criar conta (201)";   curl -s -i -X POST $B/contas -H "$j" -d '{"numeroConta":"006-7","usuarioId":1}' | sed -n '1p;/^{/p;/Location/p'
echo "--- conta duplicada (409)"; curl -s -X POST $B/contas -H "$j" -d '{"numeroConta":"006-7","usuarioId":1}'; echo
echo "--- usuario inexistente (422)"; curl -s -X POST $B/contas -H "$j" -d '{"numeroConta":"007-1","usuarioId":99}'; echo
echo "--- numero invalido (400)"; curl -s -X POST $B/contas -H "$j" -d '{"numeroConta":"a","usuarioId":1}'; echo
echo "--- deposito 500";        curl -s -X POST $B/contas/006-7/depositos -H "$j" -d '{"valor":500}'; echo
echo "--- deposito negativo (400)"; curl -s -X POST $B/contas/006-7/depositos -H "$j" -d '{"valor":-5}'; echo
echo "--- listar";              curl -s $B/contas; echo
echo "--- listar por usuario";  curl -s "$B/contas?usuarioId=1"; echo
echo "--- buscar";              curl -s $B/contas/006-7; echo
echo "--- buscar inexistente (404)"; curl -s $B/contas/999-9; echo
echo "--- renomear conta";      curl -s -X PUT $B/contas/006-7 -H "$j" -d '{"numeroConta":"006-8"}'; echo
echo "--- bloquear";            curl -s -X POST $B/contas/006-8/bloqueio; echo
echo "--- deposito bloqueada (422)"; curl -s -X POST $B/contas/006-8/depositos -H "$j" -d '{"valor":10}'; echo
echo "--- desbloquear";         curl -s -X DELETE $B/contas/006-8/bloqueio; echo
echo "--- excluir com saldo (409)"; curl -s -X DELETE $B/contas/006-8; echo
echo "--- usuario com conta nao pode ser excluido (409)"; curl -s -X DELETE $B/usuarios/1; echo
