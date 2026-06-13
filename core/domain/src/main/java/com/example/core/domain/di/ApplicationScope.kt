package com.example.core.domain.di

import javax.inject.Qualifier

/**
 * Квалификатор app-scoped корутин-скоупа: переживает жизненный цикл ViewModel.
 * Нужен для очистки session-данных в `ViewModel.onCleared()`, где `viewModelScope` уже отменён.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
