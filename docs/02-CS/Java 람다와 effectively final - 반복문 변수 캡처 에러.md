# Java 람다와 effectively final: 반복문 변수 캡처 에러 완전 정복

본 문서는 Java에서 람다식이 외부 지역 변수를 캡처할 때 발생하는 제약을 심층적으로 설명하고, 실무에서 마주치는 대표적인 문제 상황과 해결 패턴을 정리한다.

---

## 1. 개념 및 문제 상황

Java에서 람다식(`() -> { ... }`) 내부에서 외부 블록(Enclosing Scope)의 지역 변수를 참조할 때 핵심 제약이 있다. 다음과 같은 컴파일 에러가 발생할 수 있다.

> Error: "Variable used in lambda expression should be final or effectively final"

### 1.1. 원인: 반복문 변수의 재할당

```java
for (int i = 1; i <= 30; i++) {
    // 에러 발생: i는 반복마다 값이 증가(재할당)하므로 effectively final이 아님
    productRepository.findByTitle(title).ifPresentOrElse(
        Product::updateCreatedAt,
        () -> {
            // 람다 내부에서 i 참조 시도
            int price = basePrices[0] + (i * 100);
        }
    );
}
```

`for (int i = 1; i <= 30; i++)`에서 지역 변수 `i`는 매 반복(Iteration)마다 증감식 `i++`에 의해 값이 변경된다. Java의 람다식은 값이 변하는 지역 변수를 내부로 캡처(Capture)할 수 없도록 설계되어 있으므로 컴파일 에러가 발생한다.

---

## 2. 심화: 왜 final 또는 effectively final이어야 하는가?

람다식 내부에서 외부 지역 변수를 사용할 수 있게 하는 메커니즘을 람다 캡처링(Lambda Capturing)이라고 한다. Java가 캡처링 대상 변수에 대해 `final` 속성을 강제하는 이유는 다음과 같은 메모리 구조와 동시성 문제 때문이다.

1. 메모리 할당 위치와 생명주기의 차이
   - 지역 변수: 스레드(Thread)의 스택(Stack) 메모리 영역에 생성되며, 메서드 실행이 끝나면 즉시 소멸한다.
   - 람다식(인스턴스): 힙(Heap) 메모리 영역에 객체 형태로 생성된다.
   - 문제점: 람다식이 별도의 스레드에서 비동기적으로 실행되거나, 메서드 반환 이후에 지연 실행될 때, 참조하려던 스택 영역의 지역 변수는 이미 소멸되었을 수 있다.
2. 값의 복사 (Copying)
   - 위 생명주기 차이를 극복하기 위해, Java는 지역 변수의 실제 참조가 아닌 '값의 복사본'을 람다식 내부로 전달하여 사용한다.
3. 데이터 정합성 (동시성 문제 방지)
   - 원본 지역 변수의 값이 계속 변경될 수 있다면, 람다식 내부의 복사본 값과 외부의 원본 값 사이에 불일치(Inconsistency)가 발생한다.
   - 이를 원천적으로 방지하기 위해, Java는 한 번 할당된 후 절대 값이 변하지 않음(final 또는 effectively final)을 보장하는 변수만 복사할 수 있도록 제한한다.

> 효과적인 final(Effectively Final)이란?
>
> Java 8부터 도입된 개념으로, 변수 선언 시 명시적으로 `final` 키워드를 붙이지 않았더라도, 초기화된 이후 다시 값을 할당(재할당)하는 코드가 존재하지 않아 사실상 final처럼 동작하는 변수를 의미한다.

### 2.1. 익명 클래스와의 유사점

람다 등장 이전부터 익명 내부 클래스도 동일한 제약을 가졌다. 이는 언어 설계의 일관성으로, 지역 변수 캡처는 항상 final/effectively final만 허용된다.

### 2.2. 자바 메모리 모델 관점의 추가 설명

