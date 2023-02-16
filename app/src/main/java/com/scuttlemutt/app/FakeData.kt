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

package com.scuttlemutt.app

import crypto.Crypto
import types.DawgIdentifier
import java.util.*

val MyKey = Crypto.DUMMY_SECRETKEY;

val AKey = Crypto.DUMMY_SECRETKEY
val ADawgTag = DawgIdentifier(
    "ADawg",
    UUID.fromString("26f4dfb5-ac74-4832-90ef-05daf947aa87")
)

val BKey = Crypto.DUMMY_SECRETKEY
val BDawgTag = DawgIdentifier(
    "BDawg",
    UUID.fromString("fa47fef0-a646-4959-8f36-efdf94a4f295")
)

val CKey = Crypto.DUMMY_SECRETKEY
val CDawgTag = DawgIdentifier(
    "CDawg",
    UUID.fromString("9d2166e8-da9b-48f8-b3fe-34e0162bf79a")
)