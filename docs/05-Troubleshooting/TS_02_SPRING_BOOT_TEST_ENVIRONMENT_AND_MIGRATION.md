# Spring Boot 테스트 환경 구축 및 최신화 가이드

본 문서는 MusicNote-Store 프로젝트의 테스트 코드를 작성하는 과정에서 발생한 환경 설정 문제와 이를 해결하기 위한 구조적 개선 사항, 그리고 Spring Boot 3.4 버전 도입에 따른 최신 테스트 표준 라이브러리 전환 과정을 기술한다.

---

## 1. 개요 (Introduction)

소프트웨어 개발 과정에서 테스트 코드는 코드의 안정성을 보장하는 핵심 지표이다. 특히 Spring Boot 환경에서는 계층별 슬라이스 테스트(Slice Test)를 통해 특정 레이어의 동작만을 효율적으로 검증할 수 있다. 본 프로젝트에서는 `ProductController`와 `ProductService`의 단위 테스트를 구현하면서 마주한 설정상의 한계를 극복하고, 최신 프레임워크 트렌드에 발맞춘 리팩토링을 수행하였다.

---

## 2. @WebMvcTest와 JPA Auditing 설정의 분리

### 2.1 문제 현상 (Problem Statement)
컨트롤러 레이어의 순수 로직을 검증하기 위해 `@WebMvcTest`를 사용하던 중 다음과 같은 예외가 발생하며 애플리케이션 컨텍스트(ApplicationContext) 로딩에 실패하였다.

- **에러 로그**: `java.lang.IllegalStateException: Failed to load ApplicationContext`
- **상세 원인**: `JPA metamodel must not be empty!`

### 2.2 원인 분석 (Root Cause Analysis)
기존 구조에서는 메인 애플리케이션 클래스(`StoreApplication`)에 `@EnableJpaAuditing` 어노테이션이 선언되어 있었다. `@WebMvcTest`는 컨트롤러와 관련된 빈들만을 로드하는 슬라이스 테스트인데, 메인 클래스의 설정을 읽어들이는 과정에서 JPA Auditing 기능이 활성화되려고 시도한다. 그러나 `@WebMvcTest` 환경에는 엔티티(Entity) 등 JPA 관련 설정이 로드되지 않기 때문에 JPA 메타모델을 찾지 못해 충돌이 발생하는 것이다.

### 2.3 해결 방법 (Solution)
JPA Auditing 설정을 메인 애플리케이션 클래스에서 제거하고, 별도의 설정 클래스로 분리함으로써 관심사를 분리하였다.

- **JpaAuditingConfig.java** 생성:
```java
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    // JPA Auditing 전용 설정 클래스
}
```

이로써 일반적인 서버 구동 시에는 JPA Auditing이 정상 작동하며, JPA 의존성이 배제된 슬라이스 테스트 시에는 해당 설정이 로드되지 않아 환경적 격리를 실현할 수 있다.

---

## 3. 전역 예외 처리기의 개선: 필수 파라미터 누락 대응

### 3.1 문제 현상
테스트 도중 `@RequestParam(required = true)`가 설정된 엔드포인트에 필수 파라미터를 누락하여 요청을 보낼 경우, 시스템은 400(Bad Request) 응답을 반환해야 함에도 불구하고 500(Internal Server Error)을 반환하는 현상이 발견되었다.

### 3.2 원인 분석
스프링 프레임워크는 필수 파라미터 누락 시 `MissingServletRequestParameterException`을 발생시킨다. 하지만 프로젝트의 전역 예외 처리기(`MusicNoteExceptionHandler`)에서 해당 예외에 대한 별도의 핸들러를 정의하지 않아, 가장 포괄적인 예외 처리기인 `Exception.class` 핸들러가 이를 가로채 500 에러로 처리하고 있었다.

### 3.3 해결 방법
해당 예외를 400(Bad Request) 응답군으로 포함하도록 핸들러를 수정하였다.

```java
@ExceptionHandler(value = {
        HttpRequestMethodNotSupportedException.class,
        MethodArgumentNotValidException.class,
        MissingServletRequestParameterException.class // 신규 추가
})
public ResponseEntity<MusicNoteErrorResponse> handleBadRequest(Exception e, ...) {
    // 400 에러 응답 처리
}
```

---

## 4. @MockBean에서 @MockitoBean으로의 전환 (Migration)

### 4.1 변경 배경
Spring Boot 3.4.0 버전부터 기존에 사용되던 `org.springframework.boot.test.mock.mockito.MockBean` 어노테이션이 **지원 중단(Deprecated)** 되었다. 이는 스프링 프레임워크 자체가 Mockito와의 통합을 공식적인 프레임워크 코어 기능으로 편입시키면서 발생한 변화이다.

### 4.2 주요 차이점 및 적용
- **변경 전**: `org.springframework.boot.test.mock.mockito.MockBean`
- **변경 후**: `org.springframework.test.context.bean.override.mockito.MockitoBean`

새로운 `@MockitoBean`은 스프링의 빈 오버라이딩(Bean Overriding) 인프라를 활용하여 더욱 안정적인 목(Mock) 객체 주입을 지원한다. 본 프로젝트의 모든 컨트롤러 테스트 코드에 대해 해당 어노테이션으로의 교체 작업을 완료하였다.

---

## 5. 학습 요약 및 결론

본 과정을 통해 얻은 기술적 교훈은 다음과 같다.

1.  **관심사의 분리(SoC)**: `@SpringBootApplication` 클래스는 가급적 설정을 최소화하고, 인프라 관련 설정(`JPA`, `Security` 등)은 별도 클래스로 관리하는 것이 테스트 환경 구축에 유리하다.
2.  **슬라이스 테스트의 이해**: 각 테스트 어노테이션(`@WebMvcTest`, `@DataJpaTest` 등)이 로드하는 빈의 범위를 정확히 파악해야 환경 설정 오류를 사전에 방지할 수 있다.
3.  **라이브러리 생태계 변화 주시**: 프레임워크의 메이저/마이너 버전 업데이트 시 발생하는 지원 중단(Deprecation) 사항을 빠르게 파악하고 반영함으로써 코드의 지속 가능성을 높여야 한다.
