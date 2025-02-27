# Build stage
FROM eclipse-temurin:21-jdk-jammy as builder
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Install Maven and build
RUN apt-get update && \
    apt-get install -y maven && \
    mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Install monitoring agent and security updates
RUN apt-get update && \
    apt-get install -y --no-install-recommends libjemalloc2 curl && \
    rm -rf /var/lib/apt/lists/* && \
    addgroup --gid 1001 appuser && \
    adduser --uid 1001 --gid 1001 --disabled-password appuser && \
    mkdir -p /app/batch && \
    chown -R appuser:appuser /app/batch

# Copy application jar
COPY --from=builder /app/target/deal-desk-service.jar ./app.jar

# Set memory and GC optimization flags for batch processing
ENV JAVA_OPTS="-XX:+UseParallelGC -XX:MaxRAMPercentage=50.0 -XX:ParallelGCThreads=4 -Dspring.batch.job.enabled=false -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp"
ENV LD_PRELOAD=/usr/lib/x86_64-linux-gnu/libjemalloc.so.2

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]