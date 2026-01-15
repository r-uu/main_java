# JasperReports Docker Container
# Isolierte Umgebung für Report-Generierung
FROM eclipse-temurin:17-jdk-alpine

# Metadata
LABEL maintainer="r-uu"
LABEL description="Isolated JasperReports environment"

# Arbeitsverzeichnis
WORKDIR /app

# Maven installieren für Dependency Management
RUN apk add --no-cache maven

# JasperReports Dependencies (werden beim Build geholt)
COPY pom.xml /app/
RUN mvn dependency:go-offline

# Fonts für PDF-Generierung
RUN apk add --no-cache \
    fontconfig \
    ttf-dejavu \
    msttcorefonts-installer \
    && update-ms-fonts

# Application Code
COPY src /app/src

# Build
RUN mvn package -DskipTests

# Expose Port für REST API (optional)
EXPOSE 8090

# Entry Point - kann als CLI oder Server laufen
ENTRYPOINT ["java", "-jar", "target/jasperreports-service.jar"]

