FROM eclipse-temurin:17-jre
WORKDIR /app

# JAR 복사
COPY ./build/libs/*.jar pongdang.jar

# JVM 옵션 (메모리 컨테이너 인식)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar pongdang.jar"]
