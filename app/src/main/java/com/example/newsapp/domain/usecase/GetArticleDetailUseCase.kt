package com.example.newsapp.domain.usecase

import com.example.newsapp.core.helpers.Result
import com.example.newsapp.domain.model.Article
import com.example.newsapp.domain.repository.ArticleRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class GetArticleDetailUseCase @Inject constructor(
    private val repository: ArticleRepository
) {
    operator fun invoke(id: Long): Flow<Result<Article>> =
        repository.getArticle(id)
            .map { article ->
                if (article != null) {
                    Result.Success(article)
                } else {
                    Result.Error("Article not found in database.")
                }
            }
            .onStart {
                emit(Result.Loading)
            }
            .catch { t: Throwable ->
                emit(Result.Error(message = t.message ?: "Unexpected error", cause = t))
            }
}
