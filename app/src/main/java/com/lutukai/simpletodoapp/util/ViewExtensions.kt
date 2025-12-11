package com.lutukai.simpletodoapp.util

import android.view.View
import android.view.ViewStub
import androidx.core.view.isVisible
import com.lutukai.simpletodoapp.databinding.LayoutEmptyStateBinding

fun ViewStub.setEmptyState(dataIsEmpty: Boolean, title: String, desc: String) {
    if (this.parent != null) {
        if (dataIsEmpty) {
            this.inflate().apply {
                val viewStubBinding = LayoutEmptyStateBinding.bind(this)
                viewStubBinding.txtEmptyTitle.text = title
                viewStubBinding.txtEmptyDesc.text = desc
                visible()
            }
        } else {
            gone()
        }
    } else {
        this.isVisible = dataIsEmpty
    }
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}