/* $Id$ */
package com.muthuraj.cycle.fill.util.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Created by Muthuraj on 10/12/24.
 */
@Composable
fun DaysElapsedChip(daysElapsed: Pair<Int, String>, suffix: String) {
    val bgColor = when {
        daysElapsed.first > 90 -> MaterialTheme.colors.error.copy(alpha = 0.1f)
        daysElapsed.first > 60 -> MaterialTheme.colors.secondary.copy(
            alpha = 0.1f
        )

        else -> MaterialTheme.colors.primary.copy(alpha = 0.1f)
    }
    val text = when (daysElapsed.first) {
        1 -> {
            "Yesterday"
        }

        0 -> {
            "Today"
        }

        else -> {
            "${daysElapsed.second} $suffix"
        }
    }
    Text(
        modifier = Modifier.border(width = 1.dp, shape = RoundedCornerShape(16.dp), color = bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        text = text,
        style = MaterialTheme.typography.caption.copy(fontSize = 10.sp, lineHeight = 10.sp),
        color = when {
            daysElapsed.first > 90 -> MaterialTheme.colors.error
            daysElapsed.first > 60 -> MaterialTheme.colors.secondary
            else -> MaterialTheme.colors.primary
        }
    )
}