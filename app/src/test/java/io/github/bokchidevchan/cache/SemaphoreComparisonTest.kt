package io.github.bokchidevchan.cache

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

/**
 * 세마포어 유무에 따른 성능 비교 테스트
 *
 * 각 요청당 500ms 딜레이를 시뮬레이션하고,
 * 세마포어 없이 vs 세마포어 있을 때의 차이를 보여줍니다.
 */
class SemaphoreComparisonTest {

    companion object {
        const val DELAY_PER_REQUEST_MS = 500L  // 각 요청당 딜레이
        const val REQUEST_COUNT = 10           // 총 요청 수
        const val SEMAPHORE_PERMITS = 3        // 세마포어 동시 허용 수
    }

    /**
     * 느린 API 호출 시뮬레이션
     */
    private suspend fun simulateSlowApiCall(id: Int, concurrent: AtomicInteger): String {
        val current = concurrent.incrementAndGet()
        try {
            delay(DELAY_PER_REQUEST_MS)
            return "Response $id"
        } finally {
            concurrent.decrementAndGet()
        }
    }

    @Test
    fun `세마포어 없이 동시 요청`() = runBlocking {
        println("=" .repeat(60))
        println("테스트 1: 세마포어 없이 ${REQUEST_COUNT}개 동시 요청")
        println("=" .repeat(60))

        val concurrent = AtomicInteger(0)
        var maxConcurrent = 0

        val startTime = System.currentTimeMillis()

        // 모든 요청을 동시에 실행 (제한 없음)
        val results = (1..REQUEST_COUNT).map { id ->
            async(Dispatchers.Default) {
                val current = concurrent.incrementAndGet()
                synchronized(this) {
                    if (current > maxConcurrent) maxConcurrent = current
                }
                println("  [요청 $id] 시작 - 현재 동시 요청: $current")

                try {
                    delay(DELAY_PER_REQUEST_MS)
                    "Response $id"
                } finally {
                    concurrent.decrementAndGet()
                }
            }
        }.awaitAll()

        val elapsed = System.currentTimeMillis() - startTime

        println("-".repeat(60))
        println("결과:")
        println("  - 총 소요 시간: ${elapsed}ms")
        println("  - 최대 동시 요청 수: ${maxConcurrent}개")
        println("  - 완료된 요청: ${results.size}개")
        println()
        println("문제점:")
        println("  - ${REQUEST_COUNT}개 요청이 한번에 서버로 전송됨")
        println("  - 서버에 과부하 발생 가능")
        println("  - 메모리 사용량 급증")
        println("=" .repeat(60))
        println()
    }

    @Test
    fun `세마포어로 동시 요청 제한`() = runBlocking {
        println("=" .repeat(60))
        println("테스트 2: 세마포어로 동시 ${SEMAPHORE_PERMITS}개 제한")
        println("=" .repeat(60))

        val semaphore = Semaphore(SEMAPHORE_PERMITS)
        val concurrent = AtomicInteger(0)
        var maxConcurrent = 0

        val startTime = System.currentTimeMillis()

        // 세마포어로 동시 실행 수 제한
        val results = (1..REQUEST_COUNT).map { id ->
            async(Dispatchers.Default) {
                semaphore.withPermit {
                    val current = concurrent.incrementAndGet()
                    synchronized(this) {
                        if (current > maxConcurrent) maxConcurrent = current
                    }
                    println("  [요청 $id] 시작 - 현재 동시 요청: $current")

                    try {
                        delay(DELAY_PER_REQUEST_MS)
                        "Response $id"
                    } finally {
                        concurrent.decrementAndGet()
                    }
                }
            }
        }.awaitAll()

        val elapsed = System.currentTimeMillis() - startTime

        println("-".repeat(60))
        println("결과:")
        println("  - 총 소요 시간: ${elapsed}ms")
        println("  - 최대 동시 요청 수: ${maxConcurrent}개 (제한: ${SEMAPHORE_PERMITS})")
        println("  - 완료된 요청: ${results.size}개")
        println()
        println("장점:")
        println("  - 동시 요청이 ${SEMAPHORE_PERMITS}개로 제한됨")
        println("  - 서버 부하 감소")
        println("  - 안정적인 처리")
        println("=" .repeat(60))
        println()
    }

    @Test
    fun `비교 요약`() = runBlocking {
        println()
        println("*".repeat(60))
        println("         세마포어 유무 비교 요약")
        println("*".repeat(60))
        println()

        // 세마포어 없이
        val concurrent1 = AtomicInteger(0)
        var max1 = 0
        val start1 = System.currentTimeMillis()

        (1..REQUEST_COUNT).map { id ->
            async(Dispatchers.Default) {
                val c = concurrent1.incrementAndGet()
                synchronized(this) { if (c > max1) max1 = c }
                delay(DELAY_PER_REQUEST_MS)
                concurrent1.decrementAndGet()
            }
        }.awaitAll()

        val time1 = System.currentTimeMillis() - start1

        // 세마포어 있음
        val semaphore = Semaphore(SEMAPHORE_PERMITS)
        val concurrent2 = AtomicInteger(0)
        var max2 = 0
        val start2 = System.currentTimeMillis()

        (1..REQUEST_COUNT).map { id ->
            async(Dispatchers.Default) {
                semaphore.withPermit {
                    val c = concurrent2.incrementAndGet()
                    synchronized(this) { if (c > max2) max2 = c }
                    delay(DELAY_PER_REQUEST_MS)
                    concurrent2.decrementAndGet()
                }
            }
        }.awaitAll()

        val time2 = System.currentTimeMillis() - start2

        println("┌─────────────────┬───────────────┬───────────────┐")
        println("│                 │ 세마포어 없음  │ 세마포어 있음  │")
        println("├─────────────────┼───────────────┼───────────────┤")
        println("│ 총 소요 시간     │ %11dms │ %11dms │".format(time1, time2))
        println("│ 최대 동시 요청   │ %11d개 │ %11d개 │".format(max1, max2))
        println("│ 서버 부하       │      높음      │      낮음      │")
        println("└─────────────────┴───────────────┴───────────────┘")
        println()
        println("분석:")
        println("  - 세마포어 없음: ${time1}ms (${max1}개 동시)")
        println("  - 세마포어 있음: ${time2}ms (최대 ${max2}개 동시)")
        println()
        println("  세마포어가 있으면 시간은 더 걸리지만,")
        println("  서버 부하를 줄이고 안정적으로 처리합니다.")
        println()
        println("  이론적 시간:")
        println("  - 세마포어 없음: ~${DELAY_PER_REQUEST_MS}ms (모두 동시)")
        println("  - 세마포어 있음: ~${(REQUEST_COUNT / SEMAPHORE_PERMITS) * DELAY_PER_REQUEST_MS}ms (${SEMAPHORE_PERMITS}개씩 순차)")
        println("*".repeat(60))
    }
}
