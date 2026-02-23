# PeakTime Code Convention

본 문서는 PeakTime 프로젝트의 **코드 작성 규칙**을 정의합니다.
협업/PR/이슈 운영 규칙은 `docs/convention/ground-rules.md`에서 관리합니다.

- 개인 취향이 아닌 **팀 합의 기준**을 명시합니다.
- PR 리뷰 시 본 문서를 기준으로 판단합니다.
- 규칙 변경이 필요하면 코드 수정 전에 문서를 먼저 수정합니다.

---

# 1. 기본 원칙

## 1.1 일관성 우선
- "더 좋은 방법"보다 **프로젝트 전체 일관성**을 우선합니다.
- 예외가 필요한 경우 PR에 이유를 명확히 작성합니다.

## 1.2 의미 중심 코드
- 메서드/변수명은 의도를 드러내도록 작성합니다.
- 주석은 "무엇"이 아니라 **"왜"**를 설명합니다.

---

# 2. 아키텍처 & 레이어 규칙

## 2.1 4-Layer 구조
- controller: 요청/응답 처리
- service: 유스케이스 조립, 트랜잭션 경계
- repository: 데이터 접근 책임
- domain(entity): 상태/불변조건/비즈니스 규칙

> Controller는 얇게, 도메인은 단단하게 유지합니다.

## 2.2 책임 분리
- Controller는 비즈니스 로직을 포함하지 않습니다.
- Service는 엔티티 상태를 직접 set 하지 않습니다.
- 상태 변경은 반드시 도메인 메서드를 통해 수행합니다.

---

# 3. 네이밍 & 스타일

## 3.1 네이밍 규칙
- Controller: `XxxController`
- Service: `XxxService`
- Repository: `XxxRepository`
- DTO: `XxxReq`, `XxxRes`
- Exception: `XxxException`
- ErrorCode: `XxxErrorCode`

## 3.2 어노테이션 순서(권장)
```java
@RestController
@RequestMapping("/api/v2/...")
@RequiredArgsConstructor
public class XxxController {}
```

---

# 4. Entity 설계 규칙

## 4.1 필드/생성
- 필드는 `private`을 기본으로 합니다.
- 생성은 `@Builder` 또는 명시적 생성자/팩토리로 제어합니다.
- 컬렉션은 기본 초기화합니다.

```java
private final List<OrderItem> items = new ArrayList<>();
```

## 4.2 상태 변경 (Setter 지양)

✅ 권장
```java
order.cancel();
chatRoom.markMatched();
```

❌ 지양
```java
order.setStatus(CANCELED);
```

## 4.3 영속성
- 연관관계는 기본 `LAZY`
- 양방향 연관관계는 편의 메서드 제공
- `nullable=false` 등 제약은 도메인 규칙에 근거하여 설정

---

# 5. DTO 규칙

## 5.1 불변 지향
- DTO는 비즈니스 로직을 포함하지 않습니다.
- 불변 객체를 지향합니다.
- 변경이 필요 없는 경우 `record` 사용을 권장합니다.

```java
public record LoginReq(String email, String password) {}
```

## 5.2 네이밍
- 요청: `XxxReq`
- 응답: `XxxRes`

---

# 6. 트랜잭션 & 외부 자원

## 6.1 @Transactional 내부 외부 I/O 지양
- S3, SMTP, 외부 API 호출을 직접 수행하지 않습니다.
- 필요 시 보상 로직을 명시합니다.

권장 전략:
1. 트랜잭션 커밋 이후 처리
2. 실패 시 보상 트랜잭션
3. 저장소 레벨에서 원자성 보장

---

# 7. API 설계 규칙

## 7.1 버전
- `/api/v2/...` 형태 유지
- 버전 변경 시 문서/명세서 동시 업데이트

## 7.2 조회/검색
- 목록 조회는 페이징 기본 적용
- 복합 검색은 Query Parameter 확장 방식 우선

예:
```
/api/v2/admin/users?page=1&size=20&keyword=...
```

## 7.3 공통 응답
- 공통 응답 포맷 사용 시 반드시 예시를 문서에 포함합니다.

---

# 8. 예외 처리 & 에러 코드

- 예외는 `GlobalExceptionHandler`에서 일괄 처리합니다.
- 비즈니스 예외는 `ErrorCode` enum으로 관리합니다.
- 인증/인가 오류는 401/403으로 구분합니다.

권장 상태 코드:
- 400: 잘못된 요청
- 401: 인증 실패
- 403: 권한 부족
- 404: 리소스 없음
- 409: 충돌/중복
- 500: 서버 내부 오류

---

# 9. JavaDoc 규칙

## 9.1 작성 대상
- public / protected 클래스 및 메서드에 작성합니다.
- private 요소에는 작성하지 않습니다.

## 9.2 작성 원칙
- 첫 문장은 요약 문장으로 작성합니다.
- 메서드 이름으로 유추 가능한 설명은 지양합니다.
- 비즈니스 규칙/제약 조건은 명확히 작성합니다.
- `@param`, `@return`, `@throws`를 필요한 경우 작성합니다.

---

# 10. 테스트 컨벤션

- Controller / Service 단위 테스트를 기본으로 합니다.
- given / when / then 구조를 유지합니다.
- 외부 자원은 Mocking 처리합니다.
- 테스트는 독립적으로 실행 가능해야 합니다.

---

# 11. 조회/성능 규칙

- 목록 조회는 페이징을 기본 적용합니다.
- N+1 문제가 발생하지 않도록 fetch 전략을 검토합니다.
- 단순 조회는 EntityGraph, 복합 조건은 Querydsl을 고려합니다.

---

# 12. 동시성 / 멱등성

- 상태 변경 API는 멱등성을 고려합니다.
- 중복 요청에 대한 정책을 명확히 합니다.
- 필요 시 DB 제약, Optimistic Lock, 분산락을 사용합니다.

---

# 13. 로깅 규칙

## 13.1 로그 레벨
- debug: 디버깅 목적
- info: 주요 이벤트
- warn: 사용자 오류
- error: 시스템 오류

## 13.2 민감정보 보호
- 토큰, 비밀번호, 인증코드는 로그에 남기지 않습니다.
