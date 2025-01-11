/* $Id$ */
package com.muthuraj.cycle.fill.ui.settings

import androidx.lifecycle.viewModelScope
import com.muthuraj.cycle.fill.di.AppScope
import com.muthuraj.cycle.fill.util.BaseViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.tatarka.inject.annotations.Inject

/**
 * Created by Muthuraj on 11/01/25.
 */
@AppScope
@Inject
class SettingsScreenViewModel(private val settings: SettingsWrapper) :
    BaseViewModel<SettingsScreenEvent, SettingsScreenState>() {


    override fun setInitialState() = SettingsScreenState(theme = settings.getTheme())

    init {
        settings.themeStateFlow
            .onEach { theme ->
                setState { copy(theme = theme) }
            }.launchIn(viewModelScope)
    }

    override fun handleEvents(event: SettingsScreenEvent) {
        when (event) {
            is SettingsScreenEvent.OnThemeChanged -> {
                settings.setTheme(event.theme)
            }
        }
    }
}