package com.example.floatingflavors.app.core.di

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object PaymentResultBus {
    private val _result = MutableStateFlow<String?>(null)
    val result = _result.asStateFlow()

    fun emit(value: String) {
        _result.value = value
    }

    fun clear() {
        _result.value = null
    }
}

