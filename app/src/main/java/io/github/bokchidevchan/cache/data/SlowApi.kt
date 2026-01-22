package io.github.bokchidevchan.cache.data

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

/**
 * 의도적으로 느린 응답을 만드는 Interceptor
 * 실제 네트워크 지연을 시뮬레이션
 */
class DelayInterceptor(private val delayMs: Long) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        Thread.sleep(delayMs)
        return chain.proceed(chain.request())
    }
}

/**
 * JSONPlaceholder API
 */
interface PhotoApi {
    @GET("photos/{id}")
    suspend fun getPhoto(@Path("id") id: Int): PhotoResponse
}

data class PhotoResponse(
    val albumId: Int,
    val id: Int,
    val title: String,
    val url: String,
    val thumbnailUrl: String
)

/**
 * 느린 API 클라이언트 (1.5초 딜레이)
 */
object SlowApiClient {
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"
    private const val DELAY_MS = 1500L // 각 요청당 1.5초 딜레이

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(DelayInterceptor(DELAY_MS))
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: PhotoApi = retrofit.create(PhotoApi::class.java)
}
