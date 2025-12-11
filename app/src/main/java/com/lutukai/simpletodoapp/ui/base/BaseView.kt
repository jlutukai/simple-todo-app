package com.lutukai.simpletodoapp.ui.base

interface BaseView {
    fun showLoading()
    fun hideLoading()
    fun showError(message: String)
}