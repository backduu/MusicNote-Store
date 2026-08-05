# 📚 `docs/` 폴더 관리 및 기술 문서화 가이드라인

## 1. 📂 추천 폴더 체계 (Tree Structure)

`docs/` 내부를 역할별로 **4개의 핵심 카테고리**로 분류하여 관리하고 있다.

```text
📦 docs/
📦 docs/frontend
 ┃ 
 ┣ 📂 01-infra/               # 인프라, 환경 구축, 배포 관련
 ┃
 ┗ 📂 02-troubleshooting/
 
📦 docs/backend
 ┃ 
 ┣ 📂 01-infra/               # 인프라, 환경 구축, 배포 관련
 ┃ ┣ 📜 DOCKER_ORACLE_SETUP.md  # 도커 DB 가이드
 ┃ ┣ 📜 AWS_EC2_S3_DEPLOY.md    # AWS 배포 및 S3 서명 URL 설정법 (안쓸수도 있고, 홈서버 구축이 될 수도 있음)
 ┃ ┗ 📜 CI_CD_PIPELINE.md       # GitHub Actions 자동 배포 흐름도
 ┃
 ┣ 📂 02-CS/        # 백엔드 자료
 ┃ ┗ 📜 Builder and JPA Dirty checking.md 
 ┃
 ┣ 📂 03-architecture/        # 설계, ERD, 컨벤션
 ┃ ┣ 📜 ERD_AND_DOMAIN.md       # 데이터베이스 ERD 구조도 및 엔티티 관계 설명
 ┃ ┣ 📜 CODING_CONVENTION.md    # Java/Spring 깃 커밋 메세지, 코드 컨벤션 약속
 ┃ ┗ 📜 SECURITY_FLOW.md        # JWT 인증/인가 및 Security 필터 동작 시퀀스
 ┃
 ┣ 📂 04-api/                 # API 명세 및 통신 규칙
 ┃ ┣ 📜 ERROR_CODES.md          # 예외 처리(Custom Exception) 및 에러 코드 명세서
 ┃ ┗ 📜 CORS_AND_AUTH_HEADER.md # FE(Vue3) - BE(Spring) 간 CORS 및 토큰 전달 규칙
 ┃
 ┗ 📂 05-troubleshooting/     # 💥 삽질과 극복의 기록 
   ┣ 📜 TS_01_DOCKER_PORT_CONFLICT.md  # 1521 포트 충돌 및 APP_USER_PASSWORD 에러 해결
```

## 2. 🚀 퀵 스타터

본 프로젝트는 로컬 PC 환경에 DB를 직접 설치하지 않고 Docker 컨테이너를 사용하여 독립된 데이터베이스 환경을 제공한다.

1️⃣ 환경 변수 설정
루트 디렉토리의 .env.example을 복사하여 .env 파일을 생성하고 본인의 DB 비밀번호를 입력한다.

```Bash
cp .env.example .env
```

2️⃣ Docker 오라클 DB 실행
Docker Desktop이 실행된 상태에서 아래 명령어를 입력하여 DB 컨테이너(Port 11521)를 구동한다.

```Bash
# DB 컨테이너 백그라운드 실행
docker-compose up -d

# DB 준비 상태 확인 (DATABASE IS READY TO USE! 확인)
docker logs -f oracle-db
```
👉 자세한 DB 연동 및 포트 충돌 해결법은 docs/01-infra/DOCKER_ORACLE_SETUP.md 문서를 참고!!.

3️⃣ Spring Boot 서버 실행
DB 연결 완료 후, IDE(IntelliJ 등)에서 StoreApplication.java를 실행하거나 터미널에서 아래 명령어를 수행.

```Bash
./gradlew bootRun
```