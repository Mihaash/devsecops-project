# Stage 1: extract layers
FROM eclipse-temurin:17-jre-alpine as builder
WORKDIR /opt/app
COPY target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# Stage 2: build image
FROM eclipse-temurin:17-jre-alpine
RUN apk update && apk upgrade
WORKDIR /opt/app
COPY --from=builder /opt/app/dependencies/ ./
COPY --from=builder /opt/app/spring-boot-loader/ ./
COPY --from=builder /opt/app/snapshot-dependencies/ ./
COPY --from=builder /opt/app/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]