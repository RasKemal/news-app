package com.example.newsapp.di

import android.content.Context
import com.example.newsapp.data.remote.ApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.spaceflightnewsapi.net/v4/"

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        // 50 MB HTTP disk cache shared by API + image requests
        val cacheSizeBytes = 50L * 1024L * 1024L
        val cache = Cache(directory = context.cacheDir.resolve("http_cache"), maxSize = cacheSizeBytes)

        // For image responses without explicit caching, add a reasonable Cache-Control header.
        val imageCachingInterceptor = Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)

            val contentType = response.header("Content-Type") ?: ""
            val hasCachingHeaders = response.headers.names().any { name ->
                name.equals("Cache-Control", ignoreCase = true) ||
                    name.equals("Expires", ignoreCase = true) ||
                    name.equals("Pragma", ignoreCase = true)
            }

            if (!hasCachingHeaders && contentType.startsWith("image/")) {
                response.newBuilder()
                    .header("Cache-Control", "public, max-age=86400") // cache images for 1 day
                    .build()
            } else {
                response
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .cache(cache)
            .addNetworkInterceptor(imageCachingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}

