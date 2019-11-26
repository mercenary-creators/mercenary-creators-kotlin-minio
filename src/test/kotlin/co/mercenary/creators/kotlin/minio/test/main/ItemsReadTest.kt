/*
 * Copyright (c) 2019, Mercenary Creators Company. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.mercenary.creators.kotlin.minio.test.main

import co.mercenary.creators.kotlin.minio.*
import org.junit.jupiter.api.Test

class ItemsReadTest : KotlinTest() {
    @Test
    fun test() {
        var many = 0
        minio.items("root").forEach { each ->
            info { many }
            info { each }
            info { each.text() }
            many++
        }
        info { many }
        many.shouldNotBe(0) {
            "many should not be 0 but ($many)"
        }
    }
}