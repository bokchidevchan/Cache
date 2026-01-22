# Git Commit 생성

당신은 리드 레벨의 개발자입니다.
현재 스테이징된 변경사항을 분석하고 적절한 커밋 메시지를 작성해주세요.

클로드가 했다고 적지마세요

## 실행 순서

1. `git status`로 작업 상태 확인 (staged/unstaged 변경사항 체크)
2. `git diff --cached`로 스테이징된 변경사항 확인
3. 변경사항이 없으면 `git diff`로 unstaged 변경사항 확인 후 안내
4. 변경사항 분석 및 커밋 메시지 작성
5. `git commit` 명령어로 커밋 생성

## 중요 규칙

- **스테이징된 변경사항이 없으면 먼저 `git add` 하라고 안내**
- **하나의 커밋은 하나의 논리적 변경만 포함** (단일 책임 원칙)
- **변경사항이 너무 크면 여러 커밋으로 분리하라고 제안**
- GitHub public repository 가능성을 고려하여 보안 검토 필수

---

## 1) 커밋 메시지 규칙

### 커밋 메시지 형식 (Conventional Commits)
```
<type>(<scope>): <subject>

<body>

<footer>
```

### 구성 요소

| 요소 | 필수 | 설명 |
|------|------|------|
| type | ✅ | 변경 유형 |
| scope | ❌ | 영향 범위 (모듈, 파일명 등) |
| subject | ✅ | 변경 요약 (50자 이내, 명령형) |
| body | ❌ | 상세 설명 (무엇을, 왜 변경했는지) |
| footer | ❌ | Breaking Changes, Issue 참조 등 |

### Type 종류

| Type | 설명 | 예시 |
|------|------|------|
| feat | 새로운 기능 추가 | 로그인 기능 구현 |
| fix | 버그 수정 | null 체크 누락 수정 |
| hotfix | 긴급 수정 | 프로덕션 크래시 수정 |
| refactor | 코드 리팩토링 | 중복 코드 제거 |
| perf | 성능 개선 | 쿼리 최적화 |
| test | 테스트 추가/수정 | 유닛 테스트 추가 |
| docs | 문서 변경 | README 업데이트 |
| style | 코드 스타일 변경 | 포맷팅, 세미콜론 |
| chore | 빌드/설정 관련 | gradle 설정 변경 |
| config | 설정 파일 변경 | lint 규칙 수정 |
| build | 빌드 관련 변경 | 빌드 스크립트 수정 |
| deps | 의존성 변경 | 라이브러리 업데이트 |
| ci | CI/CD 설정 변경 | GitHub Actions 수정 |
| db | DB 스키마/쿼리 변경 | 마이그레이션 추가 |
| security | 보안 관련 수정 | XSS 취약점 수정 |
| arch | 아키텍처 구조 변경 | 레이어 구조 변경 |
| module | 모듈 구조 변경 | 모듈 분리 |
| di | 의존성 주입 관련 | Hilt 모듈 수정 |
| i18n | 다국어/로케일 | 번역 추가 |
| cleanup | 불필요한 코드 제거 | dead code 삭제 |
| revert | 커밋 되돌리기 | 이전 커밋 revert |

---

## 2) Subject 작성 규칙

### 형식
- **50자 이내**로 작성
- **명령형** 사용 (Add, Fix, Update, Remove 등)
- 첫 글자 대문자
- 마침표 없음
- "무엇을 했는지"가 아닌 "무엇을 하는지" 관점

### 좋은 예시
```
feat(auth): Add Google OAuth login
fix(cart): Fix quantity update not reflecting
refactor(api): Extract common error handling
```

### 나쁜 예시
```
feat: added login          # 과거형 사용
fix: bug fix               # 구체적이지 않음
Update files               # type 없음, 모호함
```

---

## 3) Body 작성 규칙 (선택)

### 형식
- Subject와 빈 줄로 구분
- **72자** 줄바꿈 권장
- **무엇을**, **왜** 변경했는지 설명
- 이전 동작과 새 동작 비교 (필요시)

### 예시
```
fix(auth): Fix token refresh race condition

Previously, multiple API calls could trigger simultaneous
token refresh requests, causing authentication failures.

Now, only the first refresh request proceeds while others
wait for its completion using a mutex lock.
```

---

## 4) Footer 작성 규칙 (선택)

### Breaking Changes
```
BREAKING CHANGE: API response format changed from array to object
```

### Issue 참조
```
Closes #123
Fixes #456
Refs #789
```

---

## 5) 변경사항 분석 체크리스트

### 🔍 변경 범위 파악
- 변경된 파일 수 및 라인 수
- 신규 파일 vs 수정 파일
- 영향받는 기능/모듈

### 🎯 적절한 Type 선택
- 기능 추가인가? → feat
- 버그 수정인가? → fix
- 코드 개선인가? → refactor
- 성능 관련인가? → perf

### 🔒 보안 검토
- API 키, 시크릿 노출 여부
- 하드코딩된 credentials
- 민감 정보 로깅

### ⚠️ 커밋 분리 필요 여부
- 여러 기능이 섞여있는가?
- 리팩토링과 기능 추가가 함께인가?
- 논리적으로 분리 가능한가?

---

## 6) 커밋 명령어

### 기본 커밋
```bash
git commit -m "<type>(<scope>): <subject>"
```

### Body 포함 커밋
```bash
git commit -m "$(cat <<'EOF'
<type>(<scope>): <subject>

<body>
EOF
)"
```

### Body + Footer 포함 커밋
```bash
git commit -m "$(cat <<'EOF'
<type>(<scope>): <subject>

<body>

<footer>
EOF
)"
```

---

## 7) 결과 출력 형식

```
## 📋 변경사항 요약
- 변경 파일: <n>개
- 추가: +<n> 라인
- 삭제: -<n> 라인

## 🔍 분석 결과
- Type: <type>
- Scope: <scope>
- 주요 변경: <변경 내용 요약>

## 🔒 보안 체크
- [ ] API 키/시크릿 노출 없음
- [ ] 민감 정보 로깅 없음
- [ ] 하드코딩된 credentials 없음

## ✍️ 커밋 메시지
<생성된 커밋 메시지>

## ✅ 커밋 완료
<커밋 해시 및 결과>
```

---

## 주의사항

### 스테이징된 변경사항이 없는 경우
```
⚠️ 스테이징된 변경사항이 없습니다.
커밋할 파일을 먼저 스테이징해주세요:

# 특정 파일 스테이징
git add <file-path>

# 모든 변경사항 스테이징
git add .

# 대화형 스테이징 (부분 선택)
git add -p
```

### 변경사항이 너무 큰 경우
```
⚠️ 변경사항이 너무 많습니다 (n개 파일, +xxx/-yyy 라인)
다음과 같이 커밋을 분리하는 것을 권장합니다:

1. <첫 번째 논리적 단위>
2. <두 번째 논리적 단위>
...

대화형 스테이징으로 부분 커밋하세요:
git add -p
```

### 보안 이슈 발견 시
```
🚨 보안 이슈가 발견되었습니다!
다음 파일에서 민감 정보가 감지되었습니다:

- <file-path>: <이슈 내용>

커밋 전에 해당 내용을 제거하거나 환경 변수로 대체해주세요.
```