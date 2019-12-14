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

package co.mercenary.creators.kotlin.minio.test.util

import co.mercenary.creators.kotlin.minio.*
import co.mercenary.creators.kotlin.util.*
import java.util.*

abstract class AbstractKotlinMinioTest @JvmOverloads constructor(private val file: String = MAIN_TEST_PROPERTIES, private val path: String? = null) : AbstractKotlinTest() {

    protected val loader = contentResourceLoader

    override fun getConfigPropertiesBuilder(): () -> Properties = {
        Properties().also { prop ->
            loader[file].toInputStream().reader(Charsets.UTF_8).buffered().use { prop.load(it) }
        }
    }

    private val template: MinioTemplate by lazy {
        MinioTemplate(getConfigProperty("co.mercenary.creators.minio.server-url"), getConfigProperty("co.mercenary.creators.minio.access-key"), getConfigProperty("co.mercenary.creators.minio.secret-key")).also {
            if (path != null) {
                it.loaderOn(path)
            }
        }
    }

    protected val minio: MinioOperations
        get() = template
}