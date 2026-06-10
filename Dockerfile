FROM amd64/amazoncorretto:17

WORKDIR /app

COPY config-repo/ /app/config-repo/
RUN test -f /app/config-repo/application-dev.yml \
   && test -f /app/config-repo/application-prod.yml \
   && test -f /app/config-repo/application-common.yml

COPY ./build/libs/WSS-Server-0.0.1-SNAPSHOT.jar /app/websoso.jar

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "websoso.jar"]