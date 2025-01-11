/* $Id$ */
package com.muthuraj.cycle.fill.util.compose

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.muthuraj.cycle.fill.network.NetworkManager
import cyclefill.composeapp.generated.resources.Res
import cyclefill.composeapp.generated.resources.local
import cyclefill.composeapp.generated.resources.tailscale
import org.jetbrains.compose.resources.painterResource

/**
 * Created by Muthuraj on 11/01/25.
 */
@Composable
fun NetworkSwitchIcon() {
    var isTailScaleSelected by remember { mutableStateOf(NetworkManager.useTailScaleUrl) }
    val networkSwitchIcon = if (isTailScaleSelected) {
        Res.drawable.local
    } else {
        Res.drawable.tailscale
    }
    IconButton(onClick = {
        NetworkManager.useTailScaleUrl = !NetworkManager.useTailScaleUrl
        isTailScaleSelected = !isTailScaleSelected
    }) {
        Icon(
            painter = painterResource(networkSwitchIcon),
            contentDescription = "Switch",
        )
    }
}