- 캡처되는 지역 변수는 람다 생성 시점의 값으로 "복사"된다. 원본 지역 변수가 이후 변경(재할당)되는 경우, 람다 내부 값과 차이가 발생할 수 있으므로 컴파일 타임에 차단한다.
- 캡처 대상이 객체 참조일 경우: 참조 자체는 바뀌지 않는다면 effectively final이지만, 참조가 가리키는 객체의 내부 상태는 변경(mutability)될 수 있다. 이는 컴파일러가 제어하지 않는 영역이므로 동시성에 주의가 필요하다.

---

## 3. 해결 방안

문제를 해결하기 위해서는 값이 계속 변하는 제어 변수 `i`를 직접 참조하는 대신, 루프 내부에서 한 번만 초기화되고 변경되지 않는 새로운 지역 변수에 값을 복사하여 람다식에 전달해야 한다.

### 3.1. 올바른 코드 적용 예시

```java
for (int i = 1; i <= 30; i++) {
    final int currentI = i; // ✅ [해결] effectively final 변수로 복사 (명시적 final)

    // idx는 (i - 1) % 10 으로 한 번만 초기화되고 이후 값이 바뀌지 않음.
    // 명시적 final이 없어도 'effectively final'로 인정됨.
    int idx = (i - 1) % 10;

    productRepository.findByTitle(title).ifPresentOrElse(
            Product::updateCreatedAt,
            () -> {
                Product product = Product.builder()
                        .title(title)
                        // ✅ i 대신 값이 고정된 currentI와 idx를 사용
                        .price(new java.math.BigDecimal(basePrices[idx] + (currentI * 100)))
                        // ... 다른 필드 설정
                        .build();

                productsToSave.add(product);
            }
    );
}
```

### 3.2. 코드 분석

- `currentI` 변수: 반복문이 1회전 할 때마다 스택에 새롭게 생성되며, `i`의 값을 한 번 할당받은 뒤 변경되지 않으므로 람다식 내부로 안전하게 캡처된다.
- `idx` 변수: `final` 키워드를 생략했지만, 생성 후 재할당되는 코드가 없으므로 컴파일러에 의해 effectively final로 취급되어 람다 내부에서 정상적으로 참조가 가능하다.

---

## 4. 추가 예시와 변형 상황

### 4.1. Stream/forEach에서의 인덱스 사용

배열이나 컬렉션을 스트림으로 순회할 때 인덱스가 필요하다면 다음과 같은 패턴을 고려할 수 있다.

```java
// IntStream.range를 이용해 인덱스를 명시적으로 만든다
java.util.stream.IntStream.range(0, list.size())
        .forEach(idx -> {
            final int i = idx; // 명시적 복사 (사실 여기서는 불필요하지만 가독성 차원에서 사용 가능)
            var item = list.get(i);
            // 람다 내부에서 i 사용 가능
        });
```

### 4.2. 비동기 작업(CompletableFuture)와 캡처

```java
for (int i = 0; i < tasks.size(); i++) {
    final int taskNo = i; // 반드시 복사하여 캡처
    java.util.concurrent.CompletableFuture.runAsync(() -> {
        // 비동기 스레드에서 taskNo 사용
        process(tasks.get(taskNo));
    });
}
```

비동기 환경에서는 원본 지역 변수의 생명주기와 실행 타이밍이 분리되므로, 캡처된 값이 명확히 고정되어 있어야 한다.

### 4.3. 익명 클래스에서도 동일

```java
for (int i = 0; i < n; i++) {
    final int fixed = i;
    new Thread(new Runnable() {
        @Override public void run() {
            System.out.println(fixed);
        }
    }).start();
}
```

---

## 5. 다른 해결 패턴과 트레이드오프

다음은 루프 내 지역 복사 외에 자주 거론되는 대안들이다. 각 방법의 장단점을 이해하고 상황에 맞게 사용하자.

1. IntStream.range 사용
   - 장점: 함수형 스타일, 인덱스를 안전하게 제공.
   - 단점: 과도한 스트림 사용은 디버깅/성능 면에서 오히려 복잡해질 수 있음.

