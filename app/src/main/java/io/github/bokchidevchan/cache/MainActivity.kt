package io.github.bokchidevchan.cache

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.bokchidevchan.cache.ui.theme.CacheTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CacheTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NoSemaphoreScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun NoSemaphoreScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    val currentRequests by viewModel.currentRequests.collectAsState()
    val maxRequests by viewModel.maxRequests.collectAsState()
    val completedRequests by viewModel.completedRequests.collectAsState()
    val totalRequests by viewModel.totalRequests.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val elapsedTime by viewModel.elapsedTime.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 헤더
        Header()

        Spacer(modifier = Modifier.height(16.dp))

        // 핵심 지표: 현재 동시 요청 수 (큰 숫자로 강조)
        ConcurrentRequestsCard(
            currentRequests = currentRequests,
            maxRequests = maxRequests,
            isLoading = isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 진행 상황
        if (totalRequests > 0) {
            ProgressSection(
                completed = completedRequests,
                total = totalRequests,
                elapsedTime = elapsedTime
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 테스트 버튼들
        TestButtons(
            isLoading = isLoading,
            onTest5 = { viewModel.loadWithoutSemaphore() },
            onTest10 = { viewModel.loadManyWithoutSemaphore() },
            onReset = { viewModel.reset() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 로그
        Text(
            text = "실행 로그",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(4.dp))

        LogSection(logs = logs, modifier = Modifier.weight(1f))
    }
}

@Composable
fun Header() {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFD32F2F), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "세마포어 없음",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "비효율적 예제",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "동시 요청 수 제한 없이 모든 요청이 한번에 실행됩니다",
            fontSize = 13.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun ConcurrentRequestsCard(
    currentRequests: Int,
    maxRequests: Int,
    isLoading: Boolean
) {
    // 동시 요청 수에 따라 색상 변경 (많을수록 빨간색)
    val backgroundColor by animateColorAsState(
        targetValue = when {
            currentRequests >= 8 -> Color(0xFFD32F2F)
            currentRequests >= 5 -> Color(0xFFFF5722)
            currentRequests >= 3 -> Color(0xFFFF9800)
            currentRequests >= 1 -> Color(0xFFFFC107)
            else -> Color(0xFFE0E0E0)
        },
        animationSpec = tween(300),
        label = "bg_color"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "현재 동시 요청 수",
                color = Color.White,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 큰 숫자
            Text(
                text = "$currentRequests",
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (maxRequests > 0) {
                Text(
                    text = "최대: ${maxRequests}개 동시 실행됨",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }

            if (currentRequests >= 5) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "서버에 부하가 걸립니다!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ProgressSection(
    completed: Int,
    total: Int,
    elapsedTime: Long
) {
    val progress by animateFloatAsState(
        targetValue = if (total > 0) completed.toFloat() / total else 0f,
        animationSpec = tween(300),
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "진행: $completed / $total", fontSize = 14.sp)
                if (elapsedTime > 0) {
                    Text(
                        text = "${elapsedTime}ms",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFFE0E0E0)
            )
        }
    }
}

@Composable
fun TestButtons(
    isLoading: Boolean,
    onTest5: () -> Unit,
    onTest10: () -> Unit,
    onReset: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onTest5,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("5개 요청 동시 실행", fontWeight = FontWeight.Bold)
                Text(
                    text = "같은 이미지 5번 요청 (각 1.5초 딜레이)",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Button(
            onClick = onTest10,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("10개 요청 동시 실행", fontWeight = FontWeight.Bold)
                Text(
                    text = "서버 폭격! 제한 없이 전부 한번에",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("초기화")
        }
    }
}

@Composable
fun LogSection(logs: List<LogItem>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()

    // 새 로그가 추가되면 자동 스크롤
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
    ) {
        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "버튼을 눌러서 테스트해보세요\n\n동시 요청 수가 얼마나 높아지는지\n확인할 수 있습니다",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(logs) { log ->
                    LogItemRow(log = log)
                }
            }
        }
    }
}

@Composable
fun LogItemRow(log: LogItem) {
    val (bgColor, textColor, dotColor) = when (log.type) {
        LogType.ERROR -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), Color(0xFFE53935))
        LogType.WARNING -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), Color(0xFFFF9800))
        LogType.SUCCESS -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), Color(0xFF4CAF50))
        LogType.REQUEST -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), Color(0xFF2196F3))
        LogType.INFO -> Triple(Color.White, Color(0xFF616161), Color(0xFFBDBDBD))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = log.message,
            fontSize = 13.sp,
            color = textColor
        )
    }
}
