# Semaphore Demo App

세마포어(Semaphore)의 필요성을 직관적으로 보여주는 Android 데모 앱입니다.

## 개요

이 앱은 네트워크 요청 시 **세마포어 없이 동시 요청**을 했을 때 발생하는 문제점을 시각적으로 보여줍니다.

### 문제 상황
- 세마포어 없이 10개 요청 → **10개가 한번에 서버로 전송**
- 서버 과부하, Rate Limit 초과, 메모리 급증 등의 문제 발생

### 해결 방법
- 세마포어로 동시 요청 수 제한 (예: 최대 3개)
- 안정적인 순차 처리

## 스크린샷

앱 실행 시:
- 큰 숫자로 **현재 동시 요청 수** 표시
- 동시 요청 수에 따라 카드 색상 변경 (노랑 → 주황 → 빨강)
- 실시간 로그로 각 요청의 시작/완료 확인

## 테스트 결과

```
┌─────────────────┬───────────────┬───────────────┐
│                 │ 세마포어 없음  │ 세마포어 있음  │
├─────────────────┼───────────────┼───────────────┤
│ 총 소요 시간     │         510ms │        2018ms │
│ 최대 동시 요청   │          10개 │           3개 │
│ 서버 부하       │      높음      │      낮음      │
└─────────────────┴───────────────┴───────────────┘
```

- **세마포어 없음**: 빠르지만 서버에 부하
- **세마포어 있음**: 느리지만 안정적

## 프로젝트 구조

```
app/src/main/java/io/github/bokchidevchan/cache/
├── MainActivity.kt           # UI (Jetpack Compose)
├── MainViewModel.kt          # 동시 요청 로직
└── data/
    └── SlowApi.kt            # DelayInterceptor (1.5초 딜레이)

app/src/test/java/io/github/bokchidevchan/cache/
└── SemaphoreComparisonTest.kt  # 성능 비교 테스트
```

## 핵심 코드

### DelayInterceptor (느린 API 시뮬레이션)
```kotlin
class DelayInterceptor(private val delayMs: Long) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        Thread.sleep(delayMs)  // 1.5초 딜레이
        return chain.proceed(chain.request())
    }
}
```

### 세마포어 없이 동시 요청
```kotlin
// 모든 요청이 한번에 실행됨
(1..10).map { id ->
    async { api.getPhoto(id) }
}.awaitAll()
```

### 세마포어로 동시 요청 제한
```kotlin
val semaphore = Semaphore(3)  // 동시 3개 제한

(1..10).map { id ->
    async {
        semaphore.withPermit {  // 허가 획득 후 실행
            api.getPhoto(id)
        }
    }
}.awaitAll()
```

## 테스트 실행

```bash
./gradlew :app:testDebugUnitTest
```

## 기술 스택

- Kotlin
- Jetpack Compose
- Coroutines (Semaphore, async/await)
- OkHttp + Retrofit
- ViewModel

## 라이선스

MIT License
