package io.github.bokchidevchan.cache

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.bokchidevchan.cache.data.PhotoResponse
import io.github.bokchidevchan.cache.data.SlowApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

/**
 * 세마포어 없이 동작하는 ViewModel
 * 모든 요청이 동시에 실행되어 비효율적
 */
class MainViewModel : ViewModel() {

    // 현재 동시에 진행 중인 요청 수
    private val _currentRequests = MutableStateFlow(0)
    val currentRequests: StateFlow<Int> = _currentRequests.asStateFlow()

    // 최대 동시 요청 수 (기록)
    private val _maxRequests = MutableStateFlow(0)
    val maxRequests: StateFlow<Int> = _maxRequests.asStateFlow()

    // 완료된 요청 수
    private val _completedRequests = MutableStateFlow(0)
    val completedRequests: StateFlow<Int> = _completedRequests.asStateFlow()

    // 총 요청 수
    private val _totalRequests = MutableStateFlow(0)
    val totalRequests: StateFlow<Int> = _totalRequests.asStateFlow()

    // 로그 메시지들
    private val _logs = MutableStateFlow<List<LogItem>>(emptyList())
    val logs: StateFlow<List<LogItem>> = _logs.asStateFlow()

    // 로딩 중 여부
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 경과 시간 (ms)
    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private val currentCount = AtomicInteger(0)
    private var maxCount = 0
    private var loadJob: Job? = null

    /**
     * 세마포어 없이 5개 요청 동시 실행
     * 같은 이미지를 5번 요청 → 캐시 없으면 5번 다 네트워크 사용
     */
    fun loadWithoutSemaphore() {
        if (_isLoading.value) return

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            reset()
            _isLoading.value = true

            val requestCount = 5
            _totalRequests.value = requestCount

            addLog("5개 요청 동시 시작! (세마포어 없음)", LogType.INFO)
            addLog("모든 요청이 한번에 서버로 나갑니다...", LogType.WARNING)

            val startTime = System.currentTimeMillis()
            val jobs = mutableListOf<Job>()

            // 5개 요청을 동시에 실행
            repeat(requestCount) { index ->
                val job = launch {
                    val requestNum = index + 1
                    val current = currentCount.incrementAndGet()
                    _currentRequests.value = current
                    if (current > maxCount) {
                        maxCount = current
                        _maxRequests.value = maxCount
                    }

                    addLog("[$requestNum] 요청 시작 (동시: $current)", LogType.REQUEST)

                    try {
                        // 느린 API 호출 (1.5초 딜레이)
                        val photo = SlowApiClient.api.getPhoto(1) // 같은 ID=1

                        _completedRequests.value++
                        addLog("[$requestNum] 완료! (ID: ${photo.id})", LogType.SUCCESS)
                    } catch (e: Exception) {
                        addLog("[$requestNum] 실패: ${e.message}", LogType.ERROR)
                    } finally {
                        currentCount.decrementAndGet()
                        _currentRequests.value = currentCount.get()
                    }
                }
                jobs.add(job)
            }

            // 모든 요청 완료 대기
            jobs.forEach { it.join() }

            val elapsed = System.currentTimeMillis() - startTime
            _elapsedTime.value = elapsed
            _isLoading.value = false

            addLog("━━━━━━━━━━━━━━━━━━", LogType.INFO)
            addLog("총 소요시간: ${elapsed}ms", LogType.INFO)
            addLog("최대 동시 요청: ${maxCount}개", LogType.WARNING)
            addLog("", LogType.INFO)
            addLog("문제점:", LogType.ERROR)
            addLog("- 같은 데이터를 5번 다운로드!", LogType.ERROR)
            addLog("- 서버에 5개 요청이 한번에!", LogType.ERROR)
        }
    }

    /**
     * 10개 요청 동시 실행 (더 극단적인 예시)
     */
    fun loadManyWithoutSemaphore() {
        if (_isLoading.value) return

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            reset()
            _isLoading.value = true

            val requestCount = 10
            _totalRequests.value = requestCount

            addLog("10개 요청 동시 시작! (세마포어 없음)", LogType.INFO)
            addLog("서버가 힘들어합니다...", LogType.WARNING)

            val startTime = System.currentTimeMillis()
            val jobs = mutableListOf<Job>()

            repeat(requestCount) { index ->
                val job = launch {
                    val requestNum = index + 1
                    val current = currentCount.incrementAndGet()
                    _currentRequests.value = current
                    if (current > maxCount) {
                        maxCount = current
                        _maxRequests.value = maxCount
                    }

                    addLog("[$requestNum] 시작 (동시: $current)", LogType.REQUEST)

                    try {
                        val photo = SlowApiClient.api.getPhoto(requestNum % 3 + 1) // ID 1~3 반복

                        _completedRequests.value++
                        addLog("[$requestNum] 완료", LogType.SUCCESS)
                    } catch (e: Exception) {
                        addLog("[$requestNum] 실패", LogType.ERROR)
                    } finally {
                        currentCount.decrementAndGet()
                        _currentRequests.value = currentCount.get()
                    }
                }
                jobs.add(job)
            }

            jobs.forEach { it.join() }

            val elapsed = System.currentTimeMillis() - startTime
            _elapsedTime.value = elapsed
            _isLoading.value = false

            addLog("━━━━━━━━━━━━━━━━━━", LogType.INFO)
            addLog("총 소요시간: ${elapsed}ms", LogType.INFO)
            addLog("최대 동시 요청: ${maxCount}개", LogType.WARNING)
            addLog("", LogType.INFO)
            addLog("세마포어가 있었다면?", LogType.SUCCESS)
            addLog("→ 동시 3개로 제한, 안정적 처리", LogType.SUCCESS)
        }
    }

    fun reset() {
        loadJob?.cancel()
        currentCount.set(0)
        maxCount = 0
        _currentRequests.value = 0
        _maxRequests.value = 0
        _completedRequests.value = 0
        _totalRequests.value = 0
        _elapsedTime.value = 0
        _logs.value = emptyList()
        _isLoading.value = false
    }

    private fun addLog(message: String, type: LogType) {
        _logs.value = _logs.value + LogItem(message, type)
    }
}

data class LogItem(
    val message: String,
    val type: LogType
)

enum class LogType {
    INFO, WARNING, ERROR, SUCCESS, REQUEST
}
