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

package co.mercenary.creators.kotlin.minio.test.aws3

import co.mercenary.creators.kotlin.minio.*
import org.junit.jupiter.api.Test

class BucketsTest : KotlinTest(AWS3_TEST_PROPERTIES) {
    @Test
    fun test() {
        minio.buckets().forEachIndexed { many, each ->
            info { "%2d : %s".format(many + 1, each.toJSONString(false)) }
        }
        info { minio }
        minio.buckets { it.startsWith("dean") }.forEachIndexed { many, each ->
            info { "%2d : %s".format(many + 1, each.toJSONString(false)) }
        }
    }
}