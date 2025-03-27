# Primera etapa: construcción (builder)
FROM eclipse-temurin:21-jdk-jammy AS builder

# Instala Maven manualmente
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo pom.xml
COPY pom.xml .

# Descarga las dependencias de Maven
RUN mvn dependency:go-offline

# Copia el código fuente
COPY src ./src

# Empaqueta la aplicación en un archivo JAR (omite los tests)
RUN mvn clean package -DskipTests

# Segunda etapa: ejecución (imagen final)
FROM eclipse-temurin:21-jre-jammy

# Establece el directorio de trabajo
WORKDIR /app

# Copia el archivo JAR generado desde la etapa anterior
COPY --from=builder /app/target/*.jar app.jar

# Expone el puerto en el que corre la aplicación (por defecto, 8080 para Spring Boot)
EXPOSE 8081

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
