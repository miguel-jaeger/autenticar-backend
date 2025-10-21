FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app/notas
# después de establecer WORKDIR /app/notas
RUN mkdir -p /app/notas/data

COPY notas ./ 
RUN mvn -B -DskipTests=true clean package

FROM eclipse-temurin:21-jre
WORKDIR /app/notas
RUN mkdir -p /app/notas/data
COPY --from=build /app/notas/target/*.jar app.jar
# EXPOSE 8080
CMD ["sh","-c","mkdir -p /app/notas/data && java -Dserver.port=${PORT:-8080} -jar /app/notas/app.jar"]

