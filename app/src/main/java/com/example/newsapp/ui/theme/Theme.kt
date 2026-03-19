package com.example.newsapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.newsapp.ui.theme.SpotifyDarkBackground
import com.example.newsapp.ui.theme.SpotifyDarkSurface
import com.example.newsapp.ui.theme.SpotifyGreen
import com.example.newsapp.ui.theme.SpotifyOnDark

private val DarkColorScheme = darkColorScheme(
    primary = SpotifyGreen,
    secondary = SpotifyGreen.copy(alpha = 0.8f),
    tertiary = SpotifyGreen.copy(alpha = 0.6f),
    background = SpotifyDarkBackground,
    surface = SpotifyDarkSurface,
    onBackground = SpotifyOnDark,
    onSurface = SpotifyOnDark
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun NewsAppTheme(
    darkTheme: Boolean = true,
    // Keep stable palette (Spotify-like) instead of dynamic colors.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}