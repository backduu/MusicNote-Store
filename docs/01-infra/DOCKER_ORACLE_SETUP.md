# 🐳 Docker 기반 Oracle DB 로컬 인프라 구축 가이드

* **문서 위치:** `docs/01-infra/DOCKER_ORACLE_SETUP.md`
* **최종 수정일:** 2026-07-14
* **대상 시스템:** MusicNote-Store Backend (Spring Boot 3 + JPA)

---

## 1. 📋 인프라 도입 배경 및 아키텍처

### ① 왜 로컬 설치 대신 도커(Docker)를 사용하는가?
오라클 데이터베이스를 PC에 직접 설치할 경우, 무거운 백그라운드 서비스로 인한 PC 리소스 저하, OS 버전별 충돌, 복잡한 계정/권한 설정 등의 문제가 발생할 수 있다. 
본 프로젝트는 **Docker 컨테이너화**를 통해 다음과 같은 이점을 확보함.

* **1분 컷 온보딩:** 설치 파일 다운로드 없이 명령어 한 줄(`docker-compose up -d`)로 DB 환경 구동
* **완벽한 환경 격리:** 개발자 PC의 OS나 환경에 상관없이 100% 동일한 DB 스펙 유지
* **포트 충돌 완벽 해결:** 로컬에 다른 DB가 켜져 있어도, 포트 매핑(`11521:1521`)을 통해 충돌 없이 동시 작동

### ② 네트워크 포트 매핑 구조
```text
[ 내 PC (Host - localhost) ]                  [ Docker 컨테이너 내부 ]
  Spring Boot App (Port: 8080)   ---(JDBC)--->  Port: 11521 (매핑 통로)
  DBeaver DB Client              ---(TCP)---->  Port: 11521    |
                                                               v
                                                Oracle 21c XE (Port: 1521)
```

## 2. 🛠️ 사전 준비 (Prerequisites)
Docker Desktop 설치 및 실행:

윈도우/맥용 Docker Desktop이 설치되어 있어야 하며, 프로그램 실행 후 좌측 하단 상태바가 "Engine running (초록색 불)"인지 반드시 확인해야 합니다.

Git Clone: 프로젝트 소스코드를 로컬에 다운로드합니다.

## 3. 📂 파일 구성 및 환경 변수 설정
프로젝트 루트 디렉토리에 위치한 인프라 관련 파일 구성은 다음과 같습니다.

```Plaintext
📦 MusicNote-Store-Backend
 ┣ 📜 docker-compose.yml   # 도커 서비스 구동 명세서
 ┣ 📜 .env                 # 비밀번호 및 민감 정보 (Git 추적 제외!)
 ┣ 📜 .env.example         # 팀원 공유용 템플릿
 ┗ 📜 .gitignore           # .env 제외 설정 완료
```

### ① .env 파일 생성 (보안 설정)
루트 디렉토리의 .env.example을 복사하여 .env 파일을 생성하고, 사용할 데이터베이스 비밀번호를 설정합니다. (특수문자 포함 시 따옴표 ' ' 사용 권장)

```Bash
# 터미널 명령어
cp .env.example .env
Ini, TOML
# .env 파일 내용 예시
ORACLE_PWD='비밀번호'
```
### ② docker-compose.yml 최종 명세
본 프로젝트는 가볍고 계정 생성까지 자동화된 커뮤니티 유지보수 이미지인 gvenzl/oracle-xe:21을 사용합니다.

```YAML
services:
  oracle-db:
    image: gvenzl/oracle-xe:21
    container_name: oracle-db
    ports:
      - "11521:1521"  # [내 PC 포트] : [컨테이너 내부 포트]
    environment:
      - ORACLE_PASSWORD=${ORACLE_PWD}         # SYSTEM 관리자 비밀번호
      - APP_USER=${ORACLE_ID}                  # 사용할 서비스 일반 계정 
      - APP_USER_PASSWORD=${ORACLE_PWD}       # 서비스 계정 비밀번호
    volumes:
      - oracle-data:/opt/oracle/oradata       # 컨테이너를 삭제해도 데이터가 유지되도록 마운트

volumes:
  oracle-data:
```

## 4. 🚀 컨테이너 실행 및 준비 상태 검증
터미널(PowerShell 또는 Bash)을 열고 아래 순서대로 명령어를 실행합니다.

Step 1. 백그라운드에서 컨테이너 실행
```Bash
docker-compose up -d
```

Step 2. 초기화 완료 로그 실시간 모니터링 (⚠️ 가장 중요!)
오라클은 컨테이너가 뜬 후 내부적으로 테이블스페이스를 만들고 APP_USER 계정을 생성하는 데 약 1~2분의 준비 시간이 소요됩니다.

```Bash
docker logs -f oracle-db
```
로그가 화면에 계속 올라가다가, 마지막에 DATABASE IS READY TO USE! 문구가 뜨면 완벽하게 초기화가 끝난 것입니다.
(로그 창에서 탈출하려면 키보드 Ctrl + C 입력)

## 5. 🔌 DB 클라이언트 & 백엔드 연동 정보
### ① DBeaver (DB GUI 클라이언트) 접속 설정
Host: localhost

Port: 11521 

Database (Service Name): gvenzl 21c XE 버전의 기본 PDB 이름

Username: .env에 설정한 아이디

Password: .env에 설정한 비밀번호 

### ② Spring Boot application.yml 설정
src/main/resources/application.yml 파일의 데이터소스 설정을 아래와 같이 맞춥니다.

```YAML
spring:
  application:
    name: store
  datasource:
    driver-class-name: oracle.jdbc.OracleDriver
    url: # oracle url 기입
    username:  # .env에 적었던 아이디와 동일하게 기입
    password:  # .env에 적었던 비밀번호와 동일하게 기입
  jpa:
    hibernate:
      ddl-auto: update      # 엔티티 변경 시 테이블 구조 자동 업데이트
      show-sql: true        # 콘솔에 JPA 쿼리 출력
      format_sql: true      # 쿼리를 가독성 좋게 정렬하여 출력
```