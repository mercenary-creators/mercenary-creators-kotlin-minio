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
import co.mercenary.creators.kotlin.util.*
import org.junit.jupiter.api.Test

class MetaTest : KotlinTest(MAIN_TEST_FILE) {
    @Test
    fun test() {
        val base = "root"
        val name = uuid().plus(".json")
        minio.traceOn()
        minio.save(name, base, TodoData.link())
        info { minio.statusOf(name, base) }
        minio.metaDataOf(name, base, MetaData("name_   " to "test_   "))
        info { minio.statusOf(name, base) }
        val many = 0.toAtomic()
        minio.itemsOf(base).forEach { each ->
            val meta = each.metaDataOf()
            if (meta.isNotEmpty()) {
                info { each.statusOf() }
                info { meta }
                many.increment()
            }
        }
        minio.delete(name, base)
        info { many }
        many shouldNotBe 0
    }
}