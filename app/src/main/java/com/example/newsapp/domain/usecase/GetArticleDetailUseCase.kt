package com.example.newsapp.domain.usecase

import com.example.newsapp.domain.common.Result
import com.example.newsapp.domain.model.Article
import com.example.newsapp.domain.repository.ArticleRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class GetArticleDetailUseCase @Inject constructor(
    private val repository: ArticleRepository
) {
    operator fun invoke(id: Long): Flow<Result<Article>> =
        flow {
            emit(Result.Loading)
            try {
                repository.getArticle(id).collect { article ->
                    if (article != null) {
                        emit(Result.Success(article))
                    }
                }
            } catch (t: Throwable) {
                emit(Result.Error(message = t.message ?: "Unexpected error", cause = t))
            }
        }
}

