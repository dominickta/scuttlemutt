/*
 * Copyright 2021 The Android Open Source Project
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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.scuttlemutt.app.R

@Composable
fun JetchatIcon(
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val semantics = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else {
        Modifier
    }
    Box(modifier = modifier.then(semantics)) {
        Image(
            painter = painterResource(id = R.drawable.husky),
            modifier = Modifier.size(200.dp),
            contentScale = ContentScale.Fit,
            contentDescription = "Scuttlemutt Logo",
        )
//        Icon(
//            painter = painterResource(id = R.drawable.husky),
////            painter = painterResource(id = R.drawable.ic_jetchat_back),
//            contentDescription = null,
////            tint = MaterialTheme.colorScheme.primaryContainer
//        )
//        Icon(
//            painter = painterResource(id = R.drawable.husky),
////            painter = painterResource(id = R.drawable.ic_jetchat_front),
//            contentDescription = null,
////            tint = MaterialTheme.colorScheme.primary
//        )
    }
}
