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

@file:kotlin.jvm.JvmName("MinioKt")

package co.mercenary.creators.kotlin.minio

import co.mercenary.creators.kotlin.util.*

const val S3_RESOURCE_PREFIX = "s3:"

const val MINIO_RESOURCE_PREFIX = "minio:"

const val X_AMAZON_REGION_USA_EAST_1 = "us-east-1"

const val X_AMAZON_META_HEADER_START = "x-amz-meta-"

@JvmOverloads
fun resolveContentType(path: String, type: String = DEFAULT_CONTENT_TYPE): String {
    if (isDefaultContentType(type)) {
        val name = path.toLowerTrim()
        if (name.endsWith(".kt")) {
            return "text/x-kotlin-source"
        }
        if (name.endsWith(".kts")) {
            return "text/x-kotlin-script"
        }
        return getDefaultContentTypeProbe().getContentType(path, type)
    }
    return type.toLowerTrim()
}

open class MinioFatalExceptiion(text: String?, root: Throwable?) : MercenaryFatalExceptiion(text, root) {
    constructor(text: String) : this(text, null)
    constructor(root: Throwable) : this(root.message, root)

    companion object {
        private const val serialVersionUID = 2L
    }
}

fun hashOf(vararg args: Any?): Int = MinioStatic.hashOf(*args)

