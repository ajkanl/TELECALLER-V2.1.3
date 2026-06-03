package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF3B82F6),
    secondary = Color(0xFF64748B),
    tertiary = Color(0xFF10B981),
    background = Color(0xFF090D16),     // Cyber black
    surface = Color(0xFF121B2D),        // Deep slate surface
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8)
)

private val LightColorScheme = darkColorScheme(
    primary = Color(0xFF3B82F6),
    secondary = Color(0xFF64748B),
    tertiary = Color(0xFF10B981),
    background = Color(0xFF090F1C),     // Premium midnight blue
    surface = Color(0xFF172033),        // Deep dark container
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1F293D),
    onSurfaceVariant = Color(0xFF94A3B8)
)

// Bento Slate Theme
private val BentoSlateColorScheme = darkColorScheme(
    primary = Color(0xFF3B82F6),        // Bright Blue
    secondary = Color(0xFF64748B),      // Slate Grey
    tertiary = Color(0xFF10B981),       // Emerald
    background = Color(0xFF0F172A),     // Slate Dark Background
    surface = Color(0xFF1E293B),        // Slate Surface
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC)
)

// Crimson Warning Theme
private val CrimsonWarningColorScheme = darkColorScheme(
    primary = Color(0xFFEF4444),        // Vivid Crimson
    secondary = Color(0xFF991B1B),      // Deep Red
    tertiary = Color(0xFFF59E0B),       // Orange Alert
    background = Color(0xFF110303),     // Pitch Red-Black Coffin
    surface = Color(0xFF1E0808),        // Deep Surfaced Card
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFFEE2E2),
    onSurface = Color(0xFFFEE2E2)
)

// Neon Emerald Theme
private val NeonEmeraldColorScheme = darkColorScheme(
    primary = Color(0xFF10B981),        // Neon Emerald
    secondary = Color(0xFF064E3B),      // Dark Forest
    tertiary = Color(0xFF34D399),       // Mint Blue
    background = Color(0xFF090D16),     // Electronic black
    surface = Color(0xFF121B2D),        // Cyber Slate Card
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color(0xFFE6FDF4),
    onSurface = Color(0xFFE6FDF4)
)

@Composable
fun MyApplicationTheme(
  themeMode: String = "system",
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme = when (themeMode) {
      "light" -> LightColorScheme
      "dark" -> DarkColorScheme
      "bento_slate" -> BentoSlateColorScheme
      "crimson_warning" -> CrimsonWarningColorScheme
      "neon_emerald" -> NeonEmeraldColorScheme
      else -> { // system
          if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
              val context = LocalContext.current
              dynamicDarkColorScheme(context)
          } else {
              DarkColorScheme
          }
      }
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
