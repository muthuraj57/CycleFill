/* $Id$ */
package com.muthuraj.cycle.fill.util.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muthuraj.cycle.fill.ui.dashboard.DashboardScreenEvent

/**
 * Created by Muthuraj on 10/12/24.
 */
@Composable
fun DaysElapsedChip(daysElapsed: Pair<Int, String>, text: String) {
    val bgColor = when {
        daysElapsed.first > 90 -> MaterialTheme.colors.error.copy(alpha = 0.1f)
        daysElapsed.first > 60 -> MaterialTheme.colors.secondary.copy(
            alpha = 0.1f
        )

        else -> MaterialTheme.colors.primary.copy(alpha = 0.1f)
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

@Composable
fun ErrorWithRetry(error: String, onRetryClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            Text(text = error, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.size(24.dp))
            Button(onClick = onRetryClick) {
                Text(text = "Retry")
            }
        }
    }
}