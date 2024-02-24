# Docker Compose 명령어

## 1. `docker-compose up`
docker-compose.yml 파일이 있는 디렉토리에서 실행되어야 합니다. </br>
서비스를 빌드하고 시작합니다.

## 2. `docker-compose up -d`
docker-compose.yml 파일이 있는 디렉토리에서 실행되어야 합니다. </br>
서비스를 빌드하고 백그라운드에서 시작합니다.

## 3. `docker-compose down`
Compose 프로젝트를 중지하고 컨테이너를 제거합니다.

## 4. `docker-compose build`
서비스의 이미지를 빌드합니다.

## 5. `docker-compose pull`
서비스에 대한 모든 이미지를 가져옵니다.

## 6. `docker-compose push`
서비스의 이미지를 푸시합니다.

## 7. `docker-compose run`
한 번만 실행되는 서비스를 실행합니다.

## 8. `docker-compose logs`
서비스의 로그를 가져옵니다.

## 9. `docker-compose ps`
실행 중인 서비스를 나열합니다.

## 10. `docker-compose restart`
서비스를 재시작합니다.

## 11. `docker-compose stop` / `docker-compose start`
서비스를 중지 / 시작합니다.

## 12. `docker exec -it 컨테이너명 mysql -u root -p`
MySQL 클라이언트를 실행합니다.
