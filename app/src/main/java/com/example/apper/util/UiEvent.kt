package com.example.apper.util

// SEALED:
// After compile time, can't be implemented anymore
sealed class UiEvent {
    object PopBackStack : UiEvent()
    data class Navigate(val route: String) : UiEvent()
    data class ShowSnackBar(val message: String, val action: String? = null) : UiEvent()
}