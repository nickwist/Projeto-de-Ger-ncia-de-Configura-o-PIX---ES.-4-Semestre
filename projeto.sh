#!/bin/bash
# Script unico do projeto: compila, sobe a API e roda os testes.
#
#   ./projeto.sh rodar    compila e sobe a API em http://localhost:7070
#   ./projeto.sh testar   compila e roda todos os testes (JUnit)
#
# Precisa so de Java 21+ e curl. O Maven e o JUnit sao baixados sozinhos se faltarem.
set -e
cd "$(dirname "$0")"

MAVEN_VERSION=3.9.11
JUNIT_VERSION=1.11.3
JUNIT_JAR="lib-test/junit-platform-console-standalone-$JUNIT_VERSION.jar"

# baixa Javalin, Jackson e slf4j (lista no pom.xml) para a pasta lib/
baixar_dependencias() {
    if [ -d lib ]; then
        return
    fi
    echo ">> baixando as dependencias da API (pom.xml)..."
    MVN="mvn"
    if ! command -v mvn >/dev/null 2>&1; then
        MVN=".tools/apache-maven-$MAVEN_VERSION/bin/mvn"
        if [ ! -x "$MVN" ]; then
            echo ">> Maven nao encontrado, baixando uma copia local em .tools/ ..."
            mkdir -p .tools
            curl -fsSL "https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz" | tar xz -C .tools
        fi
    fi
    "$MVN" -B -q dependency:copy-dependencies -DoutputDirectory=lib -DincludeScope=runtime
}

compilar() {
    baixar_dependencias
    LIBS=$(ls lib/*.jar | paste -sd:)
    echo ">> compilando o codigo..."
    rm -rf out
    mkdir -p out
    javac -cp "$LIBS" -d out $(find src -name "*.java")
}

case "$1" in
    rodar)
        compilar
        echo ">> subindo a API..."
        exec java -cp "out:$LIBS" api.ApiServer
        ;;
    testar)
        compilar
        if [ ! -f "$JUNIT_JAR" ]; then
            echo ">> baixando o JUnit $JUNIT_VERSION..."
            mkdir -p lib-test
            curl -fsSL -o "$JUNIT_JAR" \
                "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/$JUNIT_VERSION/junit-platform-console-standalone-$JUNIT_VERSION.jar"
        fi
        echo ">> compilando os testes..."
        rm -rf out-test
        mkdir -p out-test
        javac -cp "out:$LIBS:$JUNIT_JAR" -d out-test $(find test -name "*.java")
        echo ">> rodando os testes..."
        java -jar "$JUNIT_JAR" execute \
            --class-path "out:out-test:$LIBS" \
            --scan-class-path out-test \
            --reports-dir relatorio-testes \
            --disable-banner
        ;;
    *)
        echo "Uso: ./projeto.sh rodar | testar"
        exit 1
        ;;
esac