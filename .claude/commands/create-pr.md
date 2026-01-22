# Pull Request 생성

당신은 리드 레벨의 개발자입니다.
현재 브랜치의 변경사항을 분석하고 Pull Request를 생성해주세요.

클로드가 했다고 적지마세요.

## 실행 순서

1. `git branch --show-current`로 현재 브랜치 확인
2. `git status`로 작업 상태 확인 (커밋되지 않은 변경사항 체크)
3. `git log main..HEAD --oneline`으로 main 브랜치 이후 커밋 목록 확인
4. `git diff main...HEAD`로 전체 변경사항 확인
5. 변경사항 분석 및 PR 내용 작성
6. `gh pr create` 명령어로 PR 생성

## 중요 규칙

- **main 브랜치에서는 실행하지 마세요** - feature/bugfix 브랜치에서만 PR 생성
- **커밋되지 않은 변경사항이 있으면 먼저 커밋하라고 안내**
- **remote에 push되지 않았다면 push 먼저 수행**
- GitHub public repository 가능성을 고려하여 보안 검토 필수

---

## 1) PR 제목 규칙

PR 제목 형식:
```
<이모지> <type>(<scope>): <subject>
```

### Type + Emoji 매핑

| Type | Emoji | 설명 |
|------|-------|------|
| feat | ✨ | 새로운 기능 추가 |
| fix | 🐛 | 버그 수정 |
| hotfix | 🚑 | 긴급 수정 |
| refactor | ♻️ | 코드 리팩토링 |
| perf | 🚀 | 성능 개선 |
| test | ✅ | 테스트 추가/수정 |
| docs | 📚 | 문서 변경 |
| style | 🎨 | 코드 스타일 변경 |
| ui | 💄 | UI/UX 관련 변경 |
| chore | 🧹 | 빌드/설정 관련 |
| config | 🔧 | 설정 파일 변경 |
| build | 🧰 | 빌드 관련 변경 |
| deps | 📦 | 의존성 변경 |
| ci | 🤖 | CI/CD 설정 변경 |
| db | 🗃️ | DB 스키마/쿼리 변경 |
| security | 🔒 | 보안 관련 수정 |
| arch | 🧱 | 아키텍처 구조 변경 |
| module | 🧩 | 모듈 구조 변경 |
| di | 💉 | 의존성 주입 관련 |
| i18n | 🌍 | 다국어/로케일 |
| cleanup | 🧼 | 불필요한 코드 제거 |
| revert | ⏪ | 커밋 되돌리기 |

---

## 2) PR 본문 템플릿

```markdown
## Summary
<변경사항 요약 - 1~3개 bullet point>

## Changes
<주요 변경 파일 및 내용>

## Test plan
<테스트 방법 체크리스트>

## Notes (Optional)
<리뷰어가 알아야 할 추가 정보>
```

---

## 3) 변경사항 분석 체크리스트

### 🔍 변경 범위 파악
- 변경된 파일 수 및 라인 수
- 신규 파일 vs 수정 파일
- 영향받는 기능/모듈

### 🔒 보안 검토 (GitHub Public Repo 고려)
- API 키, 시크릿 노출 여부
- 하드코딩된 credentials
- 민감 정보 로깅

### ⚠️ Breaking Changes
- API 변경 여부
- 기존 기능에 영향 여부
- 마이그레이션 필요 여부

---

## 4) PR 생성 명령어

### 기본 PR 생성
```bash
gh pr create --title "<PR 제목>" --body "$(cat <<'EOF'
## Summary
<요약>

## Changes
<변경사항>

## Test plan
- [ ] 테스트 항목
EOF
)"
```

### Draft PR 생성 (리뷰 준비 전)
```bash
gh pr create --draft --title "<PR 제목>" --body "<본문>"
```

### 특정 브랜치로 PR 생성
```bash
gh pr create --base <target-branch> --title "<PR 제목>" --body "<본문>"
```

---

## 5) 결과 출력 형식

```
## 📋 브랜치 정보
- 현재 브랜치: <branch-name>
- 대상 브랜치: main
- 커밋 수: <n>개

## 📝 커밋 목록
- <커밋 메시지 목록>

## 🔍 변경사항 요약
- 변경 파일: <n>개
- 추가: +<n> 라인
- 삭제: -<n> 라인

## 🔒 보안 체크
- [ ] API 키/시크릿 노출 없음
- [ ] 민감 정보 로깅 없음
- [ ] 하드코딩된 credentials 없음

## ✍️ PR 생성
- 제목: <PR 제목>
- 본문: <PR 본문 미리보기>

## 🔗 PR URL
<생성된 PR URL>
```

---

## 주의사항

### main 브랜치인 경우
```
⚠️ 현재 main 브랜치입니다.
PR을 생성하려면 먼저 feature 브랜치를 생성하세요:

git checkout -b feature/<feature-name>
```

### 커밋되지 않은 변경사항이 있는 경우
```
⚠️ 커밋되지 않은 변경사항이 있습니다.
PR 생성 전에 먼저 커밋해주세요:

git add .
git commit -m "<커밋 메시지>"
```

### Remote에 push되지 않은 경우
```
⚠️ 현재 브랜치가 remote에 push되지 않았습니다.
PR 생성을 위해 push를 진행합니다:

git push -u origin <branch-name>
```