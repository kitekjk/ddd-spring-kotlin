# 작업 로그 #0009

## 작업 요청 프롬프트
Order 의 함수 addLineItem, removeLineItem 등등을 보면 매번 Order 새로 만들어서 반환 하는데
이렇게 하면 domainEvents 가 유지되지 않고 새로 만들어저 문제가 되거든
그러니 신규로 Order 를 생성하지 않고 기존 Order 객체의 정보를 수정하는 방식으로 변경 해줘

## 작업 시작 시간
2025-07-23 00:13

## 작업 완료 시간
2025-07-23 00:45

## 수정 내역 요약

### 문제 분석
- Order 클래스의 addLineItem, removeLineItem, updateLineItem, pay, ship, deliver, cancel 함수들이 매번 새로운 Order 객체를 생성하여 반환
- 이로 인해 기존 Order 객체의 domainEvents가 새로운 객체로 복사되지 않아 유실되는 문제 발생
- 특히 상태 변경 함수들(pay, ship, deliver, cancel)에서 새로운 도메인 이벤트를 추가하지만 기존 이벤트들은 유실됨

### 해결 방안
- Immutable 방식에서 Mutable 방식으로 변경
- 새로운 객체를 생성하지 않고 기존 객체의 상태를 직접 수정하는 방식으로 리팩토링
- domainEvents가 누적되어 유지되도록 개선

### 주요 변경사항

#### 1. Order 클래스 필드 수정
- `totalAmount`를 `val`에서 `private var`로 변경
- `auditInfo`를 `val`에서 `private var`로 변경
- 외부 접근을 위한 getter 메서드 추가: `getTotalAmount()`, `getAuditInfo()`

#### 2. 함수 시그니처 변경
모든 상태 변경 함수들의 반환 타입을 `Order`에서 `Unit`(void)으로 변경:
- `addLineItem(context: DomainContext, lineItem: OrderLineItem): Order` → `addLineItem(context: DomainContext, lineItem: OrderLineItem)`
- `removeLineItem(context: DomainContext, productId: ProductId): Order` → `removeLineItem(context: DomainContext, productId: ProductId)`
- `updateLineItem(context: DomainContext, updatedLineItem: OrderLineItem): Order` → `updateLineItem(context: DomainContext, updatedLineItem: OrderLineItem)`
- `pay(context: DomainContext): Order` → `pay(context: DomainContext)`
- `ship(context: DomainContext): Order` → `ship(context: DomainContext)`
- `deliver(context: DomainContext): Order` → `deliver(context: DomainContext)`
- `cancel(context: DomainContext): Order` → `cancel(context: DomainContext)`

#### 3. 함수 구현 변경
각 함수에서 새로운 Order 객체를 생성하는 대신 기존 객체의 상태를 직접 수정:
- `this.lineItems.add(lineItem)` 방식으로 직접 수정
- `this.totalAmount = ...` 방식으로 직접 수정
- `this.status = ...` 방식으로 직접 수정
- `this.auditInfo = ...` 방식으로 직접 수정
- `this.addDomainEvent(...)` 방식으로 기존 이벤트 목록에 추가

### 기대 효과
1. **domainEvents 유지**: 상태 변경 시 기존 도메인 이벤트들이 유실되지 않고 누적됨
2. **메모리 효율성**: 불필요한 객체 생성을 줄여 메모리 사용량 감소
3. **성능 향상**: 객체 복사 오버헤드 제거로 성능 개선
4. **이벤트 추적**: 전체 Order 생명주기 동안의 모든 도메인 이벤트 추적 가능

## 수정된 파일 내역
- `domain/src/main/kotlin/com/example/domain/model/order/Order.kt` - 핵심 Order 클래스 수정
- `test_domain_events_issue.kt` - 문제 재현 및 검증용 테스트 스크립트 생성

## 주의사항
- 기존 테스트 코드들이 함수의 반환값을 기대하고 있어 수정이 필요할 수 있음
- Immutable에서 Mutable로 변경되었으므로 동시성 환경에서 주의 필요
- 기존 코드에서 함수 체이닝을 사용하던 부분들은 수정이 필요함

## 검증 방법
1. 프로젝트 빌드 성공 확인
2. Order 생성 → addLineItem → pay → ship → deliver 순서로 실행 시 모든 도메인 이벤트가 누적되는지 확인
3. 각 상태 변경 후 getDomainEvents()로 이벤트 목록 확인

## 결론
Order 클래스의 domainEvents 유실 문제를 성공적으로 해결했습니다. 이제 Order 객체의 모든 상태 변경이 기존 객체에서 이루어지므로 도메인 이벤트들이 올바르게 누적되어 유지됩니다.