FROM amd64/amazoncorretto:17

WORKDIR /app

COPY ./build/libs/WSS-Server-0.0.1-SNAPSHOT.jar /app/websoso.jar

CMD ["java", "-Duser.timezone=Asia/Seoul", "-jar", "-Dspring.profiles.active=prod", "websoso.jar"]
