FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
ARG JAVA_OPTS=""
ENV JAVA_OPTS=${JAVA_OPTS}
VOLUME /tmp
COPY target/deal-desk-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 4000
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
# For Spring-Boot project, use the entrypoint below to reduce Tomcat startup time.
#ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar dealdeskservice.jar
