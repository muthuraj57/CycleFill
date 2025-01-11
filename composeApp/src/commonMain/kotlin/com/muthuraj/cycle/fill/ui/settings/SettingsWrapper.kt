/* $Id$ */
package com.muthuraj.cycle.fill.ui.settings

import com.muthuraj.cycle.fill.di.AppScope
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.tatarka.inject.annotations.Inject

/**
 * Created by Muthuraj on 11/01/25.
 */
@AppScope
@Inject
class SettingsWrapper {
    private val settings = Settings()

    private val themeFlow = MutableStateFlow(getTheme())
    val themeStateFlow: StateFlow<Theme> = themeFlow

    fun getTheme(): Theme {
        return when(settings[THEME_KEY, "system"]){
            "dark" -> Theme.Dark
            "light" -> Theme.Light
            else -> Theme.System
        }
    }

    fun setTheme(theme: Theme){
        settings[THEME_KEY] = when(theme){
            Theme.Dark -> "dark"
            Theme.Light -> "light"
            Theme.System -> "system"
        }
        themeFlow.value = theme
    }

    companion object{
        private const val THEME_KEY = "theme"
    }
}