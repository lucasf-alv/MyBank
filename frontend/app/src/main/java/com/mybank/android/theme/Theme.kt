package com.mybank.android.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MyBankColorScheme = darkColorScheme(
    primary = MyBankYellow,
    onPrimary = MyBankBlack,

    secondary = MyBankYellowDark,
    onSecondary = MyBankBlack,

    tertiary = MyBankYellowLight,
    onTertiary = MyBankBlack,

    background = MyBankBlack,
    onBackground = MyBankWhite,

    surface = MyBankSurface,
    onSurface = MyBankWhite,

    surfaceVariant = MyBankSurfaceVariant,
    onSurfaceVariant = MyBankMutedText,

    primaryContainer = MyBankPrimaryContainer,
    onPrimaryContainer = MyBankYellowLight,

    error = MyBankError,
    onError = MyBankBlack,

    outline = MyBankOutline
)

@Composable
fun MyBankTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = MyBankColorScheme,
        typography = Typography,
        content = content
    )
}
