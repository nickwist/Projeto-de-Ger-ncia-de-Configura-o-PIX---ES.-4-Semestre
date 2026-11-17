FROM eclipse-temurin:21-jdk
RUN apt-get update && apt-get install -y maven
WORKDIR /app
COPY src ./src
COPY pom.xml .
RUN mvn dependency:copy-dependencies -DoutputDirectory=lib -DincludeScope=runtime
RUN mkdir out && javac -cp "lib/*" -d out $(find src -name "*.java")
CMD ["java", "-cp", "out:lib/*", "api.ApiServer"]