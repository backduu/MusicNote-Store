아하, 이전에 전체 파일 내용으로 묶어 드리는 과정에서 일부 코드 블록과 텍스트 레이아웃의 마크다운 서식이 깔끔하게 정리되지 않았었군요! 죄송합니다.

가독성을 극대화하고 복사해서 바로 파일로 만들었을 때 완벽하게 렌더링되도록, **전체 내용을 100% 정통 마크다운 규격으로 철저하게 가다듬어 다시 전해드립니다.**

---

# 💥 [Troubleshooting] Docker 기반 Oracle DB 구축 에러 및 해결 일지

* **문서 위치:** `docs/04-troubleshooting/TS_01_DOCKER_ORACLE_ERRORS.md`
* **최종 수정일:** 2026-07-14
* **관련 인프라:** Docker Compose, Oracle Database 21c XE

오라클 데이터베이스 컨테이너 인프라를 로컬 환경에 구축하는 과정에서 발생한 주요 에러 현상과 원인을 분석하고, 이를 해결하며 얻은 기술적 자산을 기록합니다.

---

## 📋 요약 (Summary)

1. [Case 1: 오라클 엔터프라이즈 이미지 접근 권한 거부 (pull access denied)](https://www.google.com/search?q=%23-case-1-%EC%98%A4%EB%9D%BC%ED%81%B4-%EC%97%94%ED%84%B0%ED%94%84%EB%9D%BC%EC%9D%B4%EC%A6%88-%EC%9D%B4%EB%AF%B8%EC%A7%80-%EC%A0%91%EA%B7%BC-%EA%B6%8C%ED%95%9C-%EA%B1%B0%EB%B6%80-pull-access-denied)
2. [Case 2: 존재하지 않는 이미지 태그 참조 오류 (not found)](https://www.google.com/search?q=%23-case-2-%EC%A1%B4%EC%9E%AC%ED%95%98%EC%A7%80-%EC%95%8A%EB%8A%94-%EC%9D%B4%EB%AF%B8%EC%A7%80-%ED%83%9C%EA%B7%B8-%EC%B0%B8%EC%A1%B0-%EC%98%A4%EB%A5%98-not-found)
3. [Case 3: 호스트 포트 바인딩 충돌 (bind: Only one usage of each socket address...)](https://www.google.com/search?q=%23-case-3-%ED%98%B8%EC%8A%A4%ED%8A%B8-%ED%8F%AC%ED%8A%B8-%EB%B0%94%EC%9D%B8%EB%94%A9-%EC%B6%A9%EB%8F%8C-bind-only-one-usage-of-each-socket-address)
4. [Case 4: 컨테이너 내부 환경 변수 누락 및 컨테이너 재빌드 오류](https://www.google.com/search?q=%23-case-4-%EC%BB%A8%ED%85%8C%EC%9D%B4%EB%84%88-%EB%82%B4%EB%B6%80-%ED%99%98%EA%B2%BD-%EB%B3%80%EC%88%98-%EB%88%84%EB%9D%BD-%EB%B0%8F-%EC%BB%A8%ED%85%8C%EC%9D%B4%EB%84%88-%EC%9E%AC%EB%B9%8C%EB%93%9C-%EC%98%A4%EB%A5%98)

---

## 🚨 Case 1: 오라클 엔터프라이즈 이미지 접근 권한 거부 (`pull access denied`)

### 1️⃣ 문제 현상 (Issue)

`docker login`을 성공적으로 마친 후 `docker-compose up -d`를 실행했으나, 이미지를 다운로드하지 못하고 권한 거부 에러가 발생함.

```text
✘ Image oracle/database:19.3.0-ee Error pull access denied for oracle/database, repository does not exist or may require 'docker login'
Error response from daemon: pull access denied for oracle/database, repository does not exist or may require 'docker login'

```

### 2️⃣ 원인 분석 (Root Cause)

* **상용 이미지 참조:** `oracle/database:19.3.0-ee`는 일반 Docker Hub가 아닌 오라클 자체 컨테이너 레지스트리(OCR)의 별도 라이선스 동의 및 계정 연동이 필요한 엔터프라이즈 전용 상용 이미지임.
* **편집기 미저장:** 로컬 개발 환경에 맞춰 무료 이미지로 코드를 수정했으나, 텍스트 에디터의 수정 내용이 **디스크에 명시적으로 저장(`Ctrl + S`)되지 않은 채** 명령어가 실행되어 이전 상용 이미지 호출이 반복됨.

### 3️⃣ 해결 방안 (Solution)

* `docker-compose.yml` 파일의 이미지를 무료 및 경량 버전인 `gvenzl` 이미지로 수정 후 **확실하게 저장(`Ctrl + S`)** 처리하여 명령어를 재실행함.

### 4️⃣ 배운 점 (Lesson Learned)

* 도커 이미지 명칭 뒤에 붙는 태그(`-ee`, `-se` 등)를 통해 상용 여부를 명확히 판단해야 함.
* CLI 명령어를 수행하기 전 항상 에디터의 파일 저장 상태(미저장 동그라미 표식 등)를 교차 검증하는 습관이 필요함.

---

## 🚨 Case 2: 존재하지 않는 이미지 태그 참조 오류 (`not found`)

### 1️⃣ 문제 현상 (Issue)

오라클 무료 이미지를 다운로드하는 과정에서 해당 레퍼런스를 찾을 수 없다는 에러 발생하며 구동에 실패함.

```text
✘ Image gvenzl/oracle-free:21 Error failed to resolve reference "docker.io/gvenzl/oracle-free:21": docker.io/gvenzl/oracle-free:21: not found
Error response from daemon: failed to resolve reference "docker.io/gvenzl/oracle-free:21": docker.io/gvenzl/oracle-free:21: not found

```

### 2️⃣ 원인 분석 (Root Cause)

* **네이밍 및 태그 매칭 불일치:** 오라클 배포 판본에 따라 이미지 저장소 명칭이 다름. 21c 버전은 익스프레스 에디션인 **`oracle-xe`** 브랜치로 관리되고 있으며, 23c 이상 버전부터 `oracle-free`라는 명칭으로 전환됨.
* 존재하지 않는 조합인 `oracle-free:21`을 호출하여 Docker Hub 레지스트리 내부 검색에 실패함.

### 3️⃣ 해결 방안 (Solution)

* 도입하고자 하는 오라클 21c 버전에 맞춰 이미지 저장소 이름을 명확하게 변경하여 해결함.

```yaml
# Before (X)
image: gvenzl/oracle-free:21

# After (O)
image: gvenzl/oracle-xe:21

```

### 4️⃣ 배운 점 (Lesson Learned)

* 오픈소스 및 서드파티 도커 이미지를 사용할 때는 Docker Hub의 공식 문서를 통해 버전별 정확한 `태그(Tag)`와 `저장소명`을 확인하고 명세서를 작성해야 함.

---

## 🚨 Case 3: 호스트 포트 바인딩 충돌 (`bind: Only one usage of each socket address...`)

### 1️⃣ 문제 현상 (Issue)

Docker Desktop에서 컨테이너 구동 시 네트워크 포트 할당에 실패하며 `exit status 1`과 함께 구동이 즉시 중단됨.

```text
Cannot start Docker Compose application. Reason: compose [start] exit status 1. 
Container oracle-db Starting Error response from daemon: ports are not available: exposing port TCP 0.0.0.0:1521 -> 127.0.0.1:0: listen tcp 0.0.0.0:1521: bind: Only one usage of each socket address (protocol/network address/port) is normally permitted.

```

### 2️⃣ 원인 분석 (Root Cause)

* **포트 독점 충돌:** 호스트 PC(윈도우 OS)의 `1521` 포트를 이미 다른 백그라운드 프로세스가 선점하고 있음.
* **원인 식별:** 과거 로컬 PC에 직접 설치했던 윈도우용 오라클 서비스(`OracleServiceXE` 및 리스너)가 윈도우 시작 시 자동으로 구동되어 포트를 독점하고 있었기 때문에, 도커 엔진이 윈도우의 `1521` 통로를 확보하지 못함.

### 3️⃣ 해결 방안 (Solution)

* 기존 로컬 서비스를 매번 찾아가서 종료하는 번거로움을 피하고 독립적인 개발 환경을 보장하기 위해, 도커의 **포트 매핑(Port Forwarding)** 구조를 변경하여 충돌을 영구적으로 우회함.

```yaml
# docker-compose.yml 수정
services:
  oracle-db:
    ports:
      - "11521:1521"  # 외부(내 PC) 접속 포트를 범용적인 1521 대신 '11521'로 우회 설정

```

* **결과:** 기존 로컬 오라클 DB(`localhost:1521`)와 도커 오라클 DB(`localhost:11521`)를 포트 분리를 통해 충돌 없이 동시에 구동할 수 있게 됨.

### 4️⃣ 배운 점 (Lesson Learned)

* 도커의 핵심 가치인 포트 포워딩(`Host Port : Container Port`)을 활용하면 로컬 환경의 오염이나 충돌을 두려워하지 않고 유연한 인프라 아키텍처를 설계할 수 있음.

---

## 🚨 Case 4: 컨테이너 내부 환경 변수 누락 및 컨테이너 재빌드 오류

### 1️⃣ 문제 현상 (Issue)

도커 컨테이너 상태가 초록색(Running)으로 유지되지 못하고 계속 재시작(Restarting)을 반복하며 아래와 같은 내부 로그를 출력함.

```text
CONTAINER: starting up...
$APP_USER has been specified without $APP_USER_PASSWORD[_FILE].
Both variables are required, please specify $APP_USER and $APP_USER_PASSWORD[_FILE].

```

### 2️⃣ 원인 분석 (Root Cause)

* **환경 변수 키값 불일치:** `gvenzl` 오라클 이미지는 일반 애플리케이션 유저를 자동 생성할 때 유저명(`APP_USER`)과 비밀번호(`APP_USER_PASSWORD`)가 한 쌍으로 제공되어야 함. 그러나 설정 명세서에 `APP_PASSWORD`라는 잘못된 키값으로 등록하여 이미지 내부 스크립트가 유저 비밀번호 변수를 인식하지 못함.
* **오염된 컨테이너 상태 지속:** 코드를 수정했더라도 이미 에러 상태로 꼬여버린 컨테이너 찌꺼기가 로컬 도커 가상 네트워크에 남아있어 변경 사항이 정상 반영되지 않음.

### 3️⃣ 해결 방안 (Solution)

1. `docker-compose.yml` 파일의 유저 패스워드 환경 변수명을 공식 스펙에 맞게 `APP_USER_PASSWORD`로 정정함.
2. 오염된 상태를 클린 리셋하기 위해 컨테이너를 완전히 파괴한 후 재빌드 프로세스를 수행함.

```bash
# 1. 꼬인 컨테이너 및 관련 인프라 자원 완전 삭제
docker-compose down

# 2. 클린 상태에서 백그라운드 재구동
docker-compose up -d

```

### 4️⃣ 배운 점 (Lesson Learned)

* 도커 컨테이너 인프라 명세서에서 환경 변수(Environment)의 문자 하나, 언더바 하나는 오작동의 직격타가 되므로 공식 스펙을 철저히 검증해야 함.
* 도커 설정이 변경되었을 때는 단순히 `restart`를 하기보다 `down`을 통해 컨테이너 생명주기를 완전히 끝내고 `up`으로 새로 올리는 것이 클린 아키텍처 유지의 기본임.