package com.lutukai.simpletodoapp.ui.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * Multipreview annotation that represents various device sizes. Add this annotation to a composable
 * to render various devices.
 */
@Preview(name = "Phone", device = "spec:width=411dp,height=891dp", showBackground = true)
@Preview(name = "Phone - Landscape", device = "spec:width=891dp,height=411dp", showBackground = true)
@Preview(name = "Tablet", device = "spec:width=1280dp,height=800dp,dpi=240", showBackground = true)
@Preview(name = "Phone - Dark", device = "spec:width=411dp,height=891dp", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
annotation class DevicePreviews
