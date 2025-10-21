FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app/autenticar
# después de establecer WORKDIR /app/autenticar
RUN mkdir -p /app/autenticar/data

COPY autenticar ./ 
RUN mvn -B -DskipTests=true clean package

FROM eclipse-temurin:21-jre
WORKDIR /app/autenticar
RUN mkdir -p /app/autenticar/data
COPY --from=build /app/autenticar/target/*.jar app.jar
# EXPOSE 4002
CMD ["sh","-c","mkdir -p /app/autenticar/data && java -Dserver.port=${PORT:-4002} -jar /app/autenticar/app.jar"]

