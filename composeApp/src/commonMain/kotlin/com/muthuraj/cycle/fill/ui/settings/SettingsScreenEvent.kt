/* $Id$ */
package com.muthuraj.cycle.fill.ui.settings

import com.muthuraj.cycle.fill.util.ViewEvent

/**
 * Created by Muthuraj on 11/01/25.
 */
sealed interface SettingsScreenEvent : ViewEvent {
    data class OnThemeChanged(val theme: Theme) : SettingsScreenEvent
}