# 🎵 음표상점 (MusicNote-Store) - Backend API Server

> **"음반과 악보를 사고파는 이커머스에 AI 감정/날씨 기반 음악 추천을 더하다"**

**MusicNote-Store**는 음반(피지컬)과 악보(디지털/피지컬)를 거래할 수 있는 특화 전자상거래 플랫폼입니다. 견고한 Spring Security 기반의 권한 관리와 JPA 표준을 준수한 백엔드 API를 제공하며, 차별화된 **AI 챗봇 추천 서비스**를 통해 사용자에게 맞춤형 쇼핑 경험을 선사합니다.

---

## 🏗️ 시스템 아키텍처 및 기술 스택

| 구분 | 기술 스택 | 배지 / 비고 |
|---|---|---|
| **Frontend** | Vue 3 (Composition API), TypeScript, Pinia, Vite | ![Vue.js](https://img.shields.io/badge/Vue.js-4FC08D?style=flat&logo=vuedotjs&logoColor=white) ![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=flat&logo=typescript&logoColor=white) ![Pinia](https://img.shields.io/badge/Pinia-FFE56F?style=flat&logo=pinia&logoColor=black) |
| **Backend** | Java 17, Spring Boot 3.x, Spring Security 6, Spring Data JPA (Hibernate) | ![Java](https://img.shields.io/badge/Java-17-007396?style=flat&logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/SpringBoot-6DB33F?style=flat&logo=springboot&logoColor=white) ![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=flat&logo=springsecurity&logoColor=white) |
| **Database** | **[Local]** Oracle 21c XE (Docker, Port 11521)<br>**[Prod]** AWS RDS (Oracle) | ![Oracle](https://img.shields.io/badge/Oracle-F80000?style=flat&logo=oracle&logoColor=white) ![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white) |
| **Infra & Cloud**| Docker Compose, AWS EC2, AWS S3 (디지털 악보 저장/서명 URL) | ![AWS](https://img.shields.io/badge/AWS-232F3E?style=flat&logo=amazonaws&logoColor=white) |
| **Testing & CI/CD**| JUnit5, Mockito, GitHub Actions | ![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=flat&logo=junit5&logoColor=white) ![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF?style=flat&logo=githubactions&logoColor=white) |
| **Collaboration**| Git, GitHub, Notion (일정 및 기술 문서 동기화) | ![Notion](https://img.shields.io/badge/Notion-000000?style=flat&logo=notion&logoColor=white) |

---

## 📌 주요 비즈니스 기능 (MVP & Extended)

### 🔐 1. 회원 관리 & 보안 (Security)
* **JWT 기반 인증/인가:** Access/Refresh Token을 활용한 Stateless 인증 체계
* **역할 기반 권한 제어 (RBAC):** `USER`(일반 구매자), `SELLER`(판매자/크리에이터), `ADMIN`(관리자) 권한 분리
* **보안 강화:** 비밀번호 암호화(BCrypt), AI 기반 비정상 로그인 감지 및 알림, Rate Limit 적용

### 💿 2. 상품 & 디지털 라이선스 관리
* **피지컬 & 디지털 통합 판매:** 음반/악보 배송 상품 및 디지털 음원/악보 파일 다운로드 관리
* **AWS S3 보안 링크:** 디지털 악보 결제 완료 시, 짧은 유효기간과 횟수 제한이 적용된 **S3 서명 URL(Signed URL)** 발급
* **관리자 & 판매자 기능:** 상품 등록/수정/승인, 카테고리/태그 관리, 환불 시 라이선스 자동 무효화

### 🛒 3. 주문/결제 & 정산 시스템
* **주문 라이프사이클:** 장바구니 → 주문 생성 → 결제 연동 → 배송 상태 관리(결제완료/배송중/완료)
* **PG사 결제 연동:** 토스페이/카카오페이 등 실제 PG사 웹훅(Webhook) 실시간 처리 및 거래 로그 기록 (테스트 시 Mock API 활용)
* **크리에이터 정산:** 판매 수수료 차감 후 월별 정산 리포트 생성 및 환불 반영

### 🤖 4. AI 음악 추천 챗봇 (LangChain 특화 기능)
* **상황 기반 맞춤 추천:** 사용자의 텍스트 입력에서 감정을 분석하고, 실시간 날씨 API를 연동하여 최적의 음반/악보 추천
* **대화형 쇼핑 경험:** 단순 검색을 넘어 챗봇과의 대화를 통해 상품 상세 페이지 및 장바구니로 바로 연결

---

## 📂 프로젝트 폴더 구조

```text
📦 MusicNote-Store-Backend
 ┣ 📂 docs/                  # 인프라, 아키텍처, 트러블슈팅 등 상세 기술 문서
 ┣ 📂 src/main/java/com/store/store
 ┃ ┣ 📂 component/           # DTO <-> Entity Mapper 및 도메인 컴포넌트
 ┃ ┣ 📂 config/              # Security, JPA, CORS, PasswordEncoder 등 전역 설정
 ┃ ┣ 📂 controller/          # REST API 엔드포인트
 ┃ ┣ 📂 domain/              # JPA Entity 및 Enum 객체
 ┃ ┣ 📂 dto/                 # Request / Response DTO
 ┃ ┣ 📂 exception/           # Custom Exception 및 Global Exception Handler
 ┃ ┣ 📂 repository/          # Spring Data JPA Repository
 ┃ ┗ 📂 service/             # 비즈니스 로직 계층
 ┣ 📜 docker-compose.yml     # [Local DB] Oracle 21c XE 컨테이너 실행 설정
 ┣ 📜 .env.example           # 환경 변수 템플릿 (DB 비밀번호, AWS Key 등)
 ┗ 📜 README.md              # 프로젝트 메인 문서