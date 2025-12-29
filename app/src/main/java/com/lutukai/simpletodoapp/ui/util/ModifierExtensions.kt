package com.lutukai.simpletodoapp.ui.util

import androidx.compose.foundation.clickable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import java.util.concurrent.atomic.AtomicLong

fun Modifier.debouncedClickable(
    debounceTime: Long = 300L,
    onClick: () -> Unit
): Modifier = composed {
    val lastClickTime = remember { AtomicLong(0L) }
    clickable {
        val currentTime = System.currentTimeMillis()
        val lastTime = lastClickTime.get()
        if (currentTime - lastTime >= debounceTime) {
            if (lastClickTime.compareAndSet(lastTime, currentTime)) {
                onClick()
            }
        }
    }
}
