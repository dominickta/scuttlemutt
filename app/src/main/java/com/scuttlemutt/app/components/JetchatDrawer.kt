/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.scuttlemutt.app.components

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.scuttlemutt.app.R
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@Composable
fun JetchatDrawerContent(
    viewModel: com.scuttlemutt.app.MainViewModel,
    activeChannel: String,
    onConnectionsClicked: () -> Unit,
    onProfileClicked: (String) -> Unit,
    onChatClicked: (String) -> Unit
) {
    // Use windowInsetsTopHeight() to add a spacer which pushes the drawer content
    // below the status bar (y-axis)
    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        DrawerHeader()
        DividerItem()
        DrawerItemHeader("Chats", onConnectionsClicked)
        chatItems(viewModel, onChatClicked)
    }
}


@Composable
private fun chatItems(viewModel: com.scuttlemutt.app.MainViewModel, onChatClicked: (String) -> Unit) {
    val channelNames = viewModel.allContactNames.observeAsState().value!!
    val currChan = viewModel.activeContact.observeAsState().value!!
    for ((channel, numNewMessages) in channelNames) {
        ChatItem(channel, channel == currChan, numNewMessages) { onChatClicked(channel) }
    }
}

@Composable
private fun DrawerHeader() {
    Row(modifier = Modifier.padding(16.dp), verticalAlignment = CenterVertically) {
        JetchatIcon(
            contentDescription = null,
            modifier = Modifier.size(50.dp)
        )
        Text(
            text = "Scuttlemutt",
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            modifier = Modifier.padding(horizontal = 5.dp)
        )
    }
}
@Composable
private fun DrawerItemHeader(text: String, onConnectionsClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .heightIn(min = 52.dp)
            .padding(horizontal = 28.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(CenterVertically)
        )
        Spacer(Modifier.weight(1f))
        Icon(
            imageVector = Icons.Outlined.Lan,
            modifier = Modifier
                .clickable(onClick = onConnectionsClicked)
                .padding(horizontal = 12.dp, vertical = 16.dp)
                .height(24.dp),
            contentDescription = stringResource(id = R.string.info)
        )
    }
}

@Composable
private fun ChatItem(text: String, selected: Boolean, numNewMessages: Int, onChatClicked: () -> Unit) {
    val background = if (selected) {
        Modifier.background(MaterialTheme.colorScheme.primaryContainer)
    } else {
        Modifier
    }

    Log.d("JetchatDrawer", "composing: $numNewMessages")
    Row(
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(CircleShape)
            .then(background)
            .clickable(onClick = onChatClicked),
        verticalAlignment = CenterVertically
    ) {
        // this displays a default chat item
        val iconTint = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_jetchat),
            tint = iconTint,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
            contentDescription = null
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.padding(start = 12.dp).weight(1f)
        )
        Spacer(modifier = Modifier.weight(.05f))
        Box(
            modifier = Modifier
                .padding(horizontal = 25.dp)
                .size(25.dp)
                .clip(CircleShape)
                .background(
                    if (numNewMessages > 0) MaterialTheme.colorScheme.primary
                    else if (selected) MaterialTheme.colorScheme.primaryContainer
                    else Color.White )
                .align(CenterVertically)
        ) {
            if (numNewMessages > 0) {
                Text(
                    text = "$numNewMessages",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun ProfileItem(text: String, @DrawableRes profilePic: Int?, onProfileClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(CircleShape)
            .clickable(onClick = onProfileClicked),
        verticalAlignment = CenterVertically
    ) {
        val paddingSizeModifier = Modifier
            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
            .size(24.dp)
        if (profilePic != null) {
            Image(
                painter = painterResource(id = profilePic),
                modifier = paddingSizeModifier.then(Modifier.clip(CircleShape)),
                contentScale = ContentScale.Crop,
                contentDescription = null
            )
        } else {
            Spacer(modifier = paddingSizeModifier)
        }
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
fun DividerItem(modifier: Modifier = Modifier) {
    Divider(
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    )
}

@Preview
@Composable
fun ChatItemPreview() {
    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        DrawerHeader()
        DividerItem()
        DrawerItemHeader("Chats", {})
        ChatItem("DDawgDDawgDDawgDDawgDDawgDDawgDDawgDDawgDDawg", false, 0, {})

        ChatItem("DDawgDDawgDDawgDDawgDDawgDDawgDDawgDDawgDDawg", false, 5, {})
        ChatItem("DDawgDDawgDDawgDDawgDDawgDDawgDDawgDDawgDDawg", true, 0, {})
        ChatItem("DDawgDDawgDDawgDDawgDDawgDDawgDDawgDDawgDDawg", true, 5, {})
        ChatItem("DDawgDDawgDDawgDDawgDDawgDDawgDDawgDDawgDDawg", false, 3, {})
    }
}