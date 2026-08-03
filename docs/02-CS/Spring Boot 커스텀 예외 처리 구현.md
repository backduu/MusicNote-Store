# Spring Boot 커스텀 예외 처리 구현 (Custom Exception Handling)

본 문서는 MusicNote-Store 프로젝트에서 비즈니스 로직 수행 중 발생하는 예외를 전역적으로 관리하고, 클라이언트에게 일관된 응답을 제공하기 위해 설계 및 구현된 **글로벌 예외 처리 시스템**에 대해 기술한다.

---

## 1. 개요

Spring Boot의 `@RestControllerAdvice`를 활용하여 애플리케이션 전반의 예외를 중앙에서 가로채어 처리한다. 이를 통해 서비스 계층(Service Layer)의 비즈니스 로직은 복잡한 `try-catch` 구문 없이 예외를 발생시키는(throw) 것에만 집중할 수 있으며, 클라이언트는 표준화된 형식의 에러 응답을 수신하게 된다.

## 2. 주요 구성 요소

### 2.1 ErrorCode (Enum)

발생 가능한 예외 상황을 규격화하여 관리한다. 각 에러 상수는 **HTTP 상태 코드(HttpStatus)**와 **에러 메시지(Message)**를 속성으로 가진다.

```java
@AllArgsConstructor
@Getter
public enum ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력값"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없음"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청");

    private final HttpStatus httpStatus;
    private final String message;
}
```

### 2.2 MusicNoteException (Custom Exception)

비즈니스 로직에서 의도적으로 발생시키는 전용 예외 클래스이다. `RuntimeException`을 상속받으며, 예외 발생 시 해당 상황에 맞는 `ErrorCode`를 함께 전달하도록 설계한다.

```java
@Getter
public class MusicNoteException extends RuntimeException {
    private String message;
    private ErrorCode errorCode;

    public MusicNoteException(String message, ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.message = message;
        this.errorCode = errorCode;
    }

    public MusicNoteException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
```

### 2.3 MusicNoteErrorResponse (DTO)

클라이언트에게 최종적으로 전달되는 에러 응답 객체이다. 일관된 JSON 포맷을 보장함으로써 클라이언트 사이드의 예외 처리 효율을 높인다.

```java
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MusicNoteErrorResponse {
    private String message;
    private ErrorCode errorCode;
}
```

### 2.4 MusicNoteExceptionHandler (Global Handler)

모든 컨트롤러에서 발생하는 예외를 감지하고 적절한 응답을 생성하는 핵심 컴포넌트이다.

- **`@ExceptionHandler(MusicNoteException.class)`**: 정의된 커스텀 예외를 포착하여 `ErrorCode`에 설정된 상태 코드와 메시지로 응답을 구성한다.
- **`@ExceptionHandler(Exception.class)`**: 예상치 못한 시스템 수준의 예외를 처리하여 서버 내부 정보 노출을 방지한다.
- **Logging**: `HttpServletRequest` 정보를 참조하여 에러가 발생한 URL과 상세 내용을 로그로 기록함으로써 사후 분석을 용이하게 한다.

---

## 3. 적용 사례

서비스 계층(Service Layer)에서 다음과 같이 간결하게 예외를 발생시킬 수 있다.

```java
@Override
public List<ProductDTO.Response> searchProducts(String keyword, int page, int size) {
    if (keyword == null || keyword.isBlank()) {
        // 커스텀 예외 발생
        throw new MusicNoteException(ErrorCode.INVALID_INPUT);
    }
    // ... 비즈니스 로직 수행 ...
}
```

---

## 4. 구현의 장점 및 핵심 포인트

1.  **응답의 일관성**: 에러 발생 시 항상 동일한 JSON 구조를 반환하여 프론트엔드 시스템과의 인터페이스를 단순화한다.
2.  **유지보수의 용이성**: 신규 에러 유형이 발생할 경우 `ErrorCode` Enum에 상수를 추가하는 것만으로 처리가 가능하다.
3.  **동적 상태 코드 처리**: 예외의 성격에 따라 적절한 HTTP 상태 코드(`400`, `404`, `500` 등)를 유연하게 반환한다.
4.  **관심사의 분리(SoC)**: 비즈니스 로직과 예외 처리 로직을 분리하여 코드의 가독성 및 관리 효율을 향상시킨다.

## 5. 향후 개선 방향 (Implementation Tip)

- **상속 구조의 최적화**: `MusicNoteException` 클래스에서 `message` 필드를 중복 선언하지 않고, 부모 클래스인 `RuntimeException`의 필드를 `super(message)`를 통해 활용하여 객체 지향적 설계를 강화한다.
- **세부 에러 정보 제공**: `@Valid` 어노테이션을 통한 유효성 검증 실패 시, 에러가 발생한 구체적인 필드 리스트를 `ErrorResponse`에 포함시켜 디버깅 정보를 확장할 수 있다.
