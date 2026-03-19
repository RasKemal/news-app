package com.example.newsapp.domain.usecase

import com.example.newsapp.domain.model.UserPreferences
import com.example.newsapp.domain.repository.UserPreferencesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveUserPreferencesUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<UserPreferences> =
        repository.observePreferences()
}

