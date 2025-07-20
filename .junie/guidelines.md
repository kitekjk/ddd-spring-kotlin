# 🧠 Junie 프로젝트 코드 작성 가이드라인

이 가이드는 Junie가 Spring Boot 3.7 + Kotlin 기반의 도메인 주도 설계(DDD) 멀티모듈 프로젝트에서 정확하고 일관된 코드 생성을 수행할 수 있도록 하는 기준 문서입니다.

---

## 📦 프로젝트 모듈 구조

```
project-root/
├── domain/
│   └── model/
│       ├── {aggregate}/       # Aggregate별 서브패키지
│       └── service/           # 여러 Aggregate 관련 도메인 서비스
├── application/
│   └── {context}/             # UseCase 및 서비스
├── infrastructure/
│   └── persistence/           # DB, 외부 구현체
├── interfaces/
│   └── web/                   # REST Controller 등 API 계층
```

---

## 📌 모듈별 책임

### 0. 공통
- Dto 클래스는 data class로 만들고 dto 패키지명으로 분리

### 1. domain 모듈

- 비즈니스 로직, 도메인 모델 정의
- 순수 Kotlin 코드로 작성
- 외부 라이브러리(Spring, JPA 등) 금지
- Aggregate Root 내에 비즈니스 로직을 만듬
- 여러 Aggregate와 연관된 비즈니스 로직은 도메인 서비스로 분리

**하위 구성요소 및 역할:**

| 구성요소 | 설명 |
|----------|------|
| Aggregate Root | 도메인 로직과 상태 변경의 진입점 |
| Entity | 식별자가 존재하며 변경 가능한 객체 |
| Value Object | 식별자 없고 불변, 의미 기반 타입 |
| Repository Interface | 도메인에서 정의하는 저장소 인터페이스 |
| Domain Service | 복수 Aggregate 간 도메인 규칙, domain/service에 위치 |

**패키지 구조 예시:**
```
domain/model/order/
├── Order.kt              # Aggregate Root
├── OrderItem.kt          # Entity
├── OrderId.kt            # VO
├── OrderRepository.kt    # Repository 인터페이스

domain/service/
└── OrderPolicyService.kt # 도메인 서비스
```

### 2. application 모듈

- UseCase 단위로 정의하고 AppService 를 postfix 로 선언
- AppService 는 서로 참조 금지
- 트랜잭션 경계 책임
- Spring Context 의존 허용 (`@Service`, `@Transactional` 등)
- domain만 의존 (infrastructure에 의존 금지)
- 별도의 interfaces 를 사용하지 않고 class로 선언하고 하나의 public 함수만 사용(단일책임원칙)
- @Transactional 을 함수가 아닌 class에 선언
- 비즈니스 로직은 도메인에서 다루게 하고, orchastration 역할만 사용

**구조 예시:**
```kotlin
@Service
@Transactional
class PlaceOrderAppService(
    private val orderRepository: OrderRepository
) {
    fun execute(command: PlaceOrderCommand): OrderResult { ... }
}
```

### 3. infrastructure 모듈

- 기술 구현 (JPA, Redis, Kafka 등)
- Repository, 외부 API 구현체 등
- domain의 인터페이스를 구현

**예시:**
```kotlin
@Repository
class OrderRepositoryImpl(
    private val jpaRepo: JpaOrderJpaRepository
) : OrderRepository {
    override fun save(order: Order): Order { ... }
}
```

### 4. interfaces 모듈

- 외부 요청과 내부 시스템 간의 API 인터페이스 역할
- REST Controller, 메시지 수신 핸들러 등
- DTO ↔ Command 변환 수행, UseCase 호출 담당

**예시:**
```kotlin
@RestController
@RequestMapping("/orders")
class OrderController(
    private val placeOrderUseCase: PlaceOrderUseCase
) {
    @PostMapping
    fun placeOrder(@RequestBody req: PlaceOrderRequest): ResponseEntity<OrderResponse> {
        val command = req.toCommand()
        val result = placeOrderUseCase.execute(command)
        return ResponseEntity.ok(OrderResponse.from(result))
    }
}
```

---

## 📚 코드 작성 규칙

### ✅ 공통

- Kotlin idiomatic style (`val`, null-safety, `data class`, `sealed class`)
- VO는 `@JvmInline` 또는 `data class`
- 객체 생성을 위한 `create()` 정적 메서드 권장
- enum보다는 sealed class 선호

### ✅ domain

- Spring, JPA, 외부 라이브러리 금지
- 테스트 가능한 순수 Kotlin 코드
- ID와 VO로 명확한 경계 표현

### ✅ application

- UseCase는 인터페이스 + Service 조합
- 트랜잭션은 ApplicationService에서 선언
- 외부 기술 의존 없이 domain만 호출

### ✅ infrastructure

- 기술 구현체는 domain 인터페이스 구현
- Spring Data JPA 등은 여기에만 위치
- 외부 API 연동도 여기에 구현

### ✅ interfaces

- Controller는 UseCase만 호출
- DTO ↔ Command 변환 로직만 포함
- 비즈니스 로직 작성 금지

---

## 📂 예시 구조: Order Aggregate

```
domain/
└── model/
    ├── order/
    │   ├── Order.kt
    │   ├── OrderItem.kt
    │   ├── OrderId.kt
    │   ├── OrderStatus.kt
    │   ├── OrderRepository.kt
    └── service/
        └── OrderPolicyService.kt

application/
└── order/
    ├── PlaceOrderAppService.kt

infrastructure/
└── persistence/
    └── order/
        └── OrderRepositoryImpl.kt

interfaces/
└── web/
    └── order/
        └── OrderController.kt
```

---



## 🔒 주의사항 요약

- ❌ Controller에서 비즈니스 로직 수행 금지
- ❌ application → infrastructure 의존 금지
- ❌ domain에서 Spring 의존 금지
- ✅ 모든 도메인 로직은 테스트 가능하게 작성

---

## 테스트 코드 작성

- Kotest을 사용
- Given-When-Then 패턴을 이용
- Kotest의 shouldBe, shouldNotBe, shouldThrow 등의 매처를 사용
- 다양한 엣지 케이스에 대한 테스트 코드 작성
- 모든 입력 유효성 검사 테스트

## 작업 로그

- .junie/logs/worklog-[####].md 파일을 만들고 작업한 내역을 기록해줘(#### 는 순번)
- 작업 요청할때 입력한 프롬프트
- 작업 시작 시간
- 작업 완료 시간
- 수정 내역 요약
- 수정된 파일 내역


## ✅ 이 문서는 Junie가 자동 인식하여 코드 생성 시 참조됩니다.
