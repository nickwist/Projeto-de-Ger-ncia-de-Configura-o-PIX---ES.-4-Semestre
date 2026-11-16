#!/bin/bash
# Smoke test do CRUD de usuarios. Rode com a API ligada (./executar.sh em outro terminal).
B=http://localhost:7070
j='Content-Type: application/json'
echo "--- saude";            curl -s $B/saude; echo
echo "--- criar (201)";       curl -s -i -X POST $B/usuarios -H "$j" -d '{"nome":"Maria","cpf":"123.456.789-00","email":"maria@email.com","telefone":"(85) 98888-8888"}' | sed -n '1p;/^{/p;/Location/p'
echo "--- cpf duplicado (409)"; curl -s -X POST $B/usuarios -H "$j" -d '{"nome":"Outra","cpf":"12345678900","email":"o@email.com","telefone":"85988888888"}'; echo
echo "--- cpf invalido (400)";  curl -s -X POST $B/usuarios -H "$j" -d '{"nome":"X","cpf":"123","email":"x@email.com","telefone":"85988888888"}'; echo
echo "--- listar";            curl -s $B/usuarios; echo
echo "--- buscar 1";          curl -s $B/usuarios/1; echo
echo "--- buscar 99 (404)";   curl -s $B/usuarios/99; echo
echo "--- atualizar 1";       curl -s -X PUT $B/usuarios/1 -H "$j" -d '{"nome":"Maria Silva","email":"maria@email.com","telefone":"85977777777"}'; echo
echo "--- deletar 1 (204)";   curl -s -o /dev/null -w "%{http_code}\n" -X DELETE $B/usuarios/1
echo "--- rota inexistente (404)"; curl -s $B/nada; echo
