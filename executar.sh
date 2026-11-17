#!/bin/bash
# Compila e sobe a API. Precisa da pasta lib/ (jars do Javalin) na raiz do projeto.
set -e
cd "$(dirname "$0")"
mkdir -p out
javac -cp "lib/*" -d out $(find src -name "*.java")
exec java -cp "out:lib/*" api.ApiServer
