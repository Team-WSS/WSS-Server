FROM amd64/amazoncorretto:17

WORKDIR /app

COPY config-repo/ /app/config-repo/

COPY ./build/libs/WSS-Server-0.0.1-SNAPSHOT.jar /app/websoso.jar

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "websoso.jar"]
