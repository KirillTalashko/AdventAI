package com.example.core.network.di

import com.example.core.network.BuildConfig
import com.example.core.network.api.DeepSeekApi
import com.example.core.network.api.OpenRouterApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val DEEP_SEEK_BASE_URL = "https://api.deepseek.com/"
    private const val OPEN_ROUTER_BASE_URL = "https://openrouter.ai/api/v1/"
    private const val OPEN_ROUTER_REFERER = "https://github.com/KirillTalashko/AdventAI"
    private const val OPEN_ROUTER_TITLE = "AdventAI"

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    @Named("DeepSeekOkHttpClient")
    fun provideDeepSeekOkHttpClient(): OkHttpClient =
        createOkHttpClient(
            apiKey = BuildConfig.DEEPSEEK_API_KEY,
            extraHeaders = emptyMap()
        )

    @Provides
    @Singleton
    @Named("OpenRouterOkHttpClient")
    fun provideOpenRouterOkHttpClient(): OkHttpClient =
        createOkHttpClient(
            apiKey = BuildConfig.OPENROUTER_API_KEY,
            extraHeaders = mapOf(
                "HTTP-Referer" to OPEN_ROUTER_REFERER,
                "X-OpenRouter-Title" to OPEN_ROUTER_TITLE
            )
        )

    private fun createOkHttpClient(
        apiKey: String,
        extraHeaders: Map<String, String>
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")

                extraHeaders.forEach { (name, value) ->
                    requestBuilder.addHeader(name, value)
                }

                if (apiKey.isNotBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer $apiKey")
                }

                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("DeepSeekRetrofit")
    fun provideDeepSeekRetrofit(
        @Named("DeepSeekOkHttpClient") okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit = Retrofit.Builder()
        .baseUrl(DEEP_SEEK_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    @Singleton
    @Named("OpenRouterRetrofit")
    fun provideOpenRouterRetrofit(
        @Named("OpenRouterOkHttpClient") okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit = Retrofit.Builder()
        .baseUrl(OPEN_ROUTER_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    @Singleton
    fun provideDeepSeekApi(
        @Named("DeepSeekRetrofit") retrofit: Retrofit
    ): DeepSeekApi =
        retrofit.create(DeepSeekApi::class.java)

    @Provides
    @Singleton
    fun provideOpenRouterApi(
        @Named("OpenRouterRetrofit") retrofit: Retrofit
    ): OpenRouterApi =
        retrofit.create(OpenRouterApi::class.java)
}
