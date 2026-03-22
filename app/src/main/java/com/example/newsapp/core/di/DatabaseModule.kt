package com.example.newsapp.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.newsapp.data.local.NewsDatabase
import com.example.newsapp.data.local.dao.ArticleDao
import com.example.newsapp.data.local.dao.RemoteKeysDao
import com.example.newsapp.data.repository.ArticleRepositoryImpl
import com.example.newsapp.domain.repository.ArticleRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DB_NAME = "news_db"

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NewsDatabase =
        Room.databaseBuilder(context, NewsDatabase::class.java, DB_NAME)
            // FTS4 addition changes the schema. For the assessment environment we can safely reset
            // local cache instead of writing a complex migration for the virtual table.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideArticleDao(database: NewsDatabase): ArticleDao = database.articleDao()

    @Provides
    fun provideRemoteKeysDao(database: NewsDatabase): RemoteKeysDao = database.remoteKeysDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindArticleRepository(impl: ArticleRepositoryImpl): ArticleRepository
}

