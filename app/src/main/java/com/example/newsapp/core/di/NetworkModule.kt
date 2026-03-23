package com.example.newsapp.core.di

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
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt module providing network-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.spaceflightnewsapi.net/v4/"

    internal const val API_OKHTTP_QUALIFIER = "api_okhttp"
    internal const val IMAGE_OKHTTP_QUALIFIER = "image_okhttp"

    @Provides
    @Singleton
    @Named(API_OKHTTP_QUALIFIER)
    fun provideApiOkHttpClient(
        @ApplicationContext context: Context
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        // HTTP disk cache for API requests only.
        // Images are cached by Coil's own disk/memory caches (see `ImageModule`).
        val cacheSizeBytes = 50L * 1024L * 1024L
        val cache = Cache(directory = context.cacheDir.resolve("http_cache"), maxSize = cacheSizeBytes)

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .cache(cache)
            .build()
    }

    @Provides
    @Singleton
    @Named(IMAGE_OKHTTP_QUALIFIER)
    fun provideImageOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        // No HTTP disk cache here: Coil's own disk/memory caches are the source of truth for images.
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideRetrofit(
        @Named(API_OKHTTP_QUALIFIER) okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit =
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

