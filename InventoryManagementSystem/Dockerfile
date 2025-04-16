# Usa una imagen base de Java
FROM eclipse-temurin:17-jdk

# Define el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo JAR compilado al contenedor
COPY target/*.jar app.jar

# Expón el puerto (Render lo reemplaza con $PORT)
EXPOSE 8080

# Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "app.jar"]
