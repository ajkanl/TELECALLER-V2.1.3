package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Responsive extension property for Integers to scale Text Units (sp)
 * dynamically based on screen width dimensions.
 */
val Int.rsp: TextUnit
    @Composable
    get() {
        val configuration = LocalConfiguration.current
        val screenWidthDp = configuration.screenWidthDp
        val baseScale = when {
            screenWidthDp >= 900 -> 1.28f  // Tablets & Desktops
            screenWidthDp >= 600 -> 1.14f  // Foldables & landscape phones
            screenWidthDp < 360 -> 0.88f   // Small/compact legacy screens
            else -> 1.0f                   // Default standard phones
        }
        return (this * baseScale).sp
    }

/**
 * Responsive extension property for Doubles to scale Text Units (sp)
 * dynamically based on screen width dimensions.
 */
val Double.rsp: TextUnit
    @Composable
    get() {
        val configuration = LocalConfiguration.current
        val screenWidthDp = configuration.screenWidthDp
        val baseScale = when {
            screenWidthDp >= 900 -> 1.28f
            screenWidthDp >= 600 -> 1.14f
            screenWidthDp < 360 -> 0.88f
            else -> 1.0f
        }
        return (this * baseScale).sp
    }

/**
 * Responsive extension property for Floats to scale Text Units (sp)
 * dynamically based on screen width dimensions.
 */
val Float.rsp: TextUnit
    @Composable
    get() {
        val configuration = LocalConfiguration.current
        val screenWidthDp = configuration.screenWidthDp
        val baseScale = when {
            screenWidthDp >= 900 -> 1.28f
            screenWidthDp >= 600 -> 1.14f
            screenWidthDp < 360 -> 0.88f
            else -> 1.0f
        }
        return (this * baseScale).sp
    }

/**
 * Responsive extension property for Integers to scale Layout Dimensions (dp)
 * dynamically based on screen width dimensions.
 */
val Int.rdp: Dp
    @Composable
    get() {
        val configuration = LocalConfiguration.current
        val screenWidthDp = configuration.screenWidthDp
        val baseScale = when {
            screenWidthDp >= 900 -> 1.22f
            screenWidthDp >= 600 -> 1.10f
            screenWidthDp < 360 -> 0.86f
            else -> 1.0f
        }
        return (this * baseScale).dp
    }

/**
 * Check if the active layout display area is considered wide (e.g., tablet/foldable width).
 */
@Composable
fun isWideScreen(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp >= 600
}

/**
 * Check if the active layout display area has expanded dimensions (large tablet/desktop).
 */
@Composable
fun isExpandedTablet(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp >= 900
}
