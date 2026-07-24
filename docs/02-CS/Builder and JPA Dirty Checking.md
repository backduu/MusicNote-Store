# 빌더 패턴과 JPA 더티 체킹

## 1. 문제 상황

애플리케이션 시작 시 초기 데이터를 넣는 `DataInitializer`에서 다음과 같은 요구가 발생할 수 있다.

- `music_lover` 사용자가 없으면 새로 생성한다.
- 사용자가 이미 있으면 같은 사용자를 갱신하고, 이후 상품과 장바구니 생성 로직을 계속 수행한다.
- 애플리케이션을 다시 실행해도 유니크 제약조건 위반이 발생하지 않아야 한다.

이 상황에서 중요한 점은 **객체 생성**과 **기존 객체 변경**은 서로 다른 작업이라는 사실이다. 빌더 패턴은 주로 객체 생성에 사용하고, JPA 더티 체킹은 조회한 기존 엔티티의 변경을 데이터베이스에 반영하는 데 사용한다.

---

## 2. 빌더 패턴

### 2.1 정의

빌더 패턴(Builder Pattern)은 생성자의 매개변수가 많거나, 일부 필드만 선택적으로 설정해야 할 때 객체를 읽기 쉽게 생성하는 생성 패턴이다.

```java
User user = User.builder()
        .username("music_lover")
        .email("lover@musicnote.com")
        .name("홍길동")
        .role(UserRole.USER)
        .status(UserStatus.ACTIVE)
        .build();
```

위 코드는 메모리에 새로운 `User` 객체를 조립한다. 이 시점에는 데이터베이스에 어떤 SQL도 실행되지 않는다.

### 2.2 빌더와 `save()`의 역할 분리

빌더와 Repository의 `save()`는 별개의 역할을 가진다.

| 구분 | 역할 | 데이터베이스 접근 |
| --- | --- | --- |
| `User.builder()` | 새 `User` 객체를 생성하고 필드를 설정한다. | 하지 않는다. |
| `build()` | 조립이 끝난 `User` 객체를 반환한다. | 하지 않는다. |
| `userRepository.save(user)` | 엔티티 상태에 따라 영속화 또는 병합을 요청한다. | `INSERT` 또는 `UPDATE`가 발생할 수 있다. |

즉, 다음 코드에서 builder는 객체를 만들고 `save()`는 그 객체를 JPA에 전달한다.

```java
User newUser = User.builder()
        .username("music_lover")
        .email("lover@musicnote.com")
        .build();

userRepository.save(newUser);
```

### 2.3 기존 엔티티 수정에 builder를 바로 사용하지 않는 이유

기존 엔티티를 수정하려고 다시 `User.builder()`를 호출하면, 대부분의 경우 `id`가 없는 **새 객체**가 만들어진다.

```java
// 기존 유저를 수정하는 코드가 아니다.
User anotherUser = User.builder()
        .username("music_lover")
        .email("lover@musicnote.com")
        .build();
```

이 객체를 저장하면 JPA는 새 엔티티로 판단하여 `INSERT`를 시도할 수 있다. 이미 같은 username 또는 email이 존재한다면 데이터베이스의 유니크 제약조건에 의해 `ORA-00001` 오류가 발생한다.

따라서 builder는 **신규 생성 경로**에서 사용하고, 기존 데이터의 수정은 DB에서 조회한 엔티티 자체를 변경하는 방식으로 처리한다.

---

## 3. JPA 엔티티의 상태와 영속성 컨텍스트

JPA는 엔티티를 영속성 컨텍스트(Persistence Context)라는 관리 영역에서 다룬다. 영속성 컨텍스트는 트랜잭션 동안 엔티티를 보관하고 상태 변화를 추적하는 1차 캐시라고 이해할 수 있다.

엔티티는 대표적으로 다음 상태를 가진다.

| 상태 | 의미 |
| --- | --- |
| 비영속(Transient) | 새로 생성했지만 영속성 컨텍스트가 관리하지 않는 객체이다. |
| 영속(Managed) | 조회하거나 `persist`한 뒤 영속성 컨텍스트가 관리하는 객체이다. |
| 준영속(Detached) | 과거에는 관리됐지만 현재 영속성 컨텍스트와 분리된 객체이다. |
| 삭제(Removed) | 삭제가 예약된 객체이다. |

```java
// 비영속 상태: 단지 자바 객체이다.
User newUser = User.builder().username("music_lover").build();

// 저장 후 영속 상태가 된다.
userRepository.save(newUser);

// 조회 결과는 보통 영속 상태이다.
User existingUser = userRepository.findByUsername("music_lover")
        .orElseThrow();
```
