# Websoso OCI 개발 앱 배포

현재 `~/websoso/compose.yaml`의 MySQL·Redis 및 볼륨을 그대로 사용한다. 이 디렉터리의 `compose.app.yaml`만 CI/CD가 갱신하며, 공용 `/srv/edge`나 다른 프로젝트는 건드리지 않는다.

## 배포 전 한 번 준비

1. GitHub Actions secrets에 `OCI_DEV_HOST`(OCI 공인 IP), `OCI_DEV_USER`(`websoso`), `OCI_DEV_SSH_KEY`(해당 계정의 개인 키)를 등록한다. 기존 `DEVELOP_SERVER_*`는 이전 개발 서버 설정이므로 사용하지 않는다.
2. 기존 `ACTION_TOKEN`이 비공개 서브모듈을 읽을 수 있어야 한다. 워크플로는 서브모듈 `main`의 최신 커밋을 가져오고, 빌드에서 사용한 **같은 커밋**의 `application-common.yml`, `application-dev.yml`, FCM JSON, 해당 개발 Apple 키만 SSH로 `~/websoso/config-repo/`에 복사한다. 운영 설정은 전송하지 않는다. 서버에서 서브모듈을 수동으로 갱신할 필요는 없다. 이 폴더는 Docker 이미지에 넣지 않고 컨테이너에 읽기 전용으로 마운트한다. OCI 전용 빌드 컨텍스트에도 JAR 외의 파일은 포함하지 않는다. 파일을 GitHub Actions 로그나 공개 저장소로 복사하지 않는다.
3. `application-dev.yml`에서 MySQL·Redis의 주소와 데이터베이스 이름이 OCI 구성과 일치하는지 확인한다. 앱은 같은 Compose 네트워크에서 `websoso-mysql-container:3306`, `websoso-redis-container:6379`로 접속한다. 호스트에서 터널링할 때만 `127.0.0.1:3307`, `127.0.0.1:6380`을 사용한다. 앱의 DB·Redis 비밀번호는 서버의 기존 `.env`에 있는 `MYSQL_PASSWORD`, `REDIS_PASSWORD`를 사용한다. Apple·S3 값은 기존 GitHub Actions secrets에서 배포 시 전달한다. 데이터 행은 나중에 옮겨도 되지만, 앱이 사용하는 테이블·제약조건 등 스키마는 먼저 준비한다. 현재 저장소의 개발 설정에서 자동 스키마 생성 설정은 확인되지 않았다.
4. `edge` 네트워크와 공용 nginx가 이미 떠 있어야 한다. 웹소소 도메인 설정과 인증서를 먼저 적용한다. `https://dev.websoso.kr/actuator/health`의 정상 응답은 앱 배포 후 확인한다.
5. 기존 DB 볼륨을 초기화하거나 `docker compose down -v`를 실행하지 않는다. 데이터 이전은 앱 기동 이후에 진행해도 된다.

## 워크플로

- `dev` 푸시: JAR을 빌드한 후 `linux/arm64` OCI 개발 이미지 `websoso/websoso-image:dev-<commit SHA>`를 Docker Hub에 올린다. 기존 운영용 `latest` 태그를 덮어쓰지 않는다.
- 빌드 성공 후 OCI 필수 파일과 DB·Redis 컨테이너를 확인하고, 빌드에서 사용한 서브모듈 커밋의 개발 설정을 서버에 전송한다. 그런 다음 **api 서비스만** pull/up·재생성한다. 설정만 바뀌어도 앱을 재시작한다. MySQL·Redis 컨테이너와 볼륨은 재생성하지 않는다. 마지막에 공개 헬스 엔드포인트를 확인한다. 기존 개발 서버의 `~/deploy.sh`는 더 이상 호출하지 않는다.
- GitHub Actions의 `CD-develop`을 `dev` 브랜치에서 수동 실행해 동일한 절차를 다시 수행할 수도 있다.
- 애플리케이션은 `edge` 네트워크에만 추가로 연결되고 호스트 포트를 열지 않는다. 공용 nginx 사이트 설정은 이 워크플로의 범위 밖이다.

## 서버에서 확인

```bash
cd ~/websoso
docker compose -p websoso-dev -f compose.yaml -f compose.app.yaml ps
docker logs --tail 100 websoso-api-container
curl --fail --show-error https://dev.websoso.kr/actuator/health
```

수동 배포가 실패하면 먼저 앱 로그와 `docker compose ... config --quiet` 결과를 확인한다. DB·Redis 볼륨, 공용 nginx, 다른 프로젝트 컨테이너는 조작하지 않는다. 배포 전 앱 이미지 태그를 기록해 두면 이전 태그를 `WEBSOSO_APP_IMAGE`로 지정해 앱만 다시 올릴 수 있다.
