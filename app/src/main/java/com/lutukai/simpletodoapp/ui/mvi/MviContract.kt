package com.lutukai.simpletodoapp.ui.mvi

/**
 * Marker interface for UI State - represents the current state of the screen
 */
interface UiState

/**
 * Marker interface for User Intent/Action - represents user interactions
 */
interface UiIntent

/**
 * Marker interface for Side Effects - one-time events like navigation, snackbar, etc.
 */
interface SideEffect