2. 래퍼(Container) 활용: AtomicInteger, 1-원소 배열
   - 예시: `AtomicInteger idx = new AtomicInteger(0); list.forEach(x -> idx.getAndIncrement());`
   - 장점: 캡처되는 참조는 final이지만 내부 상태는 변경 가능하므로 가변 상태 전달 가능.
   - 단점: 가독성 저하, 동시성 안전성에 대한 오해 가능(AtomicInteger는 연산이 원자적일 뿐, 모든 복합 시나리오를 자동으로 안전하게 만들지는 않음).
   - 주의: 이 패턴은 인덱스 증가 같이 간단한 상황에서만 제한적으로 고려하고, 가능하면 루프 복사(권장) 또는 IntStream.range를 선호.

3. 외부 스코프의 필드 사용
   - 장점: 로컬 제약 회피 가능.
   - 단점: 공유 상태 증가로 사이드 이펙트/동시성 문제 유발. 테스트/유지보수 난이도 증가.

---

## 6. 흔한 실수와 안티패턴

- 루프 변수 직접 사용: `for (...) { repo.save(() -> use(i)); }` 형태에서 `i` 직접 캡처는 불가.
- 캡처 대상을 재할당: 람다 외부에서 `obj = new Obj()`로 참조를 바꾸면 effectively final이 아님.
- 동시성 오해: 참조가 final이어도 참조 대상 객체의 내부 상태는 여전히 변경될 수 있음. 멀티스레드에서 가시성/동기화 문제를 고려해야 함.
- 불필요한 AtomicInteger 남용: 단일 스레드 문맥에서 단순히 인덱스를 쓰려는 목적이라면 오히려 가독성만 해친다.

---

## 7. FAQ

- Q1. 왜 지역 변수를 참조가 아닌 값 복사로 처리하나요?
  - 지역 변수는 스택에 존재하고 수명도 짧다. 람다는 힙에 살아남을 수 있으므로 안전을 위해 값 스냅샷을 복사한다.

- Q2. 객체를 캡처하면 내부 상태 변경은 가능하나요?
  - 가능하다. 하지만 참조 자체가 재할당되면 안 된다. 내부 상태 변경은 동시성 위험을 동반할 수 있으니 필요 시 동기화/불변 객체 패턴을 고려.

- Q3. `final`을 붙이지 않아도 되나요?
  - 재할당이 없다면 effectively final로 인정되어 `final` 키워드가 없어도 된다. 다만 명시적으로 `final`을 붙이면 의도를 더 명확히 드러낼 수 있다.

- Q4. 증가하는 카운터를 람다에서 쓰려면?
  - 권장: 인덱스가 필요한 경우 `IntStream.range`, 또는 루프 내 지역 복사. 불가피하면 `AtomicInteger`나 1-원소 배열 같은 래퍼를 신중히 사용.

---

## 8. 체크리스트(Quick Guide)

- 캡처하려는 지역 변수가 재할당되는가? 재할당된다면 람다 내부로 직접 넘기지 말 것.
- 루프 내부에서 한 번만 초기화되는 지역 복사 변수를 만들었는가?
- 비동기 실행 여부를 고려했는가? 캡처된 값이 실행 시점까지 유효한가?
- 공유 상태/동시성 이슈는 없는가? 필요한 경우 불변 객체/동기화 사용.

---

## 9. 요약

- 람다에서 외부 지역 변수를 캡처하려면 해당 변수는 `final` 또는 `effectively final`이어야 한다.
- 반복문 제어 변수는 값이 변하므로 직접 캡처할 수 없다. 루프 내부 지역 복사 변수를 만들어 사용하라.
- 스트림 인덱싱, 비동기 실행, 익명 클래스 등 다양한 문맥에서도 동일 원칙이 적용된다.

---

## 10. 참고 자료

- Java Language Specification, Lambda Expressions
- Oracle Java Tutorials: Lambda Expressions
- Brian Goetz, "State of the Lambda"
- Java Memory Model (JMM) 관련 자료
