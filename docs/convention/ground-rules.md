# PeakTime Ground Rules

본 문서는 PeakTime 프로젝트의 **협업 및 운영 규칙**을 정의합니다.
코드 작성 규칙은 `code-convention.md`에서 관리합니다.

---

# 1. 이슈 운영 규칙

- 모든 작업은 Issue 기반으로 진행합니다.
- Issue에는 목적, 범위, 완료 조건을 명확히 작성합니다.
- 큰 작업은 Task 단위로 분리합니다.

## 1.1 Definition of Done

- 기능은 검증 방법을 포함해야 합니다. (curl, 로그, 테스트 등)
- 서버 작업은 재현 가능한 문서로 남깁니다.
- 문서 변경은 PR로 관리합니다.

---

# 2. 브랜치 전략

- main: 배포 브랜치
- develop: 개발 통합 브랜치
- feature/{feature-name}
- fix/{fix-name}
- hotfix/{hotfix-name}

---

# 3. PR 작성 규칙

## 3.1 기본 원칙
- PR은 하나의 책임 단위로 작성합니다.
- PR 제목은 작업 범위를 드러내야 합니다.

예:
- [feat] RefreshToken Rotation 적용
- [fix] OAuth redirect URI 수정
- [docs] Code Convention 문서 정리
- [infra] Nginx HTTPS 적용

## 3.2 PR 본문 필수 항목
- 관련 Issue close
- 작업 내용 요약
- 검증 방법
- 영향 범위

---

# 4. 리뷰 기준

- 리뷰는 code-convention.md를 기준으로 진행합니다.
- 취향이 아닌 문서 기준으로 코멘트합니다.
- 새로운 기준은 문서 반영 후 적용합니다.

## 4.1 리뷰 우선순위
1. 기능 정합성
2. 보안(인증/인가)
3. 트랜잭션/동시성
4. 성능(N+1)
5. 가독성/네이밍

---

# 5. 문서 위치 규칙

- 인프라: docs/infra/
- 컨벤션: docs/convention/
- 아키텍처: docs/architecture/

---

# 6. 변경 관리 정책

- 규칙 변경 시 문서를 먼저 수정합니다.
- 문서 변경은 PR로 관리합니다.
- 합의된 기준은 다음 PR부터 적용합니다.