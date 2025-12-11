package com.lutukai.simpletodoapp.ui.base

interface BasePresenter <V : BaseView> {
    fun attach(view: V)
    fun detach()
